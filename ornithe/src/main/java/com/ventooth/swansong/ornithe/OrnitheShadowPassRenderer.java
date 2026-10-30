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

import com.ventooth.swansong.shader.DrawBuffers;
import com.ventooth.swansong.shader.ShadowFrustum;
import com.ventooth.swansong.shader.ShadowPassRenderer;
import com.ventooth.swansong.shader.ShadowProperties;
import com.ventooth.swansong.shader.StateGraph.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Culler;
import net.minecraft.client.render.block.BlockLayer;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.util.math.Box;

public final class OrnitheShadowPassRenderer implements ShadowPassRenderer {
    private final ShadowFrustum frustum = new ShadowFrustum();
    private final Culler culler = new Culler() {
        @Override
        public boolean isVisible(Box box) {
            return frustum.testAab((float) box.minX,
                                   (float) box.minY,
                                   (float) box.minZ,
                                   (float) box.maxX,
                                   (float) box.maxY,
                                   (float) box.maxZ);
        }

        @Override
        public void prepare(double x, double y, double z) {
        }
    };
    private int frame = Integer.MIN_VALUE;

    @Override
    public void renderShadowPass(ShadowProperties shadow, DrawBuffers buffers) {
        var engine = SwanSongOrnithe.ENGINE;
        var mc = Minecraft.getInstance();
        var worldRenderer = mc.worldRenderer;
        var camera = mc.getCamera();
        var partialTick = engine.state()
                                .getSubTick();

        GLX.bindFramebuffer(GLX.GL_FRAMEBUFFER, 0);
        engine.graph()
              .moveTo(Node.ShadowBegin);
        var perspective = mc.options.perspective;
        mc.options.perspective = 1;

        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();

        mc.gameRenderer.setupCamera(partialTick, 2);
        engine.state()
              .setCameraShadow(shadow.resolution, shadow.distance, shadow.fov, shadow.intervalSize);
        Camera.setup(mc.player, false);

        buffers.shadow.bindDraw();
        GlStateManager.clearColor(1F, 1F, 1F, 1F);
        GlStateManager.clear(16640);

        frustum.shadowModelViewMatrix.set(engine.state()
                                                .shadowModelView());
        frustum.begin();
        for (var info : worldRenderer.compiledChunks) {
            var bounds = info.chunk.bounds;
            frustum.addShadowReceiver((float) bounds.minX,
                                      (float) bounds.minY,
                                      (float) bounds.minZ,
                                      (float) bounds.maxX,
                                      (float) bounds.maxY,
                                      (float) bounds.maxZ);
        }
        frustum.end();

        GlStateManager.shadeModel(7425);
        GlStateManager.enableDepthTest();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.disableCull();
        mc.getTextureManager()
          .bind(TextureAtlas.BLOCKS_LOCATION);

        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.enableAlphaTest();

        worldRenderer.viewChanged = true;
        worldRenderer.setupRender(camera, partialTick, culler, frame++, mc.player.isSpectator());

        engine.graph()
              .moveTo(Node.ShadowChunk0);
        worldRenderer.render(BlockLayer.SOLID, partialTick, 2, camera);
        worldRenderer.render(BlockLayer.CUTOUT_MIPPED, partialTick, 2, camera);
        worldRenderer.render(BlockLayer.CUTOUT, partialTick, 2, camera);

        GlStateManager.shadeModel(7424);
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();

        Lighting.turnOn();
        worldRenderer.renderEntities(camera, culler, partialTick);
        Lighting.turnOff();

        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();

        engine.unlockShader();
        engine.blitDepth(buffers.shadowDepthTex0, buffers.shadowDepthTex1);
        buffers.shadow.bind();
        engine.lockShader();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GlStateManager.alphaFunc(516, 0.1F);
        mc.getTextureManager()
          .bind(TextureAtlas.BLOCKS_LOCATION);
        GlStateManager.shadeModel(7425);

        engine.graph()
              .moveTo(Node.ShadowChunk1);
        worldRenderer.render(BlockLayer.TRANSLUCENT, partialTick, 2, camera);

        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();

        engine.graph()
              .moveTo(Node.ShadowLast);
        mc.options.perspective = perspective;

        if (shadow.depthMipmapEnabled(0)) {
            engine.genMipmap(buffers.shadowDepthTex0);
        }
        if (shadow.depthMipmapEnabled(1)) {
            engine.genMipmap(buffers.shadowDepthTex1);
        }
        if (shadow.colorMipmapEnabled(0)) {
            engine.genMipmap(buffers.shadowColorTex0);
        }
        if (shadow.colorMipmapEnabled(1)) {
            engine.genMipmap(buffers.shadowColorTex1);
        }

        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);

        GLX.bindFramebuffer(GLX.GL_FRAMEBUFFER, 0);
        worldRenderer.viewChanged = true;
        mc.getTextureManager()
          .bind(TextureAtlas.BLOCKS_LOCATION);
    }
}
