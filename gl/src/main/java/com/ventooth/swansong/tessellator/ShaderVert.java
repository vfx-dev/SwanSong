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

import lombok.val;
import org.jetbrains.annotations.Nullable;

public class ShaderVert {
    public static final int POSITION_X_STRIDE_OFFSET = 0;
    public static final int POSITION_Y_STRIDE_OFFSET = 1;
    public static final int POSITION_Z_STRIDE_OFFSET = 2;

    public static final int TEXTURE_U_STRIDE_OFFSET = 3;
    public static final int TEXTURE_V_STRIDE_OFFSET = 4;

    public static final int COLOR_STRIDE_OFFSET = 5;
    public static final int LIGHT_MAP_STRIDE_OFFSET = 6;

    public static final int ENTITY_DATA_0_STRIDE_OFFSET = 7;
    public static final int ENTITY_DATA_1_STRIDE_OFFSET = 8;

    public static final int NORMAL_X_STRIDE_OFFSET = 9;
    public static final int NORMAL_Y_STRIDE_OFFSET = 10;
    public static final int NORMAL_Z_STRIDE_OFFSET = 11;

    public static final int TANGENT_X_STRIDE_OFFSET = 12;
    public static final int TANGENT_Y_STRIDE_OFFSET = 13;
    public static final int TANGENT_Z_STRIDE_OFFSET = 14;
    public static final int TANGENT_W_STRIDE_OFFSET = 15;

    public static final int MID_TEXTURE_U_STRIDE_OFFSET = 16;
    public static final int MID_TEXTURE_V_STRIDE_OFFSET = 17;

    public static final int EDGE_TEXTURE_U_STRIDE_OFFSET = 18;
    public static final int EDGE_TEXTURE_V_STRIDE_OFFSET = 19;

    public float positionX;
    public float positionY;
    public float positionZ;

    public float textureU;
    public float textureV;

    public int colorARGB;
    public int lightMapUV;

    public int entityData;
    public int entityData2;

    public float normalX;
    public float normalY;
    public float normalZ;

    public float tangentX;
    public float tangentY;
    public float tangentZ;
    public float tangentW;

    public float midTextureU;
    public float midTextureV;

    public float edgeTextureU;
    public float edgeTextureV;

    public void toIntArray(int index, int[] output) {
        output[POSITION_X_STRIDE_OFFSET + index] = Float.floatToRawIntBits(positionX);
        output[POSITION_Y_STRIDE_OFFSET + index] = Float.floatToRawIntBits(positionY);
        output[POSITION_Z_STRIDE_OFFSET + index] = Float.floatToRawIntBits(positionZ);

        output[TEXTURE_U_STRIDE_OFFSET + index] = Float.floatToRawIntBits(textureU);
        output[TEXTURE_V_STRIDE_OFFSET + index] = Float.floatToRawIntBits(textureV);

        output[COLOR_STRIDE_OFFSET + index] = colorARGB;
        output[LIGHT_MAP_STRIDE_OFFSET + index] = lightMapUV;

        output[ENTITY_DATA_0_STRIDE_OFFSET + index] = entityData;
        output[ENTITY_DATA_1_STRIDE_OFFSET + index] = entityData2;

        output[NORMAL_X_STRIDE_OFFSET + index] = Float.floatToRawIntBits(normalX);
        output[NORMAL_Y_STRIDE_OFFSET + index] = Float.floatToRawIntBits(normalY);
        output[NORMAL_Z_STRIDE_OFFSET + index] = Float.floatToRawIntBits(normalZ);

        output[TANGENT_X_STRIDE_OFFSET + index] = Float.floatToRawIntBits(tangentX);
        output[TANGENT_Y_STRIDE_OFFSET + index] = Float.floatToRawIntBits(tangentY);
        output[TANGENT_Z_STRIDE_OFFSET + index] = Float.floatToRawIntBits(tangentZ);
        output[TANGENT_W_STRIDE_OFFSET + index] = Float.floatToRawIntBits(tangentW);

        output[MID_TEXTURE_U_STRIDE_OFFSET + index] = Float.floatToRawIntBits(midTextureU);
        output[MID_TEXTURE_V_STRIDE_OFFSET + index] = Float.floatToRawIntBits(midTextureV);

        output[EDGE_TEXTURE_U_STRIDE_OFFSET + index] = Float.floatToRawIntBits(edgeTextureU);
        output[EDGE_TEXTURE_V_STRIDE_OFFSET + index] = Float.floatToRawIntBits(edgeTextureV);
    }

    public static void completeTriangle(ShaderVert a, ShaderVert b, ShaderVert c) {
        calculateTriangleNormal(a, b, c);
        calculateTangent(a, b, c, null);
        calculateTriangleMidAndEdgeTexUV(a, b, c);
    }

    public static void completeQuad(ShaderVert a, ShaderVert b, ShaderVert c, ShaderVert d) {
        calculateQuadNormal(a, b, c, d);
        calculateTangent(a, b, c, d);
        calculateQuadMidAndEdgeTexUV(a, b, c, d);
    }

