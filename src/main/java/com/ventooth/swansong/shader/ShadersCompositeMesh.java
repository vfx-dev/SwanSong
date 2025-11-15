/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader;

import com.ventooth.swansong.debug.DebugMarker;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadersCompositeMesh {
    private static boolean isInitialized = false;

    private static int vao = 0;
    private static int vbo = 0;

    public static void drawWithColor() {
        draw(Mask.Color);
    }

    public static void drawWithAnaglyphField(int field) {
        if (field == 0) {
            draw(Mask.AnaglyphCyan);
        } else {
            draw(Mask.AnaglyphRed);
        }
    }

    public static void drawWithDepth(boolean combine) {
        draw(combine ? Mask.DepthCombine : Mask.Depth);
    }

    public static void draw(Mask mask) {
        if (!isInitialized) {
            throw new AssertionError();
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, 1.0, 0.0, 1.0, 0.0, 1.0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        if (mask == Mask.DepthCombine) {
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        } else {
            GL11.glDepthFunc(GL11.GL_ALWAYS);
        }

        switch (mask) {
            case Color -> {
                GL11.glDepthMask(false);
                GL11.glColorMask(true, true, true, true);
            }
            case AnaglyphRed -> {
                GL11.glDepthMask(false);
                GL11.glColorMask(true, false, false, false);
            }
            case AnaglyphCyan -> {
                GL11.glDepthMask(false);
                GL11.glColorMask(false, true, true, false);
            }
            case Depth, DepthCombine -> {
                GL11.glDepthMask(true);
                GL11.glColorMask(false, false, false, false);
            }
            case Both -> {
                GL11.glDepthMask(true);
                GL11.glColorMask(true, true, true, true);
            }
        }

        {
            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
            GL30.glBindVertexArray(GL11.GL_ZERO);
        }

        if (DebugMarker.isEnabled()) {
            DebugMarker.GENERIC.insert("Composite drawn with mask: " + mask);
        }

        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();

        GL11.glPopAttrib();
    }

    public static void init() {
        if (isInitialized) {
            return;
        }

        vbo = GL15.glGenBuffers();
        vao = GL30.glGenVertexArrays();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        // Arranged as XY-UV, used as a triangle-strip based quad.
        val mesh = BufferUtils.createFloatBuffer(16);

        //@formatter:off
        // Bottom-Left
        mesh.put(0F).put(0F)
            .put(0F).put(0F);
        // Bottom-Right
        mesh.put(1F).put(0F)
            .put(1F).put(0F);
        // Top-Left
        mesh.put(0F).put(1F)
            .put(0F).put(1F);
        // Top-Right
        mesh.put(1F).put(1F)
            .put(1F).put(1F);
        mesh.flip();
        //@formatter:on

        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mesh, GL15.GL_STATIC_DRAW);

        val xySizeFloats = 2;
        val uvSizeFloats = 2;

        val xySizeBytes = xySizeFloats * Float.BYTES;
        val uvSizeBytes = uvSizeFloats * Float.BYTES;

        val xyOffsetBytes = 0;
        val uvOffsetBytes = xySizeBytes;

        val strideBytes = xySizeBytes + uvSizeBytes;

        GL11.glVertexPointer(xySizeFloats, GL11.GL_FLOAT, strideBytes, xyOffsetBytes);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

        GL11.glTexCoordPointer(uvSizeFloats, GL11.GL_FLOAT, strideBytes, uvOffsetBytes);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        isInitialized = true;
    }

    public static void deinit() {
        if (!isInitialized) {
            return;
        }

        GL30.glDeleteVertexArrays(vao);
        vao = 0;
        GL15.glDeleteBuffers(vbo);
        vbo = 0;

        isInitialized = false;
    }

    public enum Mask {
        Color,
        AnaglyphRed,
        AnaglyphCyan,
        Depth,
        DepthCombine,
        Both
    }
}
