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

import lombok.val;
import org.intellij.lang.annotations.MagicConstant;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;

public class GLShader {
    public int glName;

    public void glCreateShader(@MagicConstant(intValues = {GL20.GL_VERTEX_SHADER, GL20.GL_FRAGMENT_SHADER}) int type) {
        glName = GL20.glCreateShader(type);
    }

    public void glDeleteShader() {
        GL20.glDeleteShader(glName);
        glName = 0;
    }

    /**
     * MUST BE NULL TERMINATED
     */
    public void glShaderSource(ByteBuffer string) {
        ShaderHax.glShaderSource(glName, string);
    }

    public void glCompileShader() {
        GL20.glCompileShader(glName);
    }

    public boolean glGetShaderCompileStatus() {
        return GL20.glGetShaderi(glName, GL20.GL_COMPILE_STATUS) == GL11.GL_TRUE;
    }

    public String glGetShaderInfoLog() {
        val length = glGetShaderInfoLogLength();
        if (length > 0) {
            return glGetShaderInfoLog(length);
        } else {
            return "";
        }
    }

    public int glGetShaderInfoLogLength() {
        return GL20.glGetShaderi(glName, GL20.GL_INFO_LOG_LENGTH);
    }

    public String glGetShaderInfoLog(int maxLength) {
        return GL20.glGetShaderInfoLog(glName, maxLength);
    }
}
