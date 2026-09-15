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

import com.ventooth.swansong.mathparser.AbstractParser;
import com.ventooth.swansong.uniforms.Type;
import com.ventooth.swansong.uniforms.UniformFunction;
import com.ventooth.swansong.uniforms.UniformFunctionRegistry;
import com.ventooth.swansong.uniforms.VecUtil;
import com.ventooth.swansong.uniforms.compiler.ast.ConstNode;
import com.ventooth.swansong.uniforms.compiler.ast.TypedNode;
import com.ventooth.swansong.uniforms.compiler.ast.UntypedNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedBoolNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedBranchNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedCastNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedFunctionNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedMathNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedMultiMatchNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedRelNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedUnaryMinusNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedUnaryNotNode;
import com.ventooth.swansong.uniforms.compiler.ast.untyped.UntypedBinaryNode;
import com.ventooth.swansong.uniforms.compiler.ast.untyped.UntypedFunctionNode;
import com.ventooth.swansong.uniforms.compiler.ast.untyped.UntypedSwizzleNode;
import com.ventooth.swansong.uniforms.compiler.ast.untyped.UntypedUnaryNode;
import com.ventooth.swansong.uniforms.compiler.ast.untyped.UntypedVarNode;
import com.ventooth.swansong.uniforms.compiler.transform.Transformation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class TypeResolver implements Transformation<UntypedNode, TypedNode> {
    @RequiredArgsConstructor
    public static final class Flags {
        public final boolean castIntDivToFloat;
    }

    private final Flags flags;
    private final UniformFunctionRegistry registry;
    private int statefulIndexedCounter = 0;

    @Override
    public TypedNode transform(UntypedNode input) {
        if (input instanceof ConstNode cst) {
            return cst;
        } else if (input instanceof UntypedBinaryNode bin) {
            return resolveBinary(transform(bin.left), transform(bin.right), bin.operator);
        } else if (input instanceof UntypedFunctionNode fn) {
            return resolveFunction(fn.name, transform(fn.params));
        } else if (input instanceof UntypedSwizzleNode swiz) {
            return resolveSwizzle(transform(swiz.value), swiz.index);
        } else if (input instanceof UntypedUnaryNode un) {
            return resolveUnary(transform(un.param), un.op);
        } else if (input instanceof UntypedVarNode var) {
            return resolveFunction(var.name, Collections.emptyList());
        } else {
            throw new AssertionError(input.getClass()
                                          .getName());
        }
    }

    //region binary

    private TypedNode resolveBinary(TypedNode left, TypedNode right, AbstractParser.Operator operator) {
        return switch (operator) {
            case Add -> resolveMath(left, right, TypedMathNode.Op.Add);
            case Sub -> resolveMath(left, right, TypedMathNode.Op.Sub);
            case Mul -> resolveMath(left, right, TypedMathNode.Op.Mul);
            case Div -> resolveMath(left, right, TypedMathNode.Op.Div);
            case Rem -> resolveMath(left, right, TypedMathNode.Op.Rem);
            case Eq -> resolveRel(left, right, TypedRelNode.Op.Eq);
            case Ne -> resolveRel(left, right, TypedRelNode.Op.Ne);
            case Ge -> resolveRel(left, right, TypedRelNode.Op.Ge);
            case Gt -> resolveRel(left, right, TypedRelNode.Op.Gt);
            case Le -> resolveRel(left, right, TypedRelNode.Op.Le);
            case Lt -> resolveRel(left, right, TypedRelNode.Op.Lt);
            case Or -> resolveBool(Arrays.asList(left, right), TypedBoolNode.Op.Or);
            case And -> resolveBool(Arrays.asList(left, right), TypedBoolNode.Op.And);
        };
    }

    private TypedNode resolveMath(TypedNode left, TypedNode right, TypedMathNode.Op op) {
        val p = coerce(left, right);
        left = p.getLeft();
        right = p.getRight();
        switch (left.outputType()) {
            case Bool -> {
                if (flags.castIntDivToFloat && op == TypedMathNode.Op.Div) {
                    left = new TypedCastNode(Type.Float, left);
                    right = new TypedCastNode(Type.Float, right);
                } else {
                    left = new TypedCastNode(Type.Int, left);
                    right = new TypedCastNode(Type.Int, right);
                }
            }
            case Int -> {
                if (flags.castIntDivToFloat && op == TypedMathNode.Op.Div) {
                    left = new TypedCastNode(Type.Float, left);
                    right = new TypedCastNode(Type.Float, right);
                }
            }
            case Vec2, Vec3, Vec4 -> {
                val name = switch (op) {
                    case Add -> "add";
                    case Sub -> "sub";
                    case Mul -> "mul";
                    case Div -> "div";
                    case Rem -> "rem";
                };
                val theOp = VecUtil.REGISTRY.resolve(name, Arrays.asList(left.outputType(), right.outputType()));
                if (theOp == null) {
                    //This should never happen
                    throw new AssertionError("Unknown binary operation between vectors " + op);
                }
                return new TypedFunctionNode(theOp, Arrays.asList(left, right));
            }
        }
        return new TypedMathNode(left, right, op);
    }

    private TypedNode resolveRel(TypedNode left, TypedNode right, TypedRelNode.Op op) {
        val p = coerce(left, right);
        return new TypedRelNode(p.getLeft(), p.getRight(), op);
    }

    private TypedNode resolveBool(List<TypedNode> nodes, TypedBoolNode.Op op) {
        for (val node : nodes) {
            if (node.outputType() != Type.Bool) {
                throw new IllegalArgumentException();
            }
        }
        return new TypedBoolNode(nodes, op);
    }

    //endregion

    //region unary

    private TypedNode resolveUnary(TypedNode param, UntypedUnaryNode.Op op) {
        return switch (op) {
            case Not -> new TypedUnaryNotNode(param);
            case Minus -> switch (param.outputType()) {
                case Bool, Int, Float -> new TypedUnaryMinusNode(param);
                case Vec2, Vec3, Vec4 -> {
                    val theOp = VecUtil.REGISTRY.resolve("neg", Collections.singletonList(param.outputType()));
                    if (theOp == null) {
                        throw new AssertionError("Could not find vector negation function!");
                    }
                    yield new TypedFunctionNode(theOp, Collections.singletonList(param));
                }
            };
        };
    }

    //endregion

    //region fn

    private TypedNode resolveFunction(String name, List<TypedNode> params) {
        val size = params.size();
        switch (name) {
            case "if" -> {
                if (size != 3) {
                    throw new IllegalArgumentException();
                }
                return resolveBranch(params.get(0), params.get(1), params.get(2));
            }
            case "in" -> {
                if (size < 2) {
                    throw new IllegalArgumentException();
                }
                return resolveMultiMatch(params);
            }
        }
        val types = new ArrayList<Type>(params.size());
        for (val param : params) {
            types.add(param.outputType());
        }
        val function = registry.resolve(name, types);
        if (function != null) {
            return resolveFunction(function, params);
        }
        throw new IllegalStateException("Unknown uniform variable/function \"" + name + "\" with parameters: " + types);
    }

    private TypedNode resolveFunction(UniformFunction fn, List<TypedNode> params) {
        val size = params.size();
        val types = fn.params();
        val newParams = new ArrayList<TypedNode>(size);
        int i = 0;
        if (fn.statefulIndexed() &&
            !params.isEmpty() &&
            params.get(0)
                  .outputType() == Type.Int) {
            i = 1;
            newParams.add(ConstNode.Int.of(statefulIndexedCounter++));
        }
        for (; i < size; i++) {
            var param = params.get(i);
            val type = types.get(i);
            if (param.outputType() != type) {
                newParams.add(new TypedCastNode(type, param));
            } else {
                newParams.add(param);
            }
        }
        return new TypedFunctionNode(fn, Collections.unmodifiableList(newParams));
    }

    private TypedNode resolveBranch(TypedNode cond, TypedNode ifTrue, TypedNode ifFalse) {
        val p = coerce(ifTrue, ifFalse);
        return new TypedBranchNode(cond, ifTrue, ifFalse);
    }

    private TypedNode resolveMultiMatch(List<TypedNode> elems) {
        val resElems = new ArrayList<>(elems);
        coerce(resElems);
        return new TypedMultiMatchNode(Collections.unmodifiableList(resElems));
    }

    //endregion

    //region swizzle

    private TypedNode resolveSwizzle(TypedNode value, int index) {
        val fn = VecUtil.REGISTRY.resolve("swiz", Arrays.asList(value.outputType(), Type.Int));
        if (fn == null) {
            throw new IllegalStateException("Could not generate swizzle for value of type " + value.outputType());
        }
        return new TypedFunctionNode(fn, Arrays.asList(value, ConstNode.Int.of(index)));
    }

    //endregion

    //region utils

    private void coerce(ArrayList<TypedNode> elems) {
        if (elems.isEmpty()) {
            return;
        }
        val size = elems.size();
        var outType = elems.get(0)
                           .outputType();
        for (int i = 1; i < size; i++) {
            outType = Type.coerce(outType,
                                  elems.get(i)
                                       .outputType());
        }
        for (int i = 0; i < size; i++) {
            val elem = elems.get(i);
            if (elem.outputType() != outType) {
                elems.set(i, new TypedCastNode(outType, elem));
            }
        }
    }

    private Pair<TypedNode, TypedNode> coerce(TypedNode left, TypedNode right) {
        val lt = left.outputType();
        val rt = right.outputType();
        val outType = Type.coerce(lt, rt);
        if (outType != lt) {
            left = new TypedCastNode(outType, left);
        }
        if (outType != rt) {
            right = new TypedCastNode(outType, right);
        }
        return Pair.of(left, right);
    }

    //endregion
}
