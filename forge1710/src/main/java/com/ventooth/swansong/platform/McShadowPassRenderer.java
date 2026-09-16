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

import com.ventooth.swansong.debug.GLDebugGroups;
import com.ventooth.swansong.mixin.extensions.WorldRendererExt;
import com.ventooth.swansong.shader.DrawBuffers;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import com.ventooth.swansong.shader.ShadowPassRenderer;
import com.ventooth.swansong.shader.ShadowProperties;
import com.ventooth.swansong.shader.StateGraph.Node;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.ForgeHooksClient;

public final class McShadowPassRenderer implements ShadowPassRenderer {
    public static @Nullable Frustrum mcFrustrum;

    private @Nullable Frustrum frustrum;
    private @Nullable ClippingHelperShadow ch;

    private int shadowFrustumCheckOffset = 0;

    private void clipRenderersByFrustumShadow(WorldRenderer[] wrs) {
        assert frustrum != null: "frustrum not initialized";

        for (int i = 0, wrsLength = wrs.length; i < wrsLength; i++) {
            var wr = wrs[i];
            val wre = (WorldRendererExt) wr;
            wre.swan$backupFrustum();
            if (!wr.skipAllRenderPasses() && (!wr.isInFrustum || (i + shadowFrustumCheckOffset & 15) == 0)) {
                wr.updateInFrustum(frustrum);
            }
        }
        shadowFrustumCheckOffset++;
    }

    private void addWorldToShadowReceivers(WorldRenderer[] wrs) {
        for (val wr : wrs) {
            val wre = (WorldRendererExt) wr;
            if (wr != null && wre.swan$initialized() && wr.isVisible && wr.isInFrustum && !wr.skipAllRenderPasses()) {
                ch.addShadowReceiver(wr);
            }
        }
    }

