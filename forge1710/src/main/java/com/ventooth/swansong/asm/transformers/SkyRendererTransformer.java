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

import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import com.ventooth.swansong.Tags;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class SkyRendererTransformer implements TurboClassTransformer {
    @Override
    public String owner() {
        return Tags.MOD_ID;
    }

    @Override
    public String name() {
        return "SkyRendererTransformer";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        return !className.startsWith(Tags.ROOT_PKG);
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val node = classNode.getNode();
        if (node == null) {
            return false;
        }
        if (!node.superName.equals("net/minecraftforge/client/IRenderHandler")) {
            return false;
        }
        boolean modified = false;
        for (val method : node.methods) {
            var it = method.instructions.iterator();
            while (it.hasNext()) {
                if (!(it.next() instanceof MethodInsnNode inst)) {
                    continue;
                }

                if (inst.getOpcode() != Opcodes.INVOKESTATIC) {
                    continue;
                }

                if (!("org/lwjgl/opengl/GL11".equals(inst.owner) || "org/lwjglx/opengl/GL11".equals(inst.owner))) {
                    continue;
                }

                if (!"(I)V".equals(inst.desc)) {
                    continue;
                }

                if (patchTexture(inst)) {
                    modified = true;
                } else if (patchCallList(inst)) {
                    modified = true;
                }
            }
        }
        return modified;
    }

    private static boolean patchTexture(MethodInsnNode inst) {
        if (!"glEnable".equals(inst.name) && !"glDisable".equals(inst.name)) {
            return false;
        }

        final int cap;
        if (inst.getPrevious() instanceof IntInsnNode capInst && capInst.getOpcode() == Opcodes.SIPUSH) {
            cap = capInst.operand;
        } else {
            return false;
        }

        if (cap != GL11.GL_TEXTURE_2D) {
            return false;
        }
        inst.owner = "com/ventooth/swansong/ASMHooks";
        return true;
    }

    private static boolean patchCallList(MethodInsnNode inst) {
        if (!"glCallList".equals(inst.name)) {
            return false;
        }

        inst.owner = "com/ventooth/swansong/ASMHooks";
        return true;
    }
}