    private static void calculateTriangleNormal(ShaderVert vertA, ShaderVert vertB, ShaderVert vertC) {
        val length_AC_X = vertC.positionX - vertA.positionX;
        val length_AC_Y = vertC.positionY - vertA.positionY;
        val length_AC_Z = vertC.positionZ - vertA.positionZ;

        val length_AB_X = vertA.positionX - vertB.positionX;
        val length_AB_Y = vertA.positionY - vertB.positionY;
        val length_AB_Z = vertA.positionZ - vertB.positionZ;

        var normalX = (length_AC_Y * length_AB_Z) - (length_AC_Z * length_AB_Y);
        var normalY = (length_AC_Z * length_AB_X) - (length_AC_X * length_AB_Z);
        var normalZ = (length_AC_X * length_AB_Y) - (length_AC_Y * length_AB_X);

        val length = safeSqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        normalX /= length;
        normalY /= length;
        normalZ /= length;

        setVertNormal(vertA, normalX, normalY, normalZ);
        setVertNormal(vertB, normalX, normalY, normalZ);
        setVertNormal(vertC, normalX, normalY, normalZ);

    }

    private static void calculateQuadNormal(ShaderVert vertA, ShaderVert vertB, ShaderVert vertC, ShaderVert vertD) {
        val length_AC_X = vertC.positionX - vertA.positionX;
        val length_AC_Y = vertC.positionY - vertA.positionY;
        val length_AC_Z = vertC.positionZ - vertA.positionZ;

        val length_DB_X = vertD.positionX - vertB.positionX;
        val length_DB_Y = vertD.positionY - vertB.positionY;
        val length_DB_Z = vertD.positionZ - vertB.positionZ;

        var normalX = (length_AC_Y * length_DB_Z) - (length_AC_Z * length_DB_Y);
        var normalY = (length_AC_Z * length_DB_X) - (length_AC_X * length_DB_Z);
        var normalZ = (length_AC_X * length_DB_Y) - (length_AC_Y * length_DB_X);

        val length = safeSqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        normalX /= length;
        normalY /= length;
        normalZ /= length;

        setVertNormal(vertA, normalX, normalY, normalZ);
        setVertNormal(vertB, normalX, normalY, normalZ);
        setVertNormal(vertC, normalX, normalY, normalZ);
        setVertNormal(vertD, normalX, normalY, normalZ);

    }

    private static void setVertNormal(ShaderVert vert, float normalX, float normalY, float normalZ) {
        vert.normalX = normalX;
        vert.normalY = normalY;
        vert.normalZ = normalZ;
    }

    private static void calculateTangent(ShaderVert vertA,
                                         ShaderVert vertB,
                                         ShaderVert vertC,
                                         @Nullable ShaderVert vertD) {
        val length_AB_X = vertB.positionX - vertA.positionX;
        val length_AB_Y = vertB.positionY - vertA.positionY;
        val length_AB_Z = vertB.positionZ - vertA.positionZ;

        val length_AC_X = vertC.positionX - vertA.positionX;
        val length_AC_Y = vertC.positionY - vertA.positionY;
        val length_AC_Z = vertC.positionZ - vertA.positionZ;

        val length_AB_U = vertB.textureU - vertA.textureU;
        val length_AB_V = vertB.textureV - vertA.textureV;

        val length_AC_U = vertC.textureU - vertA.textureU;
        val length_AC_V = vertC.textureV - vertA.textureV;

        val lengthSq_UV = length_AB_U * length_AC_V - length_AC_U * length_AB_V;

        final float deltaFactor;
        if (lengthSq_UV == 0.0) {
            deltaFactor = 1.0f;
        } else {
            deltaFactor = 1.0f / lengthSq_UV;
        }

        final float tangentX;
        final float tangentY;
        final float tangentZ;
        {
            val tangentXUnscaled = deltaFactor * (length_AC_V * length_AB_X - length_AB_V * length_AC_X);
            val tangentYUnscaled = deltaFactor * (length_AC_V * length_AB_Y - length_AB_V * length_AC_Y);
            val tangentZUnscaled = deltaFactor * (length_AC_V * length_AB_Z - length_AB_V * length_AC_Z);

            val tangentLength = safeSqrt(tangentXUnscaled * tangentXUnscaled +
                                         tangentYUnscaled * tangentYUnscaled +
                                         tangentZUnscaled * tangentZUnscaled);
            tangentX = tangentXUnscaled / tangentLength;
            tangentY = tangentYUnscaled / tangentLength;
            tangentZ = tangentZUnscaled / tangentLength;
        }

        final float biTangentX;
        final float biTangentY;
        final float biTangentZ;
        {
            val biTangentXUnscaled = deltaFactor * (-length_AC_U * length_AB_X + length_AB_U * length_AC_X);
            val biTangentYUnscaled = deltaFactor * (-length_AC_U * length_AB_Y + length_AB_U * length_AC_Y);
            val biTangentZUnscaled = deltaFactor * (-length_AC_U * length_AB_Z + length_AB_U * length_AC_Z);

            val biTangentLength = safeSqrt(biTangentXUnscaled * biTangentXUnscaled +
                                           biTangentYUnscaled * biTangentYUnscaled +
                                           biTangentZUnscaled * biTangentZUnscaled);
            biTangentX = biTangentXUnscaled / biTangentLength;
            biTangentY = biTangentYUnscaled / biTangentLength;
            biTangentZ = biTangentZUnscaled / biTangentLength;
        }

        val otherBiTangentX = tangentY * vertA.normalZ - tangentZ * vertA.normalY;
        val otherBiTangentY = tangentZ * vertA.normalX - tangentX * vertA.normalZ;
        val otherBiTangentZ = tangentX * vertA.normalY - tangentY * vertA.normalX;

        val tangentDotProduct = (biTangentX * otherBiTangentX) +
                                (biTangentY * otherBiTangentY) +
                                (biTangentZ * otherBiTangentZ);

        final float tangentW;
        if (tangentDotProduct < 0) {
            tangentW = -1.0F;
        } else {
            tangentW = 1.0F;
        }

        setVertTangent(vertA, tangentX, tangentY, tangentZ, tangentW);
        setVertTangent(vertB, tangentX, tangentY, tangentZ, tangentW);
        setVertTangent(vertC, tangentX, tangentY, tangentZ, tangentW);
        if (vertD != null) {
            setVertTangent(vertD, tangentX, tangentY, tangentZ, tangentW);
        }
    }