    @Override
    public void renderShadowPass(ShadowProperties shadow, DrawBuffers buffers) {

        if (frustrum == null || ch == null) {
            frustrum = new Frustrum();
            ch = new ClippingHelperShadow();
            frustrum.clippingHelper = ch;
        }

        GLDebugGroups.RENDER_SHADOW.push();

        val partialTicks = ShaderState.getSubTick();
        val entityRenderer = Minecraft.getMinecraft().entityRenderer;

        // Set to zero before pushing attribs
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        Minecraft mc = Minecraft.getMinecraft();
        RenderGlobal renderGlobal = mc.renderGlobal;
        ShaderEngine.graph.moveTo(Node.ShadowBegin);
        val preShadowPassThirdPersonView = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = 1;

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();

        entityRenderer.setupCameraTransform(partialTicks, 2);

        ShaderState.setCameraShadow(shadow.resolution,
                                    shadow.distance,
                                    shadow.fov,
                                    shadow.intervalSize);
        ActiveRenderInfo.updateRenderInfo(mc.thePlayer, false);

        buffers.shadow.bindDraw();

        GL11.glClearColor(1F, 1F, 1F, 1F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        val wrs = renderGlobal.sortedWorldRenderers;
        val numWrs = wrs.length;

        // region Shadow culling stuff
        val viewEntity = mc.renderViewEntity;
        ch.shadowModelViewMatrix.set(ShaderState.shadowModelView());

        ch.begin();

        addWorldToShadowReceivers(wrs);

        if (mcFrustrum != null) {
            try {
                // Defensive Copy (We do this once a frame, so should be ok?)
                val entities = mc.theWorld.loadedEntityList.toArray(new Entity[0]);
                val tileEntities = mc.theWorld.loadedTileEntityList.toArray(new TileEntity[0]);

                // TODO: Handling for infinite extent bounding boxes?
                for (val entity : entities) {
                    val aabb = entity.boundingBox;
                    if (mcFrustrum.isBoundingBoxInFrustum(aabb)) {
                        ch.addShadowReceiver(aabb);
                    }
                }
                for (val tileEntity : tileEntities) {
                    val aabb = tileEntity.getRenderBoundingBox();
                    if (mcFrustrum.isBoundingBoxInFrustum(aabb)) {
                        ch.addShadowReceiver(aabb);
                    }
                }
            } catch (RuntimeException e) {
                ShaderEngine.log.error("Caught error while doing the shadow culling: ", e);
            }
        }

        ch.end();

        clipRenderersByFrustumShadow(wrs);
        // endregion

        // region Opaque Uhh, things
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glColorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_CULL_FACE);
        mc.getTextureManager()
          .bindTexture(TextureMap.locationBlocksTexture);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GLDebugGroups.RENDER_SHADOW_0_TERRAIN.push();
        {
            ShaderEngine.graph.moveTo(Node.ShadowChunk0);
            renderGlobal.renderSortedRenderers(0, numWrs, 0, partialTicks);
        }
        GLDebugGroups.RENDER_SHADOW_0_TERRAIN.pop();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glPushMatrix();

        GLDebugGroups.RENDER_SHADOW_0_ENTITIES.push();
        {
            ForgeHooksClient.setRenderPass(0);
            RenderHelper.enableStandardItemLighting();
            renderGlobal.renderEntities(viewEntity, frustrum, partialTicks);
            RenderHelper.disableStandardItemLighting();
        }
        GLDebugGroups.RENDER_SHADOW_0_ENTITIES.pop();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        // endregion

        ShaderEngine.unlockShader();
        // shadowtex0 -> [includes all geometry]
        // shadowtex1 -> [excludes transparent geometry]
        //
        // So like, we rendered all the OPAQUE stuff so we blit it over
        ShaderEngine.blitDepth(buffers.shadowDepthTex0, buffers.shadowDepthTex1);
        // Needed as blit will drop the FB binding...
        buffers.shadow.bind();
        ShaderEngine.lockShader();

        // region Render Translucent
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        mc.getTextureManager()
          .bindTexture(TextureMap.locationBlocksTexture);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GLDebugGroups.RENDER_SHADOW_1_TERRAIN.push();
        {
            ShaderEngine.graph.moveTo(Node.ShadowChunk1);
            renderGlobal.renderSortedRenderers(0, numWrs, 1, partialTicks);
        }
        GLDebugGroups.RENDER_SHADOW_1_TERRAIN.pop();

        GLDebugGroups.RENDER_SHADOW_1_ENTITIES.push();
        {
            RenderHelper.enableStandardItemLighting();
            ForgeHooksClient.setRenderPass(1);
            renderGlobal.renderEntities(viewEntity, frustrum, partialTicks);
            ForgeHooksClient.setRenderPass(-1);
            RenderHelper.disableStandardItemLighting();
        }
        GLDebugGroups.RENDER_SHADOW_1_ENTITIES.pop();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        // endregion

        ShaderEngine.graph.moveTo(Node.ShadowLast);

        mc.gameSettings.thirdPersonView = preShadowPassThirdPersonView;

        if (shadow.depthMipmapEnabled(0)) {
            ShaderEngine.genMipmap(buffers.shadowDepthTex0);
        }
        if (shadow.depthMipmapEnabled(1)) {
            ShaderEngine.genMipmap(buffers.shadowDepthTex1);
        }
        if (shadow.colorMipmapEnabled(0)) {
            ShaderEngine.genMipmap(buffers.shadowColorTex0);
        }
        if (shadow.colorMipmapEnabled(1)) {
            ShaderEngine.genMipmap(buffers.shadowColorTex1);
        }

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        // Need to reset this before calling pop attrib!
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glPopAttrib();

        GLDebugGroups.RENDER_SHADOW.pop();

        mc.getTextureManager()
          .bindTexture(TextureMap.locationBlocksTexture);

        for (val wr : wrs) {
            if (wr != null) {
                val wre = (WorldRendererExt) wr;
                wre.swan$restoreFrustum();
            }
        }
    }
}
