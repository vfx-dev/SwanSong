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
import lombok.val;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

/**
 * Actual logic copied from ShaderMod MIT sources
 *
 * @author Nathanael Lane
 */
public class HFNoiseTexture2D extends Texture2D {
    public HFNoiseTexture2D(String name, GLTexture texture, int internalFormat, int width, int height) {
        super(name, texture, internalFormat, width, height);
    }

    public static HFNoiseTexture2D create(int width, int height) {
        val texture = new GLTexture();
        texture.glGenTextures();
        texture.glBindTexture2D();

        setParams2D(GL11.GL_REPEAT, GL11.GL_LINEAR);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                          0,
                          GL11.GL_RGB,
                          width,
                          height,
                          0,
                          GL11.GL_RGB,
                          GL11.GL_UNSIGNED_BYTE,
                          genHFNoiseData(width, height));

        return new HFNoiseTexture2D("HFNoise", texture, GL11.GL_RGB, width, height);
    }

    public static ByteBuffer genHFNoiseData(int width, int height) {
        val image = genHFNoiseImage(width, height);
        val data = BufferUtils.createByteBuffer(image.length);
        data.put(image);
        data.flip();
        return data;
    }

    /*
     * Just a random value for each pixel to get maximum frequency
     */
    public static byte[] genHFNoiseImage(int width, int height) {
        val image = new byte[width * height * 3];

        var i = 0;
        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                for (var z = 1; z < 4; z++) {
                    image[i] = random(x, y, z);
                    i++;
                }
            }
        }
        return image;
    }

    public static byte random(int x, int y, int z) {
        val seed = (random(x) + random(y * 19)) * random(z * 23) - z;
        return (byte) (random(seed) % 128);
    }

    //from George Marsaglia's paper on XORshift PRNGs
    public static int random(int seed) {
        seed ^= (seed << 13);
        seed ^= (seed >> 17);
        seed ^= (seed << 5);
        return seed;
    }
}
