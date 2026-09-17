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

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

@RequiredArgsConstructor
public class BytecodeOptimizer {
    @RequiredArgsConstructor
    public static final class Flags {
        public final boolean optimizeJumps;
    }

    private final Flags flags;

    public void optimize(InsnList insnList) {
        boolean changedInPass;
        do {
            changedInPass = false;
            var curr = insnList.getFirst();
            while (curr != null) {
                AbstractInsnNode next = null;
                if (flags.optimizeJumps) {
                    next = removeUselessJump(curr, insnList);
                    if (next == null) {
                        next = mergeChainedJump(curr, insnList);
                    }
                    if (next == null) {
                        next = removeDoubleGoto(curr, insnList);
                    }
                }
                if (next != null) {
                    changedInPass = true;
                    curr = next;
                } else {
                    curr = curr.getNext();
                }
            }
        } while (changedInPass);
    }

    //region impl

    private AbstractInsnNode removeDoubleGoto(AbstractInsnNode insn, InsnList insnList) {
        if (!(insn instanceof JumpInsnNode jmp1) || jmp1.getOpcode() != Opcodes.GOTO) {
            return null;
        }
        val insn2 = insn.getNext();
        if (!(insn2 instanceof JumpInsnNode jmp2) || jmp2.getOpcode() != Opcodes.GOTO) {
            return null;
        }
        insnList.remove(insn2);
        return insn;
    }

    private AbstractInsnNode mergeChainedJump(AbstractInsnNode insn, InsnList insnList) {
        if (!(insn instanceof JumpInsnNode jmp1) || jmp1.getOpcode() != Opcodes.GOTO) {
            return null;
        }
        val insn2 = insn.getNext();
        if (!(insn2 instanceof LabelNode lbl)) {
            return null;
        }
        val insn3 = insn2.getNext();
        if (!(insn3 instanceof JumpInsnNode jmp2) || jmp2.getOpcode() != Opcodes.GOTO) {
            return null;
        }
        insnList.remove(lbl);
        insnList.remove(jmp2);
        replaceJumpTarget(lbl, jmp2.label, insnList);
        return insn;
    }

    private AbstractInsnNode removeUselessJump(AbstractInsnNode insn, InsnList insnList) {
        if (!(insn instanceof JumpInsnNode jmp)) {
            return null;
        }
        val next = insn.getNext();
        if (!(next instanceof LabelNode lbl)) {
            return null;
        }
        if (jmp.label != lbl) {
            return null;
        }
        insnList.remove(jmp);
        return next;
    }

    private void replaceJumpTarget(LabelNode from, LabelNode to, InsnList insnList) {
        val iter = insnList.iterator();
        while (iter.hasNext()) {
            val insn = iter.next();
            if (!(insn instanceof JumpInsnNode jump)) {
                continue;
            }
            if (jump.label == from) {
                jump.label = to;
            }
        }
    }

    //endregion
}
