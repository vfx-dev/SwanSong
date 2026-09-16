/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.platform;

import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SkyBoxRenderer {
    public static void preSkyList() {
        if (!ShaderEngine.isInitialized()) {
            return;
        }
        ShaderState.setUpPosition();
        val fogColor = ShaderState.fogColor();
        GL11.glColor3d(fogColor.x(), fogColor.y(), fogColor.z());

        Tessellator tess = Tessellator.instance;
        float farDistance = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16;
        double xzq = farDistance * 0.9238;
        double xzp = farDistance * 0.3826;
        double xzn = -xzp;
        double xzm = -xzq;
        double top = 16f;
        double bot = -ShaderState.camPos()
                                 .y();

        tess.startDrawingQuads();
        // horizon
        tess.addVertex(xzn, bot, xzm);
        tess.addVertex(xzn, top, xzm);
        tess.addVertex(xzm, top, xzn);
        tess.addVertex(xzm, bot, xzn);

        tess.addVertex(xzm, bot, xzn);
        tess.addVertex(xzm, top, xzn);
        tess.addVertex(xzm, top, xzp);
        tess.addVertex(xzm, bot, xzp);

        tess.addVertex(xzm, bot, xzp);
        tess.addVertex(xzm, top, xzp);
        tess.addVertex(xzn, top, xzp);
        tess.addVertex(xzn, bot, xzp);

        tess.addVertex(xzn, bot, xzp);
        tess.addVertex(xzn, top, xzp);
        tess.addVertex(xzp, top, xzq);
        tess.addVertex(xzp, bot, xzq);

        tess.addVertex(xzp, bot, xzq);
        tess.addVertex(xzp, top, xzq);
        tess.addVertex(xzq, top, xzp);
        tess.addVertex(xzq, bot, xzp);

        tess.addVertex(xzq, bot, xzp);
        tess.addVertex(xzq, top, xzp);
        tess.addVertex(xzq, top, xzn);
        tess.addVertex(xzq, bot, xzn);

        tess.addVertex(xzq, bot, xzn);
        tess.addVertex(xzq, top, xzn);
        tess.addVertex(xzp, top, xzm);
        tess.addVertex(xzp, bot, xzm);

        tess.addVertex(xzp, bot, xzm);
        tess.addVertex(xzp, top, xzm);
        tess.addVertex(xzn, top, xzm);
        tess.addVertex(xzn, bot, xzm);

        tess.draw();

        val skyColor = ShaderState.skyColor();
        GL11.glColor3d(skyColor.x(), skyColor.y(), skyColor.z());
    }
}
