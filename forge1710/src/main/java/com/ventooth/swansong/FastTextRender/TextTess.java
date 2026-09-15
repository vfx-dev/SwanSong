/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.FastTextRender;

import lombok.val;
import org.lwjgl.BufferUtils;
import org.lwjgl.MemoryUtil;
import org.lwjgl.opengl.GL11;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TextTess {
    private static final int nativeBufferSize = 0x200000;

    private static final Unsafe UNSAFE;

    static {
        try {
            val theField = Unsafe.class.getDeclaredField("theUnsafe");
            theField.setAccessible(true);
            UNSAFE = (Unsafe) theField.get(null);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final ByteBuffer buf = BufferUtils.createByteBuffer(nativeBufferSize * 4);
    private final long address = MemoryUtil.getAddress(buf);
    private final boolean hasTexture;

    private int rawBufferSize = 0;
    private int vertexCount;
    private int maxVertices;
    private int color;
    private long bufferIndex;
    private boolean isDrawing;

    public TextTess(boolean hasTexture) {
        this.hasTexture = hasTexture;
        long address = this.address;
    }

    /**
     * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
     */
    public void draw() {
        if (!this.isDrawing) {
            throw new IllegalStateException("Not tesselating!");
        } else {
            this.isDrawing = false;

            int stride = hasTexture ? 6 : 4;
            int strideBytes = stride * 4;

            if (this.hasTexture) {
                buf.position(16);
                GL11.glTexCoordPointer(2, GL11.GL_FLOAT, strideBytes, buf);
                GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            }

            buf.position(12);
            GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, strideBytes, buf);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

            buf.position(0);
            GL11.glVertexPointer(3, GL11.GL_FLOAT, strideBytes, buf);
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glDrawArrays(GL11.GL_QUADS, 0, vertexCount);
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

            if (this.hasTexture) {
                GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            }

            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);

            this.reset();
        }
    }

    /**
     * Clears the tessellator state in preparation for new drawing.
     */
    private void reset() {
        this.vertexCount = 0;
        this.bufferIndex = address;
    }

    /**
     * Sets draw mode in the tessellator to draw quads.
     */
    public void startDrawingQuads() {
        if (this.isDrawing) {
            throw new IllegalStateException("Already tesselating text!");
        } else {
            this.isDrawing = true;
            this.reset();
            this.vertexCount = 0;
            int maxQuads = (nativeBufferSize / ((hasTexture ? 6 : 4) * 4)) / 4;
            this.maxVertices = maxQuads * 4;
        }
    }

    public void setColorRGBA_F(float r, float g, float b, float a) {
        this.setColorRGBA((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F), (int) (a * 255.0F));
    }

    public void setColorRGBA(int red, int green, int blue, int alpha) {
        if (red > 255) {
            red = 255;
        }

        if (green > 255) {
            green = 255;
        }

        if (blue > 255) {
            blue = 255;
        }

        if (alpha > 255) {
            alpha = 255;
        }

        if (red < 0) {
            red = 0;
        }

        if (green < 0) {
            green = 0;
        }

        if (blue < 0) {
            blue = 0;
        }

        if (alpha < 0) {
            alpha = 0;
        }

        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            this.color = alpha << 24 | blue << 16 | green << 8 | red;
        } else {
            this.color = red << 24 | green << 16 | blue << 8 | alpha;
        }
    }

    /**
     * Adds a vertex specifying both x,y,z and the texture u,v for it.
     */
    public void addVertexWithUV(float x, float y, float z, float u, float v) {
        this.addVertex(x, y, z);
        UNSAFE.putFloat(bufferIndex, u);
        UNSAFE.putFloat(bufferIndex + 4, v);
        this.bufferIndex += 8;
    }

    /**
     * Adds a vertex with the specified x,y,z to the current draw call. It will trigger a draw() if the buffer gets
     * full.
     */
    public void addVertex(float x, float y, float z) {
        if (this.vertexCount >= maxVertices) {
            draw();
            startDrawingQuads();
        }

        UNSAFE.putFloat(bufferIndex, x);
        UNSAFE.putFloat(bufferIndex + 4, y);
        UNSAFE.putFloat(bufferIndex + 8, z);
        UNSAFE.putInt(bufferIndex + 12, color);

        this.bufferIndex += 16;
        ++this.vertexCount;
    }
}