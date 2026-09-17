/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms.compiler.frontend;

import com.ventooth.swansong.uniforms.Type;
import com.ventooth.swansong.uniforms.UniformFunction;
import com.ventooth.swansong.uniforms.VecUtil;
import com.ventooth.swansong.uniforms.compiler.ast.ConstNode;
import com.ventooth.swansong.uniforms.compiler.ast.TypedNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedBoolNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedBranchNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedCastNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedFunctionNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedMathNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedMultiMatchNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedRelNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedUnaryMinusNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedUnaryNotNode;
import com.ventooth.swansong.uniforms.compiler.transform.Transformation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.joml.Vector2dc;
import org.joml.Vector3dc;
import org.joml.Vector4dc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class Optimizer implements Transformation<TypedNode, TypedNode> {
    @RequiredArgsConstructor
    public static final class Flags {
        public final boolean constantFolding;
        public final boolean shortCircuitBool;
        public final boolean keepSideEffects;
    }

    private final Flags flags;

    @Override
    public TypedNode transform(TypedNode node) {
        if (node instanceof ConstNode) {
            return node;
        } else if (node instanceof TypedBoolNode bool) {
            return optimizeBool(transform(bool.elems), bool.op);
        } else if (node instanceof TypedBranchNode branch) {
            return optimizeBranch(transform(branch.cond), transform(branch.ifTrue), transform(branch.ifFalse));
        } else if (node instanceof TypedCastNode cast) {
            return optimizeCast(cast.outputType(), transform(cast.input));
        } else if (node instanceof TypedFunctionNode fn) {
            return optimizeFn(fn.function, transform(fn.params));
        } else if (node instanceof TypedMathNode math) {
            return optimizeMath(transform(math.left), transform(math.right), math.op);
        } else if (node instanceof TypedMultiMatchNode multiMatch) {
            return optimizeMultiMatch(transform(multiMatch.elems));
        } else if (node instanceof TypedRelNode rel) {
            return optimizeRel(transform(rel.left), transform(rel.right), rel.op);
        } else if (node instanceof TypedUnaryMinusNode minus) {
            return optimizeUnaryMinus(transform(minus.param));
        } else if (node instanceof TypedUnaryNotNode not) {
            return optimizeUnaryNot(transform(not.param));
        } else {
            throw new UnsupportedOperationException(node.getClass()
                                                        .getName());
        }
    }

    private TypedNode optimizeUnaryNot(TypedNode input) {
        if (input instanceof TypedUnaryNotNode not) {
            return optimizeCast(Type.Bool, not.param);
        }
        if (flags.constantFolding) {
            val cst = constantFoldUnaryNot(input);
            if (cst != null) {
                return cst;
            }
        }
        return new TypedUnaryNotNode(input);
    }

    private TypedNode optimizeUnaryMinus(TypedNode input) {
        if (input instanceof TypedUnaryMinusNode minus) {
            return minus.param;
        }
        if (flags.constantFolding) {
            val cst = constantFoldUnaryMinus(input);
            if (cst != null) {
                return cst;
            }
        }
        return new TypedUnaryMinusNode(input);
    }


    //region base

    private TypedNode optimizeCast(Type output, TypedNode argument) {
        if (argument instanceof TypedCastNode argCast) {
            return optimizeCast(output, argCast.input);
        }
        if (argument.outputType() == output) {
            return argument;
        }
        if (flags.constantFolding) {
            val cst = constantFoldCast(output, argument);
            if (cst != null) {
                return cst;
            }
        }
        return new TypedCastNode(output, argument);
    }

    private TypedNode optimizeMath(TypedNode left, TypedNode right, TypedMathNode.Op op) {
        if (flags.constantFolding) {
            val cst = constantFoldMath(left, right, op);
            if (cst != null) {
                return cst;
            }
        }
        return new TypedMathNode(left, right, op);
    }

    private TypedNode optimizeBool(List<TypedNode> nodes, TypedBoolNode.Op op) {
        val newNodes = new ArrayList<TypedNode>(nodes.size());
        for (val node : nodes) {
            if (node instanceof TypedBoolNode bool && bool.op == op) {
                newNodes.addAll(bool.elems);
            } else {
                newNodes.add(node);
            }
        }
        nodes = Collections.unmodifiableList(newNodes);
        if (flags.constantFolding) {
            val cst = constantFoldBool(nodes, op);
            if (cst != null) {
                return cst;
            }
        }
        if (flags.shortCircuitBool) {
            return shortCircuitBool(nodes, op);
        }
        return new TypedBoolNode(nodes, op);
    }

    private TypedNode optimizeRel(TypedNode left, TypedNode right, TypedRelNode.Op op) {
        if (flags.constantFolding) {
            val cst = constantFoldRel(left, right, op);
            if (cst != null) {
                return cst;
            }
        }
        return new TypedRelNode(left, right, op);
    }

    private TypedNode optimizeBranch(TypedNode cond, TypedNode ifTrue, TypedNode ifFalse) {
        if (flags.constantFolding) {
            val cst = constantFoldBranch(cond, ifTrue, ifFalse);
            if (cst != null) {
                return cst;
            }
        }
        if (cond instanceof TypedUnaryNotNode not) {
            return new TypedBranchNode(not.param, ifFalse, ifTrue);
        }
        return new TypedBranchNode(cond, ifTrue, ifFalse);
    }

    private TypedNode optimizeMultiMatch(List<TypedNode> elems) {
        if (elems.size() == 2) {
            return optimizeRel(elems.get(0), elems.get(1), TypedRelNode.Op.Eq);
        }
        if (flags.constantFolding) {
            val cst = constantFoldMultiMatch(elems);
            if (cst != null) {
                return cst;
            }
        }
        return new TypedMultiMatchNode(elems);
    }

    private TypedNode optimizeFn(UniformFunction fn, List<TypedNode> params) {
        val size = params.size();
        if (flags.constantFolding) {
            val cst = constantFoldFn(fn, params);
            if (cst != null) {
                return cst;
            }
        }
        return new TypedFunctionNode(fn, params);
    }

    //endregion

    //region constant folding

    private TypedNode constantFoldUnaryNot(TypedNode argument) {
        if (!(argument instanceof ConstNode)) {
            return null;
        }
        return ConstNode.Bool.of(switch (argument.outputType()) {
            case Bool -> !((ConstNode.Bool) argument).value();
            case Int -> ((ConstNode.Int) argument).value == 0;
            case Float -> ((ConstNode.Float) argument).value == 0;
            case Vec2 -> ((ConstNode.Vec2) argument).value()
                                                    .equals(0, 0);
            case Vec3 -> ((ConstNode.Vec3) argument).value()
                                                    .equals(0, 0, 0);
            case Vec4 -> ((ConstNode.Vec4) argument).value()
                                                    .equals(0, 0, 0, 0);
        });
    }

    private TypedNode constantFoldUnaryMinus(TypedNode argument) {
        if (!(argument instanceof ConstNode)) {
            return null;
        }
        return switch (argument.outputType()) {
            case Bool -> ((ConstNode.Bool) argument).value() ? ConstNode.Bool.False : ConstNode.Int.of(-1);
            case Int -> ConstNode.Int.of(-((ConstNode.Int) argument).value);
            case Float -> new ConstNode.Float(-((ConstNode.Float) argument).value);
            case Vec2 -> new ConstNode.Vec2(VecUtil.neg(((ConstNode.Vec2) argument).value()));
            case Vec3 -> new ConstNode.Vec3(VecUtil.neg(((ConstNode.Vec3) argument).value()));
            case Vec4 -> new ConstNode.Vec4(VecUtil.neg(((ConstNode.Vec4) argument).value()));
        };
    }

    private TypedNode constantFoldCast(Type output, TypedNode argument) {
        if (!(argument instanceof ConstNode)) {
            return null;
        }
        return switch (argument.outputType()) {
            case Int -> {
                val intVal = ((ConstNode.Int) argument).value;
                yield switch (output) {
                    case Float -> new ConstNode.Float(intVal);
                    case Vec2 -> new ConstNode.Vec2(intVal, intVal);
                    case Vec3 -> new ConstNode.Vec3(intVal, intVal, intVal);
                    case Vec4 -> new ConstNode.Vec4(intVal, intVal, intVal, intVal);
                    default -> null;
                };
            }
            case Float -> {
                val floatVal = ((ConstNode.Float) argument).value;
                yield switch (output) {
                    case Int -> ConstNode.Int.of((int) floatVal);
                    case Vec2 -> new ConstNode.Vec2(floatVal, floatVal);
                    case Vec3 -> new ConstNode.Vec3(floatVal, floatVal, floatVal);
                    case Vec4 -> new ConstNode.Vec4(floatVal, floatVal, floatVal, floatVal);
                    default -> null;
                };
            }
            case Bool -> {
                val intVal = ((ConstNode.Bool) argument).value() ? 1 : 0;
                yield switch (output) {
                    case Float -> new ConstNode.Float(intVal);
                    case Vec2 -> new ConstNode.Vec2(intVal, intVal);
                    case Vec3 -> new ConstNode.Vec3(intVal, intVal, intVal);
                    case Vec4 -> new ConstNode.Vec4(intVal, intVal, intVal, intVal);
                    default -> null;
                };
            }
            default -> null;
        };
    }

    private TypedNode constantFoldMath(TypedNode left, TypedNode right, TypedMathNode.Op op) {
        if (!(left instanceof ConstNode) || !(right instanceof ConstNode)) {
            return null;
        }
        return switch (left.outputType()) {
            case Int -> {
                val aVal = ((ConstNode.Int) left).value;
                val bVal = ((ConstNode.Int) right).value;
                yield switch (op) {
                    case Add -> ConstNode.Int.of(aVal + bVal);
                    case Sub -> ConstNode.Int.of(aVal - bVal);
                    case Mul -> ConstNode.Int.of(aVal * bVal);
                    case Div -> ConstNode.Int.of(aVal / bVal);
                    case Rem -> ConstNode.Int.of(aVal % bVal);
                };
            }
            case Float -> {
                val aVal = ((ConstNode.Float) left).value;
                val bVal = ((ConstNode.Float) right).value;
                yield switch (op) {
                    case Add -> new ConstNode.Float(aVal + bVal);
                    case Sub -> new ConstNode.Float(aVal - bVal);
                    case Mul -> new ConstNode.Float(aVal * bVal);
                    case Div -> new ConstNode.Float(aVal / bVal);
                    case Rem -> new ConstNode.Float(aVal % bVal);
                };
            }
            case Vec2 -> {
                val vLeft = ((ConstNode.Vec2) left).value();
                val vRight = ((ConstNode.Vec2) right).value();
                yield new ConstNode.Vec2(switch (op) {
                    case Add -> VecUtil.add(vLeft, vRight);
                    case Sub -> VecUtil.sub(vLeft, vRight);
                    case Mul -> VecUtil.mul(vLeft, vRight);
                    case Div -> VecUtil.div(vLeft, vRight);
                    case Rem -> VecUtil.rem(vLeft, vRight);
                });
            }
            case Vec3 -> {
                val vLeft = ((ConstNode.Vec3) left).value();
                val vRight = ((ConstNode.Vec3) right).value();
                yield new ConstNode.Vec3(switch (op) {
                    case Add -> VecUtil.add(vLeft, vRight);
                    case Sub -> VecUtil.sub(vLeft, vRight);
                    case Mul -> VecUtil.mul(vLeft, vRight);
                    case Div -> VecUtil.div(vLeft, vRight);
                    case Rem -> VecUtil.rem(vLeft, vRight);
                });
            }
            case Vec4 -> {
                val vLeft = ((ConstNode.Vec4) left).value();
                val vRight = ((ConstNode.Vec4) right).value();
                yield new ConstNode.Vec4(switch (op) {
                    case Add -> VecUtil.add(vLeft, vRight);
                    case Sub -> VecUtil.sub(vLeft, vRight);
                    case Mul -> VecUtil.mul(vLeft, vRight);
                    case Div -> VecUtil.div(vLeft, vRight);
                    case Rem -> VecUtil.rem(vLeft, vRight);
                });
            }
            default -> null;
        };
    }

    private TypedNode constantFoldBool(List<TypedNode> nodes, TypedBoolNode.Op op) {
        for (val node : nodes) {
            if (!(node instanceof ConstNode.Bool boolNode)) {
                return null;
            }
            val nodeVal = boolNode.value();
            switch (op) {
                case And -> {
                    if (!nodeVal) {
                        return ConstNode.Bool.of(false);
                    }
                }
                case Or -> {
                    if (nodeVal) {
                        return ConstNode.Bool.of(true);
                    }
                }
            }
        }
        return ConstNode.Bool.of(switch (op) {
            case And -> true;
            case Or -> false;
        });
    }

    private ConstNode.Bool constantFoldRel(TypedNode left, TypedNode right, TypedRelNode.Op op) {
        if (!(left instanceof ConstNode) || !(right instanceof ConstNode)) {
            return null;
        }
        return switch (left.outputType()) {
            case Int -> {
                val aVal = ((ConstNode.Int) left).value;
                val bVal = ((ConstNode.Int) right).value;
                yield ConstNode.Bool.of(switch (op) {
                    case Eq -> aVal == bVal;
                    case Ne -> aVal != bVal;
                    case Ge -> aVal >= bVal;
                    case Gt -> aVal > bVal;
                    case Le -> aVal <= bVal;
                    case Lt -> aVal < bVal;
                });
            }
            case Float -> {
                val aVal = ((ConstNode.Float) left).value;
                val bVal = ((ConstNode.Float) right).value;
                yield ConstNode.Bool.of(switch (op) {
                    case Eq -> aVal == bVal;
                    case Ne -> aVal != bVal;
                    case Ge -> aVal >= bVal;
                    case Gt -> aVal > bVal;
                    case Le -> aVal <= bVal;
                    case Lt -> aVal < bVal;
                });
            }
            case Bool -> {
                val aVal = ((ConstNode.Bool) left).value();
                val bVal = ((ConstNode.Bool) right).value();
                yield ConstNode.Bool.of(switch (op) {
                    case Eq -> aVal == bVal;
                    case Ne -> aVal != bVal;
                    case Ge -> aVal || !bVal;
                    case Gt -> aVal && !bVal;
                    case Le -> !aVal || bVal;
                    case Lt -> !aVal && bVal;
                });
            }
            default -> null;
        };
    }

    private TypedNode constantFoldBranch(TypedNode cond, TypedNode ifTrue, TypedNode ifFalse) {
        if (!(cond instanceof ConstNode.Bool boolCond)) {
            return null;
        }
        if (boolCond.value()) {
            return ifTrue;
        } else {
            return ifFalse;
        }
    }

    private TypedNode constantFoldMultiMatch(List<TypedNode> elems) {
        val value = elems.get(0);
        if (!(value instanceof ConstNode cstVal)) {
            return null;
        }
        int successfulFolds = 0;
        for (int i = 1, size = elems.size(); i < size; i++) {
            val elem = elems.get(i);
            if (elem instanceof ConstNode cstElem) {
                val folded = constantFoldRel(cstVal, cstElem, TypedRelNode.Op.Eq);
                if (folded != null) {
                    if (folded.value()) {
                        return folded;
                    }
                    successfulFolds++;
                }
            }
        }
        if (successfulFolds == elems.size() - 1) {
            return ConstNode.Bool.False;
        }
        return null;
    }

    private TypedNode constantFoldFn(UniformFunction method, List<TypedNode> params) {
        val cf = method.constantFoldMethod();
        if (cf == null) {
            return null;
        }
        val size = params.size();
        var args = new Object[size];
        for (int i = 0; i < size; i++) {
            val param = params.get(i);
            if (!(param instanceof ConstNode)) {
                return null;
            }
            val arg = switch (param.outputType()) {
                case Bool -> ((ConstNode.Bool) param).value();
                case Int -> ((ConstNode.Int) param).value;
                case Float -> ((ConstNode.Float) param).value;
                case Vec2 -> ((ConstNode.Vec2) param).value();
                case Vec3 -> ((ConstNode.Vec3) param).value();
                case Vec4 -> ((ConstNode.Vec4) param).value();
                default -> null;
            };
            if (arg == null) {
                return null;
            }
            args[i] = arg;
        }
        Object result;
        try {
            result = cf.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
        return switch (method.returns()) {
            case Int -> ConstNode.Int.of((Integer) result);
            case Float -> new ConstNode.Float((Double) result);
            case Bool -> ConstNode.Bool.of((Boolean) result);
            case Vec2 -> new ConstNode.Vec2((Vector2dc) result);
            case Vec3 -> new ConstNode.Vec3((Vector3dc) result);
            case Vec4 -> new ConstNode.Vec4((Vector4dc) result);
            default -> throw new AssertionError();
        };
    }

    //endregion

    //region misc

    private TypedNode shortCircuitBool(List<TypedNode> nodes, TypedBoolNode.Op op) {
        val remaining = new ArrayList<TypedNode>(nodes.size());
        for (val node : nodes) {
            if (!(node instanceof ConstNode.Bool nodeConst)) {
                remaining.add(node);
                continue;
            }
            val nodeVal = nodeConst.value();
            if (!(op == TypedBoolNode.Op.Or && nodeVal || op == TypedBoolNode.Op.And && !nodeVal)) {
                continue;
            }
            if (remaining.isEmpty() || !flags.keepSideEffects) {
                return ConstNode.Bool.of(nodeVal);
            } else {
                break;
            }
        }
        if (remaining.isEmpty()) {
            return ConstNode.Bool.of(switch (op) {
                case And -> true;
                case Or -> false;
            });
        }
        if (remaining.size() == 1) {
            return remaining.get(0);
        }
        return new TypedBoolNode(Collections.unmodifiableList(remaining), op);
    }

    //endregion
}
