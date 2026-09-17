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

import com.ventooth.swansong.api.SwanSongLifecycleEvent;
import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.mixin.interfaces.ShaderGameSettings;
import com.ventooth.swansong.pbr.PBRTextureEngine;
import com.ventooth.swansong.resources.pack.DimensionInfo;
import com.ventooth.swansong.shader.HostRenderer;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.common.MinecraftForge;

public final class McHostRenderer implements HostRenderer {
    @Override
    public boolean isHandVisible() {
        val mc = Minecraft.getMinecraft();
        val viewEntity = mc.renderViewEntity;
        if (viewEntity == null) {
            return false;
        }
        return !mc.gameSettings.hideGUI && mc.gameSettings.thirdPersonView == 0 && !viewEntity.isPlayerSleeping();
    }

    @Override
    public double fieldOfView(float partialTick) {
        return Minecraft.getMinecraft().entityRenderer.getFOVModifier(partialTick, false);
    }

    @Override
    public double farPlaneDistance() {
        return Minecraft.getMinecraft().entityRenderer.farPlaneDistance;
    }

    @Override
    public void applyCameraEffects(float partialTick) {
        val mc = Minecraft.getMinecraft();
        mc.entityRenderer.hurtCameraEffect(partialTick);
        if (mc.gameSettings.viewBobbing) {
            mc.entityRenderer.setupViewBobbing(partialTick);
        }
    }

    @Override
    public void renderFirstPersonItem(float partialTick) {
        val entityRenderer = Minecraft.getMinecraft().entityRenderer;
        entityRenderer.enableLightmap(partialTick);
        entityRenderer.itemRenderer.renderItemInFirstPerson(partialTick);
        entityRenderer.disableLightmap(partialTick);
    }

    @Override
    public double anaglyphOffset() {
        return ((ShaderGameSettings) Minecraft.getMinecraft().gameSettings).swan$anaglyph();
    }

    @Override
    public int anaglyphField() {
        return EntityRenderer.anaglyphField;
    }

    @Override
    public void bindMainFramebuffer(boolean setViewport) {
        Minecraft.getMinecraft()
                 .getFramebuffer()
                 .bindFramebuffer(setViewport);
    }

    @Override
    public void resetLightmapCoords() {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
    }

    @Override
    public void reloadChunkRenderers() {
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    @Override
    public @Nullable DimensionInfo currentDimension() {
        val world = Minecraft.getMinecraft().theWorld;
        if (world == null) {
            return null;
        }
        val provider = world.provider;
        if (provider == null) {
            return null;
        }
        return new DimensionInfo(provider.dimensionId, provider.getDimensionName(), provider.getClass());
    }

    @Override
    public int mainFramebufferId() {
        return Minecraft.getMinecraft()
                        .getFramebuffer().framebufferObject;
    }

    @Override
    public int mainFramebufferTexture() {
        return Minecraft.getMinecraft()
                        .getFramebuffer().framebufferTexture;
    }

    @Override
    public int mainFramebufferWidth() {
        return Minecraft.getMinecraft()
                        .getFramebuffer().framebufferWidth;
    }

    @Override
    public int mainFramebufferHeight() {
        return Minecraft.getMinecraft()
                        .getFramebuffer().framebufferHeight;
    }

    @Override
    public void onEngineInit() {
        PBRTextureEngine.init();
    }

    @Override
    public void onEngineDeinit() {
        PBRTextureEngine.deinit();
    }

    @Override
    public double handDepth() {
        return 0.125 * ShadersConfig.HandDepth.get();
    }

    @Override
    public double renderQuality() {
        return ShadersConfig.RenderQuality.get();
    }

    @Override
    public double shadowQuality() {
        return ShadersConfig.ShadowQuality.get();
    }

    @Override
    public boolean allowDepthOfField() {
        return ShadersConfig.LetMeUseDepthOfFieldPlease;
    }

    @Override
    public void onLifecycle(Lifecycle stage) {
        val event = switch (stage) {
            case RELOAD_SCHEDULED -> new SwanSongLifecycleEvent.ShaderPackReload();
            case LOADED -> new SwanSongLifecycleEvent.ShaderPackLoaded();
            case UNLOADED -> new SwanSongLifecycleEvent.ShaderPackUnloaded();
        };
        MinecraftForge.EVENT_BUS.post(event);
    }
}
