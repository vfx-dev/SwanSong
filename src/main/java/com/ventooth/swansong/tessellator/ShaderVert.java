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
}
