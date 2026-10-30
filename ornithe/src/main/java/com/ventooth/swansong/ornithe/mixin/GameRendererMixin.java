/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */
package com.ventooth.swansong.ornithe.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.ventooth.swansong.ornithe.SwanSongOrnithe;
import com.ventooth.swansong.shader.StateGraph.Node;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.ItemInHandRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    private static boolean swansong$active() {
        return SwanSongOrnithe.ENGINE.isInitializedImpl();
    }

    private static void swansong$moveTo(Node node) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.graph()
                                  .moveTo(node);
        }
    }

    @Inject(method = "renderWorld(FJ)V",
            at = @At("HEAD"),
            require = 1)
    private void swansong$beginRenderWorld(CallbackInfo ci) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.beginRenderWorld();
        }
    }

    @Inject(method = "renderWorld(FJ)V",
            at = @At("RETURN"),
            require = 1)
    private void swansong$unmanaged(CallbackInfo ci) {
        swansong$moveTo(Node.Unmanaged);
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                     args = "ldc=clear"),
            require = 1)
    private void swansong$renderBegin(CallbackInfo ci) {
        swansong$moveTo(Node.RenderBegin);
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/GameRenderer;setupCamera(FI)V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void swansong$updateCamera(CallbackInfo ci) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.state()
                                  .updateCamera(true);
        }
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/world/WorldRenderer;renderSky(FI)V"),
            require = 1)
    private void swansong$renderSky(CallbackInfo ci) {
        swansong$moveTo(Node.RenderSkyBasic);
    }

    @Inject(method = "renderClouds",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/world/WorldRenderer;renderClouds(FI)V"),
            require = 1)
    private void swansong$renderClouds(CallbackInfo ci) {
        swansong$moveTo(Node.RenderClouds);
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/world/WorldRenderer;render(Lnet/minecraft/client/render/block/BlockLayer;DILnet/minecraft/entity/Entity;)I",
                     ordinal = 0),
            require = 1)
    private void swansong$renderChunk0(CallbackInfo ci) {
        swansong$moveTo(Node.RenderChunk0);
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/Minecraft;crosshairTarget:Lnet/minecraft/world/HitResult;",
                     ordinal = 0),
            require = 1)
    private void swansong$renderSelectionBox(CallbackInfo ci) {
        swansong$moveTo(Node.RenderSelectionBox);
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/world/WorldRenderer;renderMiningProgress(Lnet/minecraft/client/render/vertex/Tesselator;Lnet/minecraft/client/render/vertex/BufferBuilder;Lnet/minecraft/entity/Entity;F)V"),
            require = 1)
    private void swansong$renderBlockDamage(CallbackInfo ci) {
        swansong$moveTo(Node.RenderBlockDamage);
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/ParticleManager;renderLit(Lnet/minecraft/entity/Entity;F)V"),
            require = 1)
    private void swansong$renderParticlesLit(CallbackInfo ci) {
        swansong$moveTo(Node.RenderParticlesLit);
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/ParticleManager;render(Lnet/minecraft/entity/Entity;F)V"),
            require = 1)
    private void swansong$renderParticles(CallbackInfo ci) {
        swansong$moveTo(Node.RenderParticles);
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/GameRenderer;renderSnowAndRain(F)V"),
            require = 1)
    private void swansong$renderWeather(CallbackInfo ci) {
        if (swansong$active()) {
            GlStateManager.enableDepthTest();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableAlphaTest();
            SwanSongOrnithe.ENGINE.graph()
                                  .moveTo(Node.RenderWeather);
        }
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/platform/GlStateManager;depthMask(Z)V",
                     ordinal = 1,
                     shift = At.Shift.AFTER),
            require = 1)
    private void swansong$renderHand(CallbackInfo ci) {
        swansong$moveTo(Node.RenderHand0);
    }

    @Inject(method = "render(IFJ)V",
            slice = @Slice(from = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/render/GameRenderer;renderSnowAndRain(F)V")),
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/texture/TextureManager;bind(Lnet/minecraft/resource/Identifier;)V",
                     ordinal = 0),
            require = 1)
    private void swansong$deferredPipeline(CallbackInfo ci) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.runDeferredPipeline();
        }
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/world/WorldRenderer;render(Lnet/minecraft/client/render/block/BlockLayer;DILnet/minecraft/entity/Entity;)I",
                     ordinal = 3),
            require = 1)
    private void swansong$renderChunk1(CallbackInfo ci) {
        if (swansong$active()) {
            GlStateManager.depthMask(true);
            SwanSongOrnithe.ENGINE.graph()
                                  .moveTo(Node.RenderChunk1);
        }
    }

    @Inject(method = "render(IFJ)V",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                     args = "ldc=hand"),
            require = 1)
    private void swansong$renderLast(CallbackInfo ci) {
        if (swansong$active()) {
            var engine = SwanSongOrnithe.ENGINE;
            engine.graph()
                  .moveTo(Node.RenderLast);
            engine.preRenderLast();
            engine.finishRenderFinal();
        }
    }

    @WrapWithCondition(method = "renderItemInHand(FI)V",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/render/ItemInHandRenderer;renderInFirstPerson(F)V"),
                       require = 1)
    private boolean swansong$skipVanillaHand(ItemInHandRenderer instance, float tickDelta) {
        return !swansong$active();
    }

    @WrapWithCondition(method = "setupFog(IF)V",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/render/platform/GlStateManager;fogMode(I)V"),
                       require = 1)
    private boolean swansong$updateFogMode(int mode) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.state()
                                  .updateFogMode(mode);
        }
        return true;
    }

    @Inject(method = "updateColorBuffer(FFFF)Ljava/nio/FloatBuffer;",
            at = @At("HEAD"),
            require = 1)
    private void swansong$updateFogColor(float r, float g, float b, float a, CallbackInfoReturnable<FloatBuffer> cir) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.state()
                                  .updateFogColor(r, g, b);
        }
    }
}
