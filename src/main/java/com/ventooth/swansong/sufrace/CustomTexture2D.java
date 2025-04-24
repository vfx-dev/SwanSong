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

import com.ventooth.swansong.Share;
import com.ventooth.swansong.gl.GLTexture;
import com.ventooth.swansong.shader.preprocessor.FSProvider;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.nio.IntBuffer;

public final class CustomTexture2D extends Texture2D {
    private static final Logger log = Share.getLogger();

    private CustomTexture2D(String name, GLTexture texture, int internalFormat, int width, int height) {
        super(name, texture, internalFormat, width, height);
    }

    public static @Nullable CustomTexture2D load(FSProvider fs, String path) {
        final int width;
        final int height;
        final IntBuffer buf;
        try {
            val img = ImageIO.read(fs.get(path));
            width = img.getWidth();
            height = img.getHeight();

            val size = width * height;
            val pixels = new int[size];
            img.getRGB(0, 0, width, height, pixels, 0, width);

            buf = BufferUtils.createIntBuffer(width * height);
            buf.put(pixels);
            buf.flip();
        } catch (Exception e) {
            log.warn("Failed to load custom texture", e);
            return null;
        }

        TextureMeta meta;
        try {
            val str = IOUtils.toString(fs.get(path + ".mcmeta"));
            meta = TextureMeta.read(str);
            if (meta == null) {
                log.debug("Texture meta is null");
                meta = new TextureMeta(false, false);
            }
        } catch (Exception e) {
            meta = new TextureMeta(false, false);
            log.warn("Failed to load texture meta", e);
        }

        val texture = new GLTexture();
        texture.glGenTextures();
        texture.glBindTexture2D();

        setParams2D(meta.clamp(), meta.blur());

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                          0,
                          GL11.GL_RGBA,
                          width,
                          height,
                          0,
                          GL12.GL_BGRA,
                          GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                          buf);

        return new CustomTexture2D("custom:" + path, texture, GL11.GL_RGBA, width, height);
    }
}
