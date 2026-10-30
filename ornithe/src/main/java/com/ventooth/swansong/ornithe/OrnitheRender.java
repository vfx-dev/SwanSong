/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */
package com.ventooth.swansong.ornithe;

import com.ventooth.swansong.shader.StateGraph.Stack;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.texture.DynamicTexture;

public final class OrnitheRender {
    private static DynamicTexture whiteTexture;

    private OrnitheRender() {
    }

    public static boolean isManaged() {
        return SwanSongOrnithe.ENGINE.graph()
                                     .isManaged();
    }

    public static boolean isShadowPass() {
        return SwanSongOrnithe.ENGINE.graph()
                                     .isShadowPass();
    }

    public static void push(Stack stack) {
        if (isManaged()) {
            SwanSongOrnithe.ENGINE.graph()
                                  .push(stack);
        }
    }

    public static void pop(Stack stack) {
        if (isManaged()) {
            SwanSongOrnithe.ENGINE.graph()
                                  .pop(stack);
        }
    }

    public static void resetEntityColor() {
        SwanSongOrnithe.ENGINE.state()
                              .resetEntityColor();
    }

    public static void bindWhiteTexture() {
        if (whiteTexture == null) {
            whiteTexture = new DynamicTexture(1, 1);
            whiteTexture.getPixels()[0] = -1;
            whiteTexture.upload();
        }
        GlStateManager.bindTexture(whiteTexture.getGlId());
    }

    public static void fullBrightLightmap() {
        GLX.multiTexCoord2f(GLX.GL_TEXTURE1, 240F, 240F);
    }
}
