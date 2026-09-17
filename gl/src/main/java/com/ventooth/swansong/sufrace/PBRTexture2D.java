/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.sufrace;

import com.ventooth.swansong.gl.GLTexture;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;


import java.nio.IntBuffer;
import java.util.Objects;

@Getter
@Accessors(fluent = true,
           chain = false)
public final class PBRTexture2D extends Texture2D {
    private final String loc;

    public PBRTexture2D(String loc, GLTexture texture, int width, int height) {
        super(loc, texture, GL11.GL_RGBA, width, height);
        this.loc = loc;
    }

    public static PBRTexture2D ofWrapped(String loc, int width, int height, int glName) {
        val texture = new GLTexture();
        texture.glName = glName;
        return new PBRTexture2D(loc, texture, width, height);
    }

    /**
     * @apiNote Texture remains bound after call
     */
    public static PBRTexture2D ofIntBuffer(String loc,
                                           int width,
                                           int height,
                                           boolean clamp,
                                           boolean blur,
                                           IntBuffer buf) {
        val texture = new GLTexture();
        texture.glGenTextures();
        texture.glBindTexture2D();

        setParams2D(clamp, blur);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                          0,
                          GL11.GL_RGBA,
                          width,
                          height,
                          0,
                          GL12.GL_BGRA,
                          GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                          buf);

        return new PBRTexture2D(loc, texture, width, height);
    }

    public record Bundle(String base,
                         @Nullable PBRTexture2D norm,
                         @Nullable PBRTexture2D spec) {
    }
}