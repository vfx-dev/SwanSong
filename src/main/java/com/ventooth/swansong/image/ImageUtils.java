/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.image;

import com.ventooth.swansong.Share;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.shader.Framebuffer;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageUtils {
    public static final Logger log = Share.getLogger();

    private static IntBuffer tempBuf = BufferUtils.createIntBuffer(128 * 128);

    public static @Nullable RawImage downloadGLTextureAsBGRA(Framebuffer frameBuffer) {
        return downloadGLTextureAsBGRA(frameBuffer.framebufferTexture);
    }

    public static @Nullable RawImage downloadGLTextureAsBGRA(int tex) {
        if (tex == 0) {
            log.warn("Tried to download zero texture");
            return null;
        }
        if (!GL11.glIsTexture(tex)) {
            log.warn("Cannot download uninitialized texture: id={}", tex);
            return null;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);

            val width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            val height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

            val bufSize = width * height;
            if (bufSize <= 0) {
                log.debug("Failed to download texture: id={}, invalid size=[{}x{}]", tex, width, height);
                return null;
            }

            val oldBufSize = tempBuf.capacity();
            if (oldBufSize < bufSize) {
                tempBuf = BufferUtils.createIntBuffer(bufSize);
                log.debug("Resized temp texture buffer: ({}) -> ({})", oldBufSize, bufSize);
            }
            tempBuf.clear();

            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, tempBuf);

            val data = new int[bufSize];
            tempBuf.get(data);
            log.debug("Downloaded texture: id={} size=[{}x{}]", tex, width, height);
            return new RawImage(data, width, height);
        } finally {
            GL11.glPopAttrib();
        }
    }

    public static List<RawImage> downloadGLTextureLevelsAsBGRA(int tex) {
        val imgs = new ArrayList<RawImage>();

        if (tex == 0) {
            log.warn("Tried to download zero texture");
            return imgs;
        }
        if (!GL11.glIsTexture(tex)) {
            log.warn("Cannot download uninitialized texture: id={}", tex);
            return imgs;
        }

        for (var level = 0; level < 8; level++) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            try {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);

                val width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, level, GL11.GL_TEXTURE_WIDTH);
                val height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, level, GL11.GL_TEXTURE_HEIGHT);

                val bufSize = width * height;
                if (bufSize <= 0) {
                    if (level == 0) {
                        log.debug("Failed to download texture: id={}, invalid size=[{}x{}]", tex, width, height);
                    }
                    break;
                }

                val oldBufSize = tempBuf.capacity();
                if (oldBufSize < bufSize) {
                    tempBuf = BufferUtils.createIntBuffer(bufSize);
                    log.debug("Resized temp texture buffer: ({}) -> ({})", oldBufSize, bufSize);
                }
                tempBuf.clear();

                GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, level, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, tempBuf);

                val data = new int[bufSize];
                tempBuf.get(data);
                log.debug("Downloaded texture: id={} level={} size=[{}x{}]", tex, level, width, height);
                imgs.add(new RawImage(data, width, height));
            } finally {
                GL11.glPopAttrib();
            }
        }

        return imgs;
    }
}
