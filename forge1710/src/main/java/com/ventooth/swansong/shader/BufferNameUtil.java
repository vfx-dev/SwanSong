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

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import java.util.Arrays;

public final class BufferNameUtil {
    private BufferNameUtil() {
    }

    public static @Nullable CompositeTextureData gbufferIndexFromName(String name) {
        for (val data: CompositeTextureData.values()) {
            if (ArrayUtils.contains(data.names(), name)) {
                return data;
            }
        }
        return null;
    }

    public static int gbufferFormatFromName(String name) {
        return switch (name) {
            case "R8" -> GL30.GL_R8;
            case "RG8" -> GL30.GL_RG8;
            case "RGB8" -> GL11.GL_RGB8;
            case "RGBA8" -> GL11.GL_RGBA8;
            case "R8_SNORM" -> GL31.GL_R8_SNORM;
            case "RG8_SNORM" -> GL31.GL_RG8_SNORM;
            case "RGB8_SNORM" -> GL31.GL_RGB8_SNORM;
            case "RGBA8_SNORM" -> GL31.GL_RGBA8_SNORM;
            case "R16" -> GL30.GL_R16;
            case "RG16" -> GL30.GL_RG16;
            case "RGB16" -> GL11.GL_RGB16;
            case "RGBA16" -> GL11.GL_RGBA16;
            case "R16_SNORM" -> GL31.GL_R16_SNORM;
            case "RG16_SNORM" -> GL31.GL_RG16_SNORM;
            case "RGB16_SNORM" -> GL31.GL_RGB16_SNORM;
            case "RGBA16_SNORM" -> GL31.GL_RGBA16_SNORM;
            case "R16F" -> GL30.GL_R16F;
            case "RG16F" -> GL30.GL_RG16F;
            case "RGB16F" -> GL30.GL_RGB16F;
            case "RGBA16F" -> GL30.GL_RGBA16F;
            case "R32F" -> GL30.GL_R32F;
            case "RG32F" -> GL30.GL_RG32F;
            case "RGB32F" -> GL30.GL_RGB32F;
            case "RGBA32F" -> GL30.GL_RGBA32F;
            case "R32I" -> GL30.GL_R32I;
            case "RG32I" -> GL30.GL_RG32I;
            case "RGB32I" -> GL30.GL_RGB32I;
            case "RGBA32I" -> GL30.GL_RGBA32I;
            case "R32UI" -> GL30.GL_R32UI;
            case "RG32UI" -> GL30.GL_RG32UI;
            case "RGB32UI" -> GL30.GL_RGB32UI;
            case "RGBA32UI" -> GL30.GL_RGBA32UI;
            case "R3_G3_B2" -> GL11.GL_R3_G3_B2;
            case "RGB5_A1" -> GL11.GL_RGB5_A1;
            case "RGB10_A2" -> GL11.GL_RGB10_A2;
            case "R11F_G11F_B10F" -> GL30.GL_R11F_G11F_B10F;
            case "RGB9_E5" -> GL30.GL_RGB9_E5;
            default -> -1;
        };
    }

    public static String gbufferFormatNameFromEnum(int glEnum) {
        return switch (glEnum) {
            case GL30.GL_R8 -> "R8";
            case GL30.GL_RG8 -> "RG8";
            case GL11.GL_RGB8 -> "RGB8";
            case GL11.GL_RGBA8 -> "RGBA8";
            case GL31.GL_R8_SNORM -> "R8_SNORM";
            case GL31.GL_RG8_SNORM -> "RG8_SNORM";
            case GL31.GL_RGB8_SNORM -> "RGB8_SNORM";
            case GL31.GL_RGBA8_SNORM -> "RGBA8_SNORM";
            case GL30.GL_R16 -> "R16";
            case GL30.GL_RG16 -> "RG16";
            case GL11.GL_RGB16 -> "RGB16";
            case GL11.GL_RGBA16 -> "RGBA16";
            case GL31.GL_R16_SNORM -> "R16_SNORM";
            case GL31.GL_RG16_SNORM -> "RG16_SNORM";
            case GL31.GL_RGB16_SNORM -> "RGB16_SNORM";
            case GL31.GL_RGBA16_SNORM -> "RGBA16_SNORM";
            case GL30.GL_R16F -> "R16F";
            case GL30.GL_RG16F -> "RG16F";
            case GL30.GL_RGB16F -> "RGB16F";
            case GL30.GL_RGBA16F -> "RGBA16F";
            case GL30.GL_R32F -> "R32F";
            case GL30.GL_RG32F -> "RG32F";
            case GL30.GL_RGB32F -> "RGB32F";
            case GL30.GL_RGBA32F -> "RGBA32F";
            case GL30.GL_R32I -> "R32I";
            case GL30.GL_RG32I -> "RG32I";
            case GL30.GL_RGB32I -> "RGB32I";
            case GL30.GL_RGBA32I -> "RGBA32I";
            case GL30.GL_R32UI -> "R32UI";
            case GL30.GL_RG32UI -> "RG32UI";
            case GL30.GL_RGB32UI -> "RGB32UI";
            case GL30.GL_RGBA32UI -> "RGBA32UI";
            case GL11.GL_R3_G3_B2 -> "R3_G3_B2";
            case GL11.GL_RGB5_A1 -> "RGB5_A1";
            case GL11.GL_RGB10_A2 -> "RGB10_A2";
            case GL30.GL_R11F_G11F_B10F -> "R11F_G11F_B10F";
            case GL30.GL_RGB9_E5 -> "RGB9_E5";
            //Custom texture / internal stuff
            case GL11.GL_RGB -> "RGB";
            case GL11.GL_RGBA -> "RGBA";
            case GL11.GL_DEPTH_COMPONENT -> "DEPTH_COMPONENT";
            default -> "UNKNOWN";
        };
    }
}
