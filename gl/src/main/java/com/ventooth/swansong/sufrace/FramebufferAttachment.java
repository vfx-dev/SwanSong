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

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL30;

public enum FramebufferAttachment {
    color0,
    color1,
    color2,
    color3,
    color4,
    color5,
    color6,
    color7,
    depth,
    stencil,
    depthStencil;

    public int toGLConstant() {
        return switch (this) {
            case color0 -> GL30.GL_COLOR_ATTACHMENT0;
            case color1 -> GL30.GL_COLOR_ATTACHMENT1;
            case color2 -> GL30.GL_COLOR_ATTACHMENT2;
            case color3 -> GL30.GL_COLOR_ATTACHMENT3;
            case color4 -> GL30.GL_COLOR_ATTACHMENT4;
            case color5 -> GL30.GL_COLOR_ATTACHMENT5;
            case color6 -> GL30.GL_COLOR_ATTACHMENT6;
            case color7 -> GL30.GL_COLOR_ATTACHMENT7;
            case depth -> GL30.GL_DEPTH_ATTACHMENT;
            case stencil -> GL30.GL_STENCIL_ATTACHMENT;
            case depthStencil -> GL30.GL_DEPTH_STENCIL_ATTACHMENT;
        };
    }

    public static @Nullable FramebufferAttachment fromColorIndex(int index) {
        return switch (index) {
            case 0 -> color0;
            case 1 -> color1;
            case 2 -> color2;
            case 3 -> color3;
            case 4 -> color4;
            case 5 -> color5;
            case 6 -> color6;
            case 7 -> color7;
            default -> null;
        };
    }

    public static @Nullable FramebufferAttachment fromGLConstant(int constant) {
        return switch (constant) {
            case GL30.GL_COLOR_ATTACHMENT0 -> color0;
            case GL30.GL_COLOR_ATTACHMENT1 -> color1;
            case GL30.GL_COLOR_ATTACHMENT2 -> color2;
            case GL30.GL_COLOR_ATTACHMENT3 -> color3;
            case GL30.GL_COLOR_ATTACHMENT4 -> color4;
            case GL30.GL_COLOR_ATTACHMENT5 -> color5;
            case GL30.GL_COLOR_ATTACHMENT6 -> color6;
            case GL30.GL_COLOR_ATTACHMENT7 -> color7;
            case GL30.GL_DEPTH_ATTACHMENT -> depth;
            case GL30.GL_STENCIL_ATTACHMENT -> stencil;
            case GL30.GL_DEPTH_STENCIL_ATTACHMENT -> depthStencil;
            default -> null;
        };
    }
}
