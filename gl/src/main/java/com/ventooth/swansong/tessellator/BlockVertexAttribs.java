/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.tessellator;

import com.ventooth.swansong.shader.ShaderEntityData;
import com.ventooth.swansong.todo.tess.DanglingWiresTess;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public final class BlockVertexAttribs {
    public static final int INT_STRIDE = 13;
    public static final int STRIDE = INT_STRIDE * 4;

    private static final int POSITION = 0;
    private static final int TEXTURE = 4;
    private static final int NORMAL = 7;
    private static final int ENTITY = 8;
    private static final int MID_TEXTURE = 10;
    private static final int TANGENT = 12;

    private static final ThreadLocal<ShaderVert[]> QUAD = ThreadLocal.withInitial(() -> new ShaderVert[]{new ShaderVert(),
                                                                                                        new ShaderVert(),
                                                                                                        new ShaderVert(),
                                                                                                        new ShaderVert()});

    private BlockVertexAttribs() {
    }

    public static void endVertex(IntBuffer ints, int vertexCount, boolean quads) {
        val packed = ShaderEntityData.get()
                                     .getPackedEntityData();
        val base = (vertexCount - 1) * INT_STRIDE;
        ints.put(base + ENTITY, ShaderEntityData.unpackEntityData(packed));
        ints.put(base + ENTITY + 1, ShaderEntityData.unpackEntityData2(packed));
        if (quads && (vertexCount & 3) == 0) {
            completeQuad(ints, (vertexCount - 4) * INT_STRIDE);
        }
    }

    private static void completeQuad(IntBuffer ints, int first) {
        val quad = QUAD.get();
        for (int i = 0; i < 4; i++) {
            val vert = quad[i];
            val base = first + i * INT_STRIDE;
            vert.positionX = Float.intBitsToFloat(ints.get(base + POSITION));
            vert.positionY = Float.intBitsToFloat(ints.get(base + POSITION + 1));
            vert.positionZ = Float.intBitsToFloat(ints.get(base + POSITION + 2));
            vert.textureU = Float.intBitsToFloat(ints.get(base + TEXTURE));
            vert.textureV = Float.intBitsToFloat(ints.get(base + TEXTURE + 1));
        }
        ShaderVert.completeQuad(quad[0], quad[1], quad[2], quad[3]);
        for (int i = 0; i < 4; i++) {
            val vert = quad[i];
            val base = first + i * INT_STRIDE;
            ints.put(base + NORMAL, packBytes(vert.normalX, vert.normalY, vert.normalZ, 0));
            ints.put(base + MID_TEXTURE, Float.floatToRawIntBits(vert.midTextureU));
            ints.put(base + MID_TEXTURE + 1, Float.floatToRawIntBits(vert.midTextureV));
            ints.put(base + TANGENT, packBytes(vert.tangentX, vert.tangentY, vert.tangentZ, vert.tangentW));
        }
    }

    private static int packBytes(float x, float y, float z, float w) {
        return ((byte) (x * 127) & 0xFF) |
               ((byte) (y * 127) & 0xFF) << 8 |
               ((byte) (z * 127) & 0xFF) << 16 |
               ((byte) (w * 127) & 0xFF) << 24;
    }

    public static void enableVbo() {
        GL11.glNormalPointer(GL11.GL_BYTE, STRIDE, NORMAL * 4L);
        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        GL20.glVertexAttribPointer(DanglingWiresTess.entityAttrib, 3, GL11.GL_SHORT, false, STRIDE, ENTITY * 4L);
        GL20.glVertexAttribPointer(DanglingWiresTess.midTexCoordAttrib, 2, GL11.GL_FLOAT, false, STRIDE, MID_TEXTURE * 4L);
        GL20.glVertexAttribPointer(DanglingWiresTess.tangentAttrib, 4, GL11.GL_BYTE, true, STRIDE, TANGENT * 4L);
        enableAttribs();
    }

    public static void enableClient(ByteBuffer buffer) {
        GL11.glNormalPointer(GL11.GL_BYTE, STRIDE, buffer.position(NORMAL * 4));
        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        GL20.glVertexAttribPointer(DanglingWiresTess.entityAttrib, 3, GL11.GL_SHORT, false, STRIDE, buffer.position(ENTITY * 4));
        GL20.glVertexAttribPointer(DanglingWiresTess.midTexCoordAttrib, 2, GL11.GL_FLOAT, false, STRIDE, buffer.position(MID_TEXTURE * 4));
        GL20.glVertexAttribPointer(DanglingWiresTess.tangentAttrib, 4, GL11.GL_BYTE, true, STRIDE, buffer.position(TANGENT * 4));
        buffer.position(0);
        enableAttribs();
    }

    private static void enableAttribs() {
        GL20.glEnableVertexAttribArray(DanglingWiresTess.entityAttrib);
        GL20.glEnableVertexAttribArray(DanglingWiresTess.midTexCoordAttrib);
        GL20.glEnableVertexAttribArray(DanglingWiresTess.tangentAttrib);
    }

    public static void disable() {
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GL20.glDisableVertexAttribArray(DanglingWiresTess.entityAttrib);
        GL20.glDisableVertexAttribArray(DanglingWiresTess.midTexCoordAttrib);
        GL20.glDisableVertexAttribArray(DanglingWiresTess.tangentAttrib);
        GL20.glVertexAttrib4f(DanglingWiresTess.entityAttrib, 0, 0, 0, 1);
        GL20.glVertexAttrib4f(DanglingWiresTess.midTexCoordAttrib, 0, 0, 0, 1);
        GL20.glVertexAttrib4f(DanglingWiresTess.tangentAttrib, 0, 0, 0, 1);
    }
}
