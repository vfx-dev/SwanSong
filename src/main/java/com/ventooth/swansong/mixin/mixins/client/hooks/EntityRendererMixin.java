/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.hooks;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ventooth.swansong.mixin.interfaces.ShaderGameSettings;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import com.ventooth.swansong.shader.StateGraph.Node;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.GameSettings;

import java.nio.FloatBuffer;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow
    private Minecraft mc;

    @WrapWithCondition(method = "renderHand(FI)V",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemInFirstPerson(F)V"),
                       require = 1)
    private boolean skip_RenderHand(ItemRenderer instance, float subTick) {
        return !ShaderEngine.isInitialized();
    }

    @Inject(method = "disableLightmap(D)V",
            at = @At("RETURN"),
            require = 1)
    private void hook_DisableLightmap(CallbackInfo ci) {
        // TODO: Track lightmap
    }

    @Inject(method = "enableLightmap(D)V",
            at = @At("RETURN"),
            require = 1)
    private void hook_EnableLightmap(CallbackInfo ci) {
        // TODO: Track lightmap
    }

    @Inject(method = "renderWorld(FJ)V",
            at = @At("HEAD"),
            require = 1)
    private void hook_BeginRenderWorld(CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.beginRenderWorld();
        }
    }

    @ModifyConstant(method = "setupCameraTransform",
                    constant = @Constant(floatValue = 0.1f),
                    require = 1)
    private float redSep1(float constant) {
        return constant * ((ShaderGameSettings) mc.gameSettings).swan$anaglyph();
    }

    @ModifyConstant(method = "setupCameraTransform",
                    constant = @Constant(floatValue = 0.07f),
                    require = 1)
    private float redSep2(float constant) {
        return constant * ((ShaderGameSettings) mc.gameSettings).swan$anaglyph();
    }

    @Inject(method = "renderWorld(FJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/EntityRenderer;setupCameraTransform(FI)V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void state_UpdateCamera(CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderState.updateCamera(true);
        }
    }

    @Redirect(method = "renderWorld(FJ)V",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glColorMask(ZZZZ)V"),
              require = 4)
    private void noColorMask(boolean red, boolean green, boolean blue, boolean alpha) {

    }

    @Redirect(method = "setupCameraTransform",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/settings/GameSettings;anaglyph:Z",
                       opcode = Opcodes.GETFIELD),
              require = 2)
    private boolean setupCameraTransform_anaglyph(GameSettings instance) {
        return ShaderEngine.isInitialized() && ((ShaderGameSettings) instance).swan$anaglyph() != 0;
    }

    @Redirect(method = "renderWorld(FJ)V",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/settings/GameSettings;anaglyph:Z",
                       opcode = Opcodes.GETFIELD),
              require = 3)
    private boolean renderWorld_anaglyph(GameSettings instance) {
        return ShaderEngine.isInitialized() && ((ShaderGameSettings) instance).swan$anaglyph() != 0;
    }

    @WrapOperation(method = "renderWorld(FJ)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraftforge/client/ForgeHooksClient;dispatchRenderLast(Lnet/minecraft/client/renderer/RenderGlobal;F)V",
                            remap = false),
                   require = 1)
    private void hook_RenderLastAndEndRenderWorld(RenderGlobal rg, float subTick, Operation<Void> original) {
        original.call(rg, subTick);
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.finishRenderFinal();
        }
    }

    @WrapWithCondition(method = "setupFog(IF)V",
                       at = @At(value = "INVOKE",
                                target = "Lorg/lwjgl/opengl/GL11;glFogi(II)V",
                                remap = false),
                       require = 9)
    private boolean state_UpdateFogMode(int pname, int param) {
        if (ShaderEngine.isInitialized()) {
            if (pname == GL11.GL_FOG_MODE) {
                ShaderState.updateFogMode(param);
            }
        }
        return true;
    }

    @Inject(method = "setFogColorBuffer(FFFF)Ljava/nio/FloatBuffer;",
            at = @At("HEAD"),
            require = 1)
    private void state_UpdateFogColor(float r, float g, float b, float a, CallbackInfoReturnable<FloatBuffer> cir) {
        if (ShaderEngine.isInitialized()) {
            ShaderState.updateFogColor(r, g, b);
        }
    }

    //region graph
    @Inject(method = "renderWorld(FJ)V",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                     args = "ldc=clear"),
            require = 1)
    private void renderBegin(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.RenderBegin);
        }
    }

    @Inject(method = "renderWorld(FJ)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderGlobal;renderSky(F)V"),
            require = 1)
    private void renderSky(CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.RenderSkyBasic);
        }
    }

    @Inject(method = "renderCloudsCheck",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderGlobal;renderClouds(F)V"),
            require = 1)
    private void renderClouds(RenderGlobal renderer, float tickDelta, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.RenderClouds);
        }
    }

    @Inject(method = "renderWorld",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderGlobal;sortAndRender(Lnet/minecraft/entity/EntityLivingBase;ID)I",
                     ordinal = 0),
            require = 1)
    private void renderChunk0(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.RenderChunk0);
        }
    }

    @Inject(method = "renderWorld",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/Minecraft;objectMouseOver:Lnet/minecraft/util/MovingObjectPosition;",
                     ordinal = 0),
            require = 1)
    private void renderSelectionBox(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.RenderSelectionBox);
        }
    }

    @Inject(method = "renderWorld",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderGlobal;drawBlockDamageTexture(Lnet/minecraft/client/renderer/Tessellator;Lnet/minecraft/entity/EntityLivingBase;F)V"),
            require = 1)
    private void renderBlockDamage(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.RenderBlockDamage);
        }
    }

    @Inject(method = "renderWorld",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/particle/EffectRenderer;renderLitParticles(Lnet/minecraft/entity/Entity;F)V"),
            require = 1)
    private void renderParticlesLit(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.RenderParticlesLit);
        }
    }

    @Inject(method = "renderWorld",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/particle/EffectRenderer;renderParticles(Lnet/minecraft/entity/Entity;F)V"),
            require = 1)
    private void renderParticles(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.RenderParticles);
        }
    }

    @Inject(method = "renderWorld",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/EntityRenderer;renderRainSnow(F)V"),
            require = 1)
    private void renderWeather(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            ShaderEngine.graph.moveTo(Node.RenderWeather);
        }
    }

    @Inject(method = "renderWorld(FJ)V",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V",
                     remap = false,
                     ordinal = 1,
                     shift = At.Shift.AFTER),
            require = 1)
    private void hook_PreWater(CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.RenderHand0);
        }
    }

    @Inject(method = "renderWorld",
            slice = @Slice(from = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/renderer/EntityRenderer;renderRainSnow(F)V")),
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V",
                     ordinal = 0),
            require = 1)
    private void deferredPipeline(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.runDeferredPipeline();
        }
    }

    @Inject(method = "renderWorld",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/settings/GameSettings;fancyGraphics:Z"),
            require = 1)
    private void renderChunk1(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            //DepthMask is needed for composites
            GL11.glDepthMask(true);
            ShaderEngine.graph.moveTo(Node.RenderChunk1);
        }
    }

    @Inject(method = "renderWorld",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraftforge/client/ForgeHooksClient;dispatchRenderLast(Lnet/minecraft/client/renderer/RenderGlobal;F)V"),
            require = 1)
    private void renderLast(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.RenderLast);
        }
    }

    @Inject(method = "renderWorld",
            at = @At(value = "RETURN"),
            require = 1)
    private void unmanaged(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.graph.moveTo(Node.Unmanaged);
        }
    }
    //endregion
}
