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
import org.lwjgl.opengl.GL11;

public class GLTexture {
    public int glName;

    public void glGenTextures() {
        glName = GL11.glGenTextures();
    }

    // TODO: Rename to Delete Textures
    public void glDeleteShader() {
        GL11.glDeleteTextures(glName);
        glName = 0;
    }

    public void glBindTexture2D() {
        glBindTexture(GL11.GL_TEXTURE_2D);
    }

    public void glBindTexture(@MagicConstant(intValues = GL11.GL_TEXTURE_2D) int target) {
        GL11.glBindTexture(target, glName);
    }
}
