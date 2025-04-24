/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.asm.transformers;

import com.falsepattern.lib.asm.ASMUtil;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import com.ventooth.swansong.Tags;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * This runs AFTER the mixin transformer or everything else.
 * It WILL break inject HEAD/RETURN mixins!
 * <p>
 * <code>int draw()</code>
 * <p>
 * <code>void addVertex(double, double, double)</code>
 * <p>
 * <code>TessellatorVertexState getVertexState(float, float, float)</code>  -- IF <code>rawBufferIndex < 1</code>
 */
@SuppressWarnings("UnstableApiUsage")
public class TessellatorShaderTransformer implements TurboClassTransformer {
    private static final String TESS_CLASS = "net.minecraft.client.renderer.Tessellator";
    private static final String TESS_INTERNAL = "net/minecraft/client/renderer/Tessellator";
    private static final String SHADER_TESS_INTERNAL = "com/ventooth/swansong/tessellator/ShaderTess";
    private static final String SHADER_TESS_DESC = "L" + SHADER_TESS_INTERNAL + ";";
    private static final String TVS_INTERNAL = "net/minecraft/client/shader/TesselatorVertexState";
    private static final String TVS_DESC = "L" + TVS_INTERNAL + ";";

    @Override
    public String owner() {
        return Tags.MOD_ID;
    }

    @Override
    public String name() {
        return "TessellatorShaderTransformer";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        return TESS_CLASS.equals(className);
    }

    @SneakyThrows
    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null) {
            return false;
        }
        injectDrawHook(ASMUtil.findMethodFromMCP(cn, "draw", "()I", false));
        injectAddVertexHook(ASMUtil.findMethodFromMCP(cn, "addVertex", "(DDD)V", false));
        val rawBufferIndex = ASMUtil.findFieldFromMCP(cn, "rawBufferIndex", false);
        MethodNode foamfixMethod = null;
        for (val method : cn.methods) {
            if ("getVertexState_foamfix_old".equals(method.name)) {
                foamfixMethod = method;
                break;
            }
        }
        if (foamfixMethod != null) {
            injectGetVertexState(foamfixMethod, rawBufferIndex);
        } else {
            injectGetVertexState(ASMUtil.findMethodFromMCP(cn, "getVertexState", "(FFF)" + TVS_DESC, false),
                                 rawBufferIndex);
        }
        return true;
    }

    private void injectDrawHook(MethodNode methodNode) {
        val instructions = new InsnList();
        getShaderTess(instructions);
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, SHADER_TESS_INTERNAL, "draw", "()I", false));
        instructions.add(new InsnNode(Opcodes.IRETURN));
        instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        methodNode.instructions.insert(instructions);
    }

    private void injectAddVertexHook(MethodNode methodNode) {
        val instructions = new InsnList();
        getShaderTess(instructions);
        methodNode.maxStack = Math.max(methodNode.maxStack, 7);
        instructions.add(new VarInsnNode(Opcodes.DLOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.DLOAD, 3));
        instructions.add(new VarInsnNode(Opcodes.DLOAD, 5));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, SHADER_TESS_INTERNAL, "addVertex", "(DDD)V", false));
        instructions.add(new InsnNode(Opcodes.RETURN));
        instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        methodNode.instructions.insert(instructions);
    }

    private void injectGetVertexState(MethodNode methodNode, FieldNode rawBufferIndex) {
        val instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, TESS_INTERNAL, rawBufferIndex.name, rawBufferIndex.desc));
        instructions.add(new InsnNode(Opcodes.ICONST_1));
        val endLabel = new LabelNode();
        instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, endLabel));
        instructions.add(new InsnNode(Opcodes.ACONST_NULL));
        instructions.add(new InsnNode(Opcodes.ARETURN));
        instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        instructions.add(endLabel);
        methodNode.instructions.insert(instructions);
    }

    private void getShaderTess(InsnList instructions) {
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, TESS_INTERNAL, "swansong$shaderTess", SHADER_TESS_DESC));
    }
}
