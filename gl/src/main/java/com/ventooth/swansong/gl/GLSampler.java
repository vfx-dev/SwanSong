package com.ventooth.swansong.gl;

import org.intellij.lang.annotations.MagicConstant;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;

public class GLSampler {
    public int glName;

    public void glGenSamplers() {
        glName = GL33.glGenSamplers();
    }

    public void glDeleteSampler() {
        GL33.glDeleteSamplers(glName);
        glName = 0;
    }

    public void glBindSampler(int unit) {
        GL33.glBindSampler(unit, glName);
    }

    public void glSamplerParameter(@MagicConstant(intValues = {GL11.GL_TEXTURE_WRAP_S,
                                                               GL11.GL_TEXTURE_WRAP_T,
                                                               GL11.GL_TEXTURE_MIN_FILTER,
                                                               GL11.GL_TEXTURE_MAG_FILTER}) int pname,
                                   @MagicConstant(intValues = {GL11.GL_CLAMP, GL11.GL_NEAREST}) int param) {
        GL33.glSamplerParameteri(glName, pname, param);
    }
}
