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

import com.ventooth.swansong.Share;
import com.ventooth.swansong.mixin.mixins.client.TessellatorAccessor;
import com.ventooth.swansong.platform.ArchaicShaderEngine;
import com.ventooth.swansong.shader.ShaderEntityData;
import com.ventooth.swansong.todo.tess.DanglingWiresTess;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

import java.util.Arrays;

import static com.ventooth.swansong.mixin.mixins.client.TessellatorAccessor.getTessByteBuf;
import static com.ventooth.swansong.mixin.mixins.client.TessellatorAccessor.getTessFloatBuf;
import static com.ventooth.swansong.mixin.mixins.client.TessellatorAccessor.getTessIntBuf;
import static com.ventooth.swansong.mixin.mixins.client.TessellatorAccessor.getTessShortBuf;

public class ShaderTess {
    public static final int MIN_BUFFER_SIZE_INTS = 0x10000;
    public static final int MAX_BUFFER_SIZE_INTS = 0x1000000;
    public static final int TRIANGLE_VERTEX_COUNT = 3;
    public static final int QUAD_VERTEX_COUNT = 4;

    protected final Tessellator tess;
    protected final ShaderEntityData entityData;

    protected final ShaderVert vertA;
    protected final ShaderVert vertB;
    protected final ShaderVert vertC;
    protected final ShaderVert vertD;

    //FalseTweaks mixin lands here
    public static int vertexStrideInt() {
        return ArchaicShaderEngine.isInitialized() ? 20 : 8;
    }

    public static int vertexStrideByte() {
        return vertexStrideInt() * Integer.BYTES;
    }

    public ShaderTess(Tessellator tess) {
        this.tess = tess;
        this.entityData = ShaderEntityData.get();

        this.vertA = new ShaderVert();
        this.vertB = new ShaderVert();
        this.vertC = new ShaderVert();
        this.vertD = new ShaderVert();
    }

    public void addVertex(double posX, double posY, double posZ) {
        val fPosX = (float) (posX + tess.xOffset);
        val fPosY = (float) (posY + tess.yOffset);
        val fPosZ = (float) (posZ + tess.zOffset);
        addVertex(fPosX, fPosY, fPosZ);
    }

    public int draw() {
        val iStride = vertexStrideInt();
        val bStride = vertexStrideByte();
        if (!tess.isDrawing) {
            throw new IllegalStateException("Not tesselating!");
        }

        //        if (GLDebugGroups.isEnabled()) {
        //            GLDebugGroups.push(GLDebugGroups.TESS_DRAW);
        //        }

        tess.isDrawing = false;
        if (tess.drawMode == GL11.GL_QUADS && tess.vertexCount % 4 != 0) {
            Share.log.warn("Bad vertex count for Quads: {}", tess.vertexCount);
        }
        if (tess.drawMode == GL11.GL_TRIANGLES && tess.vertexCount % 3 != 0) {
            Share.log.warn("Bad vertex count for Triangles: {}", tess.vertexCount);
        }

        var voffset = 0;
        val realDrawMode = tess.drawMode;
        while (voffset < tess.vertexCount) {
            int vcount = Math.min(tess.vertexCount - voffset, getTessByteBuf().capacity() / bStride);
            if (realDrawMode == GL11.GL_QUADS) {
                vcount = vcount / 4 * 4;
            }

            getTessFloatBuf().clear();
            getTessShortBuf().clear();
            getTessIntBuf().clear();
            getTessIntBuf().put(tess.rawBuffer, voffset * iStride, vcount * iStride);
            getTessByteBuf().position(0);
            getTessByteBuf().limit(vcount * bStride);
            voffset += vcount;
            if (tess.hasTexture) {
                getTessFloatBuf().position(3);
                GL11.glTexCoordPointer(2, bStride, getTessFloatBuf());
                GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            }

            if (tess.hasBrightness) {
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                getTessShortBuf().position(12);
                GL11.glTexCoordPointer(2, bStride, getTessShortBuf());
                GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            }

            if (tess.hasColor) {
                getTessByteBuf().position(20);
                GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, bStride, getTessByteBuf());
                GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
            }

            if (tess.hasNormals) {
                getTessFloatBuf().position(9);
                GL11.glNormalPointer(bStride, getTessFloatBuf());
                GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
            }

            getTessFloatBuf().position(0);
            GL11.glVertexPointer(3, bStride, getTessFloatBuf());
            preDrawArray();
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glDrawArrays(realDrawMode, 0, vcount);
        }

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        postDrawArray();
        if (tess.hasTexture) {
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }

