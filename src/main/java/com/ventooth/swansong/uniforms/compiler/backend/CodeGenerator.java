/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms.compiler.backend;

import com.ventooth.swansong.uniforms.Builtins;
import com.ventooth.swansong.uniforms.Type;
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
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CodeGenerator {
    @RequiredArgsConstructor
    public static final class Flags {
        public final boolean inlineMultiMatchConst;
        public final boolean fuseIfElseRelational;
    }

    private final Flags flags;

    public CodeGenerator(Flags flags, boolean isStatic) {
        this.flags = flags;
        this.localVariableIndex = isStatic ? 0 : 1;
    }

    public void genExpr(TypedNode input, InsnList insnList) {
        if (input instanceof ConstNode constant) {
            genConst(constant, insnList);
        } else if (input instanceof TypedBoolNode bool) {
            genBool(bool, insnList);
        } else if (input instanceof TypedBranchNode branch) {
            genBranch(branch, insnList);
        } else if (input instanceof TypedCastNode cast) {
            genCast(cast.input, cast.outputType(), insnList);
        } else if (input instanceof TypedFunctionNode fn) {
            genFn(fn, insnList);
        } else if (input instanceof TypedMathNode math) {
            genMath(math, insnList);
        } else if (input instanceof TypedMultiMatchNode match) {
            genMultiMatch(match, insnList);
        } else if (input instanceof TypedRelNode rel) {
            genRel(rel, insnList);
        } else if (input instanceof TypedUnaryMinusNode minus) {
            genMinus(minus, insnList);
        } else {
            throw new UnsupportedOperationException(input.getClass()
                                                         .getName());
        }
    }

    //region direct

    private void genConst(ConstNode constant, InsnList insnList) {
        AbstractInsnNode insn;
        if (constant instanceof ConstNode.Int intCst) {
            val i = intCst.value;
            insn = switch (i) {
                case -1 -> new InsnNode(Opcodes.ICONST_M1);
                case 0 -> new InsnNode(Opcodes.ICONST_0);
                case 1 -> new InsnNode(Opcodes.ICONST_1);
                case 2 -> new InsnNode(Opcodes.ICONST_2);
                case 3 -> new InsnNode(Opcodes.ICONST_3);
                case 4 -> new InsnNode(Opcodes.ICONST_4);
                case 5 -> new InsnNode(Opcodes.ICONST_5);
                default -> new LdcInsnNode(i);
            };
        } else if (constant instanceof ConstNode.Float floatCst) {
            val f = floatCst.value;
            if (f == 0.0) {
                insn = new InsnNode(Opcodes.DCONST_0);
            } else if (f == 1.0) {
                insn = new InsnNode(Opcodes.DCONST_1);
            } else {
                insn = new LdcInsnNode(f);
            }
        } else if (constant instanceof ConstNode.Bool boolCst) {
            insn = new InsnNode(boolCst.value() ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        } else if (constant instanceof ConstNode.Vec2 vecCst) {
            val x = vecCst.x;
            val y = vecCst.y;
            if (x == y) {
                genFn(new TypedFunctionNode(Builtins.REGISTRY.resolve("vec2", Collections.singletonList(Type.Float)),
                                            Collections.singletonList(new ConstNode.Float(x))), insnList);
            } else {
                genFn(new TypedFunctionNode(Builtins.REGISTRY.resolve("vec2", Arrays.asList(Type.Float, Type.Float)),
                                            Arrays.asList(new ConstNode.Float(x), new ConstNode.Float(y))), insnList);
            }
            return;
        } else if (constant instanceof ConstNode.Vec3 vecCst) {
            val x = vecCst.x;
            val y = vecCst.y;
            val z = vecCst.z;
            if (x == y && x == z) {
                genFn(new TypedFunctionNode(Builtins.REGISTRY.resolve("vec3", Collections.singletonList(Type.Float)),
                                            Collections.singletonList(new ConstNode.Float(x))), insnList);
            } else {
                genFn(new TypedFunctionNode(Builtins.REGISTRY.resolve("vec3",
                                                                      Arrays.asList(Type.Float,
                                                                                    Type.Float,
                                                                                    Type.Float)),
                                            Arrays.asList(new ConstNode.Float(x),
                                                          new ConstNode.Float(y),
                                                          new ConstNode.Float(z))), insnList);
            }
            return;
        } else if (constant instanceof ConstNode.Vec4 vecCst) {
            val x = vecCst.x;
            val y = vecCst.y;
            val z = vecCst.z;
            val w = vecCst.w;
            if (x == y && x == z && x == w) {
                genFn(new TypedFunctionNode(Builtins.REGISTRY.resolve("vec4", Collections.singletonList(Type.Float)),
                                            Collections.singletonList(new ConstNode.Float(x))), insnList);
            } else {
                genFn(new TypedFunctionNode(Builtins.REGISTRY.resolve("vec4",
                                                                      Arrays.asList(Type.Float,
                                                                                    Type.Float,
                                                                                    Type.Float,
                                                                                    Type.Float)),
                                            Arrays.asList(new ConstNode.Float(x),
                                                          new ConstNode.Float(y),
                                                          new ConstNode.Float(z),
                                                          new ConstNode.Float(w))), insnList);
            }
            return;
        } else {
            throw new IllegalArgumentException();
        }
        insnList.add(insn);
    }

    private void genCast(TypedNode input, Type outType, InsnList insnList) {
        genExpr(input, insnList);
        val inType = input.outputType();
        switch (outType) {
            case Bool -> {
                if (inType != Type.Bool) {
                    throw new IllegalArgumentException();
                }
            }
            case Int -> {
                switch (inType) {
                    case Bool, Int -> {
                    }
                    case Float -> insnList.add(new InsnNode(Opcodes.D2I));
                    default -> throw new IllegalArgumentException();
                }
            }
            case Float -> {
                switch (inType) {
                    case Bool, Int -> insnList.add(new InsnNode(Opcodes.I2D));
                    case Float -> {
                    }
                    default -> throw new IllegalArgumentException();
                }
            }
            case Vec2, Vec3, Vec4 -> {
                if (inType == Type.Int || inType == Type.Bool) {
                    insnList.add(new InsnNode(Opcodes.I2D));
                }
                val fnName = outType.name()
                                    .toLowerCase();
                val fn = Builtins.REGISTRY.resolve(fnName, Collections.singletonList(Type.Float));
                if (fn == null) {
                    throw new IllegalArgumentException();
                }
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                                fn.javaOwner(),
                                                fn.javaName(),
                                                Type.methodDescriptor(outType, Type.Float),
                                                false));
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private void genMath(TypedMathNode math, InsnList insnList) {
        genExpr(math.left, insnList);
        genExpr(math.right, insnList);
        val type = math.outputType();
        val op = math.op;
        val insn = switch (type) {
            case Int -> switch (op) {
                case Add -> new InsnNode(Opcodes.IADD);
                case Sub -> new InsnNode(Opcodes.ISUB);
                case Mul -> new InsnNode(Opcodes.IMUL);
                case Div -> new InsnNode(Opcodes.IDIV);
                case Rem -> new InsnNode(Opcodes.IREM);
            };
            case Float -> switch (op) {
                case Add -> new InsnNode(Opcodes.DADD);
                case Sub -> new InsnNode(Opcodes.DSUB);
                case Mul -> new InsnNode(Opcodes.DMUL);
                case Div -> new InsnNode(Opcodes.DDIV);
                case Rem -> new InsnNode(Opcodes.DREM);
            };
            default -> throw new IllegalArgumentException();
        };
        insnList.add(insn);
    }

    private void genMinus(TypedUnaryMinusNode minus, InsnList insnList) {
        genExpr(minus.param, insnList);
        val type = minus.outputType();
        val insn = switch (type) {
            case Bool, Int -> Opcodes.INEG;
            case Float -> Opcodes.DNEG;
            default -> throw new IllegalArgumentException();
        };
        insnList.add(new InsnNode(insn));
    }

    private void genNot(TypedUnaryNotNode not, InsnList insnList) {
        genBranchIndirect(not.param, new IndirectTarget.Expr(ConstNode.Bool.False, ConstNode.Bool.True), insnList);
    }

    private void genRel(TypedRelNode rel, InsnList insnList) {
        genRelIndirect(rel, new IndirectTarget.Expr(ConstNode.Bool.True, ConstNode.Bool.False), insnList);
    }

    private void genBranch(TypedBranchNode branch, InsnList insnList) {
        genBranchIndirect(branch.cond, new IndirectTarget.Expr(branch.ifTrue, branch.ifFalse), insnList);
    }

    private void genMultiMatch(TypedMultiMatchNode match, InsnList insnList) {
        genMultiMatchIndirect(match, new IndirectTarget.Expr(ConstNode.Bool.True, ConstNode.Bool.False), insnList);
    }

    private void genBool(TypedBoolNode bool, InsnList insnList) {
        genBoolIndirect(bool, new IndirectTarget.Expr(ConstNode.Bool.True, ConstNode.Bool.False), insnList);
    }

    private void genFn(TypedFunctionNode fn, InsnList insnList) {
        val function = fn.function;
        val desc = new StringBuilder("(");
        val paramVals = fn.params;
        val params = function.params();
        for (int i = 0; i < params.size(); i++) {
            genExpr(paramVals.get(i), insnList);
            desc.append(params.get(i)
                              .descriptor());
        }
        desc.append(')')
            .append(function.returns()
                            .descriptor());
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                        function.javaOwner(),
                                        function.javaName(),
                                        desc.toString(),
                                        false));
    }

    //endregion

    //region indirect

    private void genRelIndirect(TypedRelNode rel, IndirectTarget tgt, InsnList insnList) {
        genExpr(rel.left, insnList);
        genExpr(rel.right, insnList);
        val op = rel.op;
        val jumpInsn = prepareJumpInsn(rel.left.outputType(), op, insnList);
        genTrueFalseBranch(tgt, jumpInsn, insnList);
    }

    private void genBranchIndirect(TypedNode cond, IndirectTarget tgt, InsnList insnList) {
        if (flags.fuseIfElseRelational) {
            if (cond instanceof TypedRelNode rel) {
                genRelIndirect(rel, tgt, insnList);
                return;
            } else if (cond instanceof TypedMultiMatchNode match) {
                genMultiMatchIndirect(match, tgt, insnList);
                return;
            } else if (cond instanceof TypedBoolNode bool) {
                genBoolIndirect(bool, tgt, insnList);
                return;
            }
        }
        genExpr(cond, insnList);
        genTrueFalseBranch(tgt, JumpInsn.BOOL, insnList);
    }

    private void genMultiMatchIndirect(TypedMultiMatchNode match, IndirectTarget tgt, InsnList insnList) {
        val elems = match.elems;
        val root = elems.get(0);
        val type = root.outputType();
        if (tgt instanceof IndirectTarget.Expr expr) {
            val trueLabel = new LabelNode();
            genMultiMatchChain(root, elems, type, trueLabel, null, insnList);
            genIndirectExprPair(expr.ifFalse, expr.ifTrue, trueLabel, insnList);
        } else if (tgt instanceof IndirectTarget.Label lbl) {
            val genTrue = !lbl.cond;
            val trueLabel = lbl.cond ? lbl.labelCond : new LabelNode();
            val falseLabel = lbl.cond ? null : lbl.labelCond;
            genMultiMatchChain(root, elems, type, trueLabel, falseLabel, insnList);
            if (genTrue) {
                insnList.add(new JumpInsnNode(Opcodes.GOTO, lbl.labelCond));
                insnList.add(trueLabel);
            }
        }
    }

    private void genMultiMatchChain(TypedNode root,
                                    List<TypedNode> elems,
                                    Type type,
                                    LabelNode trueLabel,
                                    LabelNode falseLabel,
                                    InsnList insnList) {
        if (flags.inlineMultiMatchConst && root instanceof ConstNode) {
            for (int i = 1, size = elems.size(); i < size; i++) {
                genExpr(elems.get(i), insnList);
                genExpr(root, insnList);
                val jumpInsn = prepareJumpInsn(type, TypedRelNode.Op.Eq, insnList);
                insnList.add(new JumpInsnNode(jumpInsn.jumpOpCode(true), trueLabel));
            }
            if (falseLabel != null) {
                insnList.add(new JumpInsnNode(Opcodes.GOTO, falseLabel));
            }
            return;
        }
        genExpr(root, insnList);
        val t = root.outputType();
        val local = localAlloc(t);
        try {
            genLocalStore(t, local, insnList);
            for (int i = 1, size = elems.size(); i < size; i++) {
                genExpr(elems.get(i), insnList);
                genLocalLoad(t, local, insnList);
                val jumpInsn = prepareJumpInsn(type, TypedRelNode.Op.Eq, insnList);
                insnList.add(new JumpInsnNode(jumpInsn.jumpOpCode(true), trueLabel));
            }
            if (falseLabel != null) {
                insnList.add(new JumpInsnNode(Opcodes.GOTO, falseLabel));
            }
        } finally {
            localFree(t, local);
        }
    }

    private void genBoolIndirect(TypedBoolNode bool, IndirectTarget tgt, InsnList insnList) {
        val elems = bool.elems;
        val op = bool.op;
        if (tgt instanceof IndirectTarget.Expr expr) {
            TypedNode shortCircuit;
            TypedNode passThrough;
            boolean scBranch;
            switch (op) {
                case And -> {
                    shortCircuit = expr.ifFalse;
                    passThrough = expr.ifTrue;
                    scBranch = false;
                }
                case Or -> {
                    shortCircuit = expr.ifTrue;
                    passThrough = expr.ifFalse;
                    scBranch = true;
                }
                default -> throw new AssertionError();
            }
            val scLabel = new LabelNode();
            val lbl = new IndirectTarget.Label(scLabel, scBranch);
            for (val elem : elems) {
                genBranchIndirect(elem, lbl, insnList);
            }
            genIndirectExprPair(passThrough, shortCircuit, scLabel, insnList);
        } else if (tgt instanceof IndirectTarget.Label lbl) {
            LabelNode shortCircuit;
            LabelNode passThrough;
            boolean scGen;
            val scBranch = switch (op) {
                case And -> false;
                case Or -> true;
            };
            if (scBranch == lbl.cond) {
                shortCircuit = lbl.labelCond;
                passThrough = null;
                scGen = false;
            } else {
                shortCircuit = new LabelNode();
                passThrough = lbl.labelCond;
                scGen = true;
            }
            val lbl2 = new IndirectTarget.Label(shortCircuit, scBranch);
            for (val elem : elems) {
                genBranchIndirect(elem, lbl2, insnList);
            }
            if (scGen) {
                insnList.add(new JumpInsnNode(Opcodes.GOTO, passThrough));
                insnList.add(shortCircuit);
            }
        }
    }

    // common code

    private void genTrueFalseBranch(IndirectTarget tgt, JumpInsn jumpInsn, InsnList insnList) {
        if (tgt instanceof IndirectTarget.Expr expr) {
            val falseLabel = new LabelNode();
            insnList.add(new JumpInsnNode(jumpInsn.jumpOpCode(false), falseLabel));
            genIndirectExprPair(expr.ifTrue, expr.ifFalse, falseLabel, insnList);
        } else if (tgt instanceof IndirectTarget.Label lbl) {
            insnList.add(new JumpInsnNode(jumpInsn.jumpOpCode(lbl.cond), lbl.labelCond));
        }
    }

    private void genIndirectExprPair(TypedNode first, TypedNode second, LabelNode secondLabel, InsnList insnList) {
        val endLabel = new LabelNode();
        genExpr(first, insnList);
        insnList.add(new JumpInsnNode(Opcodes.GOTO, endLabel));
        insnList.add(secondLabel);
        genExpr(second, insnList);
        insnList.add(endLabel);
    }

    private interface IndirectTarget {
        @RequiredArgsConstructor
        final class Expr implements IndirectTarget {
            public final TypedNode ifTrue;
            public final TypedNode ifFalse;
        }

        @RequiredArgsConstructor
        final class Label implements IndirectTarget {
            public final LabelNode labelCond;
            public final boolean cond;
        }
    }

    //endregion

    //region jump

    private JumpInsn prepareJumpInsn(Type type, TypedRelNode.Op op, InsnList insnList) {
        return switch (type) {
            case Bool, Int -> JumpInsn.ofBin(op, true);
            case Float -> {
                insnList.add(new InsnNode(switch (op) {
                    case Le, Lt -> Opcodes.DCMPG;
                    default -> Opcodes.DCMPL;
                }));
                yield JumpInsn.ofBin(op, false);
            }
            default -> throw new IllegalArgumentException();
        };
    }

    private enum JumpInsn {
        IEq,
        INe,
        IGe,
        IGt,
        ILe,
        ILt,
        Eq,
        Ne,
        Ge,
        Gt,
        Le,
        Lt;

        public static final JumpInsn BOOL = Ne;

        public static JumpInsn ofBin(TypedRelNode.Op op, boolean i) {
            return switch (op) {
                case Eq -> i ? IEq : Eq;
                case Ne -> i ? INe : Ne;
                case Ge -> i ? IGe : Ge;
                case Gt -> i ? IGt : Gt;
                case Le -> i ? ILe : Le;
                case Lt -> i ? ILt : Lt;
            };
        }

        public int jumpOpCode(boolean jumpToBranch) {
            return switch (this) {
                case IEq -> jumpToBranch ? Opcodes.IF_ICMPEQ : Opcodes.IF_ICMPNE;
                case INe -> jumpToBranch ? Opcodes.IF_ICMPNE : Opcodes.IF_ICMPEQ;
                case IGe -> jumpToBranch ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPLT;
                case IGt -> jumpToBranch ? Opcodes.IF_ICMPGT : Opcodes.IF_ICMPLE;
                case ILe -> jumpToBranch ? Opcodes.IF_ICMPLE : Opcodes.IF_ICMPGT;
                case ILt -> jumpToBranch ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPGE;
                case Eq -> jumpToBranch ? Opcodes.IFEQ : Opcodes.IFNE;
                case Ne -> jumpToBranch ? Opcodes.IFNE : Opcodes.IFEQ;
                case Ge -> jumpToBranch ? Opcodes.IFGE : Opcodes.IFLT;
                case Gt -> jumpToBranch ? Opcodes.IFGT : Opcodes.IFLE;
                case Le -> jumpToBranch ? Opcodes.IFLE : Opcodes.IFGT;
                case Lt -> jumpToBranch ? Opcodes.IFLT : Opcodes.IFGE;
            };
        }
    }

    //endregion

    //region local

    private int localVariableIndex;

    private int localAlloc(Type type) {
        val index = localVariableIndex;
        switch (type) {
            case Bool, Int, Vec2, Vec3, Vec4 -> localVariableIndex++;
            case Float -> localVariableIndex += 2;
            default -> throw new AssertionError();
        }
        return index;
    }

    private void localFree(Type type, int index) {
        switch (type) {
            case Bool, Int, Vec2, Vec3, Vec4 -> localVariableIndex--;
            case Float -> localVariableIndex -= 2;
            default -> throw new AssertionError();
        }
        if (localVariableIndex != index) {
            throw new IllegalStateException();
        }
    }

    private void genLocalStore(Type type, int index, InsnList insnList) {
        insnList.add(new VarInsnNode(switch (type) {
            case Bool, Int -> Opcodes.ISTORE;
            case Float -> Opcodes.DSTORE;
            case Vec2, Vec3, Vec4 -> Opcodes.ASTORE;
            default -> throw new AssertionError();
        }, index));
    }

    private void genLocalLoad(Type type, int index, InsnList insnList) {
        insnList.add(new VarInsnNode(switch (type) {
            case Bool, Int -> Opcodes.ILOAD;
            case Float -> Opcodes.DLOAD;
            case Vec2, Vec3, Vec4 -> Opcodes.ALOAD;
            default -> throw new AssertionError();
        }, index));
    }

    //endregion
}
