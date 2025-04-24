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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

// TODO: Better docs...
public class GLProgram {
    public int glName;

    public void glCreateProgram() {
        glName = GL20.glCreateProgram();
    }

    public void glDeleteProgram() {
        GL20.glDeleteProgram(glName);
        glName = 0;
    }

    public void glUseProgram() {
        GL20.glUseProgram(glName);
    }

    // region Uniforms

    /**
     * Can check if a uniform of a given name exists, otherwise -1 if it don't
     * <p>
     * Do NOT SPAM!!!
     */
    public int glGetUniformLocation(String name) {
        return GL20.glGetUniformLocation(glName, name);
    }

    /**
     * Same as the one before, but easier and maybe a microsecond slower. Just use it.
     */
    public String glGetActiveUniform(int index) {
        return glGetActiveUniform(index, glGetProgramActiveUniformMaxLength());
    }

    /**
     * Uhh, the name of the active uniform?
     */
    public String glGetActiveUniform(int index, int maxLength) {
        return GL20.glGetActiveUniform(glName, index, maxLength);
    }

    /**
     * The type of the uniform, so you know what to expect
     */
    public int glGetActiveUniformType(int index) {
        return GL20.glGetActiveUniformType(glName, index);
    }

    /**
     * What size (array-wise) is this uniform? 99% of the time? this will say 1 (ONE)
     */
    public int glGetActiveUniformSize(int index) {
        return GL20.glGetActiveUniformSize(glName, index);
    }

    /**
     * How many uh, uniforms actually exist and are used?
     */
    public int glGetProgramActiveUniforms() {
        return GL20.glGetProgrami(glName, GL20.GL_ACTIVE_UNIFORMS);
    }

    /**
     * Out of all active uniforms, which has the fattest length?
     */
    public int glGetProgramActiveUniformMaxLength() {
        return GL20.glGetProgrami(glName, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH);
    }
    // endregion

    public void glBindAttribLocation(int index, String name) {
        GL20.glBindAttribLocation(glName, index, name);
    }

    // region Linking

    public void glAttachShader(GLShader shader) {
        glAttachShader(shader.glName);
    }

    public void glAttachShader(int shader) {
        GL20.glAttachShader(glName, shader);
    }

    public void glLinkProgram() {
        GL20.glLinkProgram(glName);
    }

    public boolean glGetProgramLinkStatus() {
        return GL20.glGetProgrami(glName, GL20.GL_LINK_STATUS) == GL11.GL_TRUE;
    }

    // endregion

    // region Validation

    public void glValidateProgram() {
        GL20.glValidateProgram(glName);
    }

    public boolean glGetProgramValidateStatus() {
        return GL20.glGetProgrami(glName, GL20.GL_VALIDATE_STATUS) == GL11.GL_TRUE;
    }

    // endregion

    // region Info Log

    public String glGetProgramInfoLog() {
        val length = glGetProgramInfoLogLength();
        if (length > 0) {
            return glGetProgramInfoLog(length);
        } else {
            return "";
        }
    }

    public int glGetProgramInfoLogLength() {
        return GL20.glGetProgrami(glName, GL20.GL_INFO_LOG_LENGTH);
    }

    public String glGetProgramInfoLog(int maxLength) {
        return GL20.glGetProgramInfoLog(glName, maxLength);
    }

    // endregion
}