        if (tess.hasBrightness) {
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        }

        if (tess.hasColor) {
            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        }

        if (tess.hasNormals) {
            GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        }

        int n = tess.rawBufferIndex * 4;
        tess.reset();

        //        if (GLDebugGroups.isEnabled()) {
        //            GLDebugGroups.pop(GLDebugGroups.TESS_DRAW);
        //        }
        return n;
    }

    protected void preDrawArray() {
        val bStride = vertexStrideByte();
        if (DanglingWiresTess.useEntityAttrib) {
            getTessShortBuf().position(14);
            GL20.glVertexAttribPointer(DanglingWiresTess.entityAttrib, 3, false, false, bStride, getTessShortBuf());
            GL20.glEnableVertexAttribArray(DanglingWiresTess.entityAttrib);
        }

        if (tess.hasTexture) {
            if (DanglingWiresTess.useTangentAttrib) {
                getTessFloatBuf().position(12);
                GL20.glVertexAttribPointer(DanglingWiresTess.tangentAttrib, 4, false, bStride, getTessFloatBuf());
                GL20.glEnableVertexAttribArray(DanglingWiresTess.tangentAttrib);
            }

            if (DanglingWiresTess.useMidTexCoordAttrib) {
                getTessFloatBuf().position(16);
                GL20.glVertexAttribPointer(DanglingWiresTess.midTexCoordAttrib, 2, false, bStride, getTessFloatBuf());
                GL20.glEnableVertexAttribArray(DanglingWiresTess.midTexCoordAttrib);
            }

            if (DanglingWiresTess.useMultiTexCoord3Attrib) {
                getTessFloatBuf().position(16);
                GL13.glClientActiveTexture(GL13.GL_TEXTURE3);
                GL11.glTexCoordPointer(2, bStride, getTessFloatBuf());
                GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
            }

            if (DanglingWiresTess.useEdgeTexCoordAttrib) {
                getTessFloatBuf().position(18);
                GL20.glVertexAttribPointer(DanglingWiresTess.edgeTexCoordAttrib, 2, false, bStride, getTessFloatBuf());
                GL20.glEnableVertexAttribArray(DanglingWiresTess.edgeTexCoordAttrib);
            }
        }
    }

    protected void postDrawArray() {
        if (DanglingWiresTess.useEntityAttrib) {
            GL20.glDisableVertexAttribArray(DanglingWiresTess.entityAttrib);
        }

        if (tess.hasTexture) {
            if (DanglingWiresTess.useTangentAttrib) {
                GL20.glDisableVertexAttribArray(DanglingWiresTess.tangentAttrib);
            }

            if (DanglingWiresTess.useMidTexCoordAttrib) {
                GL20.glDisableVertexAttribArray(DanglingWiresTess.midTexCoordAttrib);
            }

            if (DanglingWiresTess.useMultiTexCoord3Attrib) {
                GL13.glClientActiveTexture(GL13.GL_TEXTURE3);
                GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
            }

            if (DanglingWiresTess.useEdgeTexCoordAttrib) {
                GL20.glDisableVertexAttribArray(DanglingWiresTess.edgeTexCoordAttrib);
            }
        }
    }

    protected void addVertex(float posX, float posY, float posZ) {
        if (tess.drawMode == GL11.GL_TRIANGLES) {
            switch (tess.addedVertices % 3) {
                case 0:
                    prepareVertex(vertA, posX, posY, posZ);
                    break;
                case 1:
                    prepareVertex(vertB, posX, posY, posZ);
                    break;
                case 2:
                    prepareVertex(vertC, posX, posY, posZ);
                default:
                    addTrianglePrimitive();
            }
        } else if (tess.drawMode == GL11.GL_QUADS) {
            switch (tess.addedVertices % 4) {
                case 0:
                    prepareVertex(vertA, posX, posY, posZ);
                    break;
                case 1:
                    prepareVertex(vertB, posX, posY, posZ);
                    break;
                case 2:
                    prepareVertex(vertC, posX, posY, posZ);
                    break;
                case 3:
                    prepareVertex(vertD, posX, posY, posZ);
                default:
                    addQuadPrimitive();
            }
        } else {
            prepareVertex(vertA, posX, posY, posZ);
            addVertex(vertA);
        }
    }

    protected void prepareBuffer() {
        val bufferSize = Math.min(tess.rawBuffer == null ? 0 : tess.rawBuffer.length,
                                  ((TessellatorAccessor) tess).rawBufferSize());
        val expectedSize = Math.max(tess.rawBufferIndex + requiredSpaceInts() + 128, MIN_BUFFER_SIZE_INTS);
        if (expectedSize < bufferSize) {
            return;
        }

        extendBuffer(bufferSize, expectedSize);
    }

    protected int requiredSpaceInts() {
        val iStride = vertexStrideInt();
        return switch (tess.drawMode) {
            case GL11.GL_TRIANGLES -> iStride * 3;
            case GL11.GL_QUADS -> iStride * 4;
            default -> iStride;
        };
    }

    protected void extendBuffer(int oldBufferSize, int expectedSize) {
        val newBufferSize1 = oldBufferSize * 2;
        val newBufferSize2 = (int) Math.ceil(expectedSize * 1.5);
        val newBufferSize = Math.max(newBufferSize1, newBufferSize2);
        val oldRawBuffer = tess.rawBuffer;
        val newRawBuffer = oldRawBuffer == null ? new int[newBufferSize] : Arrays.copyOf(oldRawBuffer, newBufferSize);

        ((TessellatorAccessor) tess).rawBufferSize(newBufferSize);
        tess.rawBuffer = newRawBuffer;
    }

    protected void prepareVertex(ShaderVert vertex, float posX, float posY, float posZ) {
        vertex.positionX = posX;
        vertex.positionY = posY;
        vertex.positionZ = posZ;
        vertex.textureU = (float) tess.textureU;
        vertex.textureV = (float) tess.textureV;
        vertex.colorARGB = tess.color;
        vertex.lightMapUV = tess.brightness;
        long packedEntityData = entityData.getPackedEntityData();
        vertex.entityData = ShaderEntityData.unpackEntityData(packedEntityData);
        vertex.entityData2 = ShaderEntityData.unpackEntityData2(packedEntityData);

        tess.addedVertices++;
        tess.vertexCount++;
    }

    protected void addTrianglePrimitive() {
        ShaderVert.completeTriangle(vertA, vertB, vertC);
        tess.hasNormals = true;

        addVertex(vertA);
        addVertex(vertB);
        addVertex(vertC);
    }

    protected void addQuadPrimitive() {
        ShaderVert.completeQuad(vertA, vertB, vertC, vertD);
        tess.hasNormals = true;

        addVertex(vertA);
        addVertex(vertB);
        addVertex(vertC);
        addVertex(vertD);
    }

    protected void addVertex(ShaderVert vertex) {
        prepareBuffer();
        vertex.toIntArray(tess.rawBufferIndex, tess.rawBuffer);
        tess.rawBufferIndex += vertexStrideInt();
    }
}
