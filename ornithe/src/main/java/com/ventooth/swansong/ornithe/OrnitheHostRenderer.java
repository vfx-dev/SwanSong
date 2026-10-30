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

import com.ventooth.swansong.resources.pack.DimensionInfo;
import com.ventooth.swansong.shader.HostRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.resource.Identifier;
import org.jetbrains.annotations.Nullable;

public final class OrnitheHostRenderer implements HostRenderer {
    private final OrnitheShaderPackManager shaderPacks;

    public OrnitheHostRenderer(OrnitheShaderPackManager shaderPacks) {
        this.shaderPacks = shaderPacks;
    }

    @Override
    public OrnitheShaderPackManager shaderPacks() {
        return shaderPacks;
    }

    @Override
    public boolean isHandVisible() {
        var mc = Minecraft.getInstance();
        var camera = mc.getCamera();
        if (camera == null) {
            return false;
        }
        var sleeping = camera instanceof LivingEntity living && living.isSleeping();
        return !mc.options.hideGui && mc.options.perspective == 0 && !sleeping;
    }

    @Override
    public double fieldOfView(float partialTick) {
        return Minecraft.getInstance().gameRenderer.getFov(partialTick, false);
    }

    @Override
    public double farPlaneDistance() {
        return Minecraft.getInstance().gameRenderer.renderDistance;
    }

    @Override
    public void applyCameraEffects(float partialTick) {
        var mc = Minecraft.getInstance();
        mc.gameRenderer.applyHurtCam(partialTick);
        if (mc.options.viewBobbing) {
            mc.gameRenderer.applyViewBobbing(partialTick);
        }
    }

    @Override
    public void renderFirstPersonItem(float partialTick) {
        var gameRenderer = Minecraft.getInstance().gameRenderer;
        gameRenderer.enableLightMap();
        gameRenderer.itemInHandRenderer.renderInFirstPerson(partialTick);
        gameRenderer.disableLightMap();
    }

    @Override
    public double anaglyphOffset() {
        return Minecraft.getInstance().options.anaglyph ? 1 : 0;
    }

    @Override
    public int anaglyphField() {
        return GameRenderer.anaglyphFilter;
    }

    @Override
    public void bindMainFramebuffer(boolean setViewport) {
        Minecraft.getInstance()
                 .getRenderTarget()
                 .bindWrite(setViewport);
    }

    @Override
    public void resetLightmapCoords() {
        GLX.multiTexCoord2f(GLX.GL_TEXTURE1, 240F, 240F);
    }

    @Override
    public void reloadChunkRenderers() {
        Minecraft.getInstance().worldRenderer.reload();
    }

    @Override
    public @Nullable DimensionInfo currentDimension() {
        var world = Minecraft.getInstance().world;
        if (world == null || world.dimension == null) {
            return null;
        }
        var dimension = world.dimension;
        return new DimensionInfo(dimension.getId(), dimension.getName(), dimension.getClass());
    }

    @Override
    public int mainFramebufferId() {
        return Minecraft.getInstance()
                        .getRenderTarget().frameBufferId;
    }

    @Override
    public int mainFramebufferTexture() {
        return Minecraft.getInstance()
                        .getRenderTarget().colorTextureId;
    }

    @Override
    public int mainFramebufferWidth() {
        return Minecraft.getInstance()
                        .getRenderTarget().width;
    }

    @Override
    public int mainFramebufferHeight() {
        return Minecraft.getInstance()
                        .getRenderTarget().height;
    }

    @Override
    public void onEngineInit() {
    }

    @Override
    public void onEngineDeinit() {
    }

    @Override
    public double handDepth() {
        return 0.125;
    }

    @Override
    public double renderQuality() {
        return 1;
    }

    @Override
    public double shadowQuality() {
        return 1;
    }

    @Override
    public boolean allowDepthOfField() {
        return false;
    }

    @Override
    public @Nullable Integer findBlockId(String modId, String blockName) {
        var id = new Identifier(modId, blockName);
        return Block.REGISTRY.containsKey(id) ? Block.getId(Block.REGISTRY.get(id)) : null;
    }

    @Override
    public void onLifecycle(Lifecycle stage) {
    }
}
