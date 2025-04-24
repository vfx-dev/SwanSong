/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.gl;

import org.intellij.lang.annotations.MagicConstant;
import org.lwjgl.opengl.GL30;

public class GLFramebuffer {
    public int glName;

    public void glGenFramebuffers() {
        glName = GL30.glGenFramebuffers();
    }

    public void glDeleteFramebuffers() {
        GL30.glDeleteFramebuffers(glName);
        glName = 0;
    }

    public void glBindFramebufferDraw() {
        glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER);
    }

    public void glBindFramebufferRead() {
        glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER);
    }

    public void glBindFramebuffer() {
        glBindFramebuffer(GL30.GL_FRAMEBUFFER);
    }

    public void glBindFramebuffer(
            @MagicConstant(intValues = {GL30.GL_FRAMEBUFFER, GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_READ_FRAMEBUFFER})
            int target) {
        GL30.glBindFramebuffer(target, glName);
    }
}