    private static void setVertTangent(ShaderVert vert, float tangentX, float tangentY, float tangentZ, float tangentW) {
        vert.tangentX = tangentX;
        vert.tangentY = tangentY;
        vert.tangentZ = tangentZ;
        vert.tangentW = tangentW;
    }

    @SuppressWarnings("DuplicatedCode")
    private static void calculateTriangleMidAndEdgeTexUV(ShaderVert vertA, ShaderVert vertB, ShaderVert vertC) {
        val a = vertA;
        val au = a.textureU;
        val av = a.textureV;
        val b = vertB;
        val bu = b.textureU;
        val bv = b.textureV;
        val c = vertC;
        val cu = c.textureU;
        val cv = c.textureV;
        val minU = min(au, bu, cu);
        val minV = min(av, bv, cv);
        val maxU = max(au, bu, cu);
        val maxV = max(av, bv, cv);

        val midU = (minU + maxU) / 2;
        val midV = (minV + maxV) / 2;

        setVertEdgeAndMidTexture(a, minU, minV, midU, midV);
        setVertEdgeAndMidTexture(b, minU, minV, midU, midV);
        setVertEdgeAndMidTexture(c, minU, minV, midU, midV);
    }

    @SuppressWarnings("DuplicatedCode")
    private static void calculateQuadMidAndEdgeTexUV(ShaderVert vertA,
                                                     ShaderVert vertB,
                                                     ShaderVert vertC,
                                                     ShaderVert vertD) {
        val a = vertA;
        val au = a.textureU;
        val av = a.textureV;
        val b = vertB;
        val bu = b.textureU;
        val bv = b.textureV;
        val c = vertC;
        val cu = c.textureU;
        val cv = c.textureV;
        val d = vertD;
        val du = d.textureU;
        val dv = d.textureV;
        val minU = min(au, bu, cu, du);
        val minV = min(av, bv, cv, dv);
        val maxU = max(au, bu, cu, du);
        val maxV = max(av, bv, cv, dv);

        val midU = (minU + maxU) / 2;
        val midV = (minV + maxV) / 2;

        setVertEdgeAndMidTexture(a, minU, minV, midU, midV);
        setVertEdgeAndMidTexture(b, minU, minV, midU, midV);
        setVertEdgeAndMidTexture(c, minU, minV, midU, midV);
        setVertEdgeAndMidTexture(d, minU, minV, midU, midV);
    }

    private static void setVertEdgeAndMidTexture(ShaderVert vert,
                                                 float edgeTextureU,
                                                 float edgeTextureV,
                                                 float midTextureU,
                                                 float midTextureV) {
        setVertEdgeTexture(vert, edgeTextureU, edgeTextureV);
        setVertMidTexture(vert, midTextureU, midTextureV);
    }

    private static void setVertMidTexture(ShaderVert vert, float midTextureU, float midTextureV) {
        vert.midTextureU = midTextureU;
        vert.midTextureV = midTextureV;
    }

    private static void setVertEdgeTexture(ShaderVert vert, float edgeTextureU, float edgeTextureV) {
        vert.edgeTextureU = edgeTextureU;
        vert.edgeTextureV = edgeTextureV;
    }

    private static float min(float a, float b, float c) {
        return Math.min(Math.min(a, b), c);
    }

    private static float max(float a, float b, float c) {
        return Math.max(Math.max(a, b), c);
    }

    private static float min(float a, float b, float c, float d) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    private static float max(float a, float b, float c, float d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    private static float safeSqrt(float value) {
        return value != 0F ? (float) Math.sqrt(value) : 1F;
    }
}
