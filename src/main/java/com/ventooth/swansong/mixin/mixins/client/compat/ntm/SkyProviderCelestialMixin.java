/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.ntm;

import com.hbm.dim.SkyProviderCelestial;
import com.hbm.render.shader.Shader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.IRenderHandler;

@Mixin(value = SkyProviderCelestial.class,
       remap = false)
public abstract class SkyProviderCelestialMixin extends IRenderHandler {
    @Shadow
    @Final
    protected static Shader planetShader;

    @Inject(method = "renderCelestials",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V"),
            slice = @Slice(from = @At(value = "CONSTANT",
                                      args = "intValue=3553",
                                      ordinal = 0),
                           to = @At(value = "INVOKE",
                                    target = "Lcom/hbm/render/shader/Shader;stop()V")),
            require = 1)
    private void shiftPlanetStop1(CallbackInfo ci) {
        planetShader.stop();
    }

    @Redirect(method = "renderCelestials",
              at = @At(value = "INVOKE",
                       target = "Lcom/hbm/render/shader/Shader;stop()V"),
              require = 1)
    private void shiftPlanetStop2(Shader instance) {

    }

    @Redirect(method = "renderSun",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;startDrawingQuads()V"),
              slice = @Slice(from = @At(value = "CONSTANT",
                                        args = "intValue=3553",
                                        ordinal = 0),
                             to = @At(value = "FIELD",
                                      target = "Lcom/hbm/dim/CelestialBody;texture:Lnet/minecraft/util/ResourceLocation;")),
              require = 2)
    private void noDrawSunSquare1(Tessellator instance) {

    }

    @Redirect(method = "renderSun",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;addVertex(DDD)V"),
              slice = @Slice(from = @At(value = "CONSTANT",
                                        args = "intValue=3553",
                                        ordinal = 0),
                             to = @At(value = "FIELD",
                                      target = "Lcom/hbm/dim/CelestialBody;texture:Lnet/minecraft/util/ResourceLocation;")),
              require = 4)
    private void noDrawSunSquare2(Tessellator instance, double x, double y, double z) {

    }

    @Redirect(method = "renderSun",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V"),
              slice = @Slice(from = @At(value = "CONSTANT",
                                        args = "intValue=3553",
                                        ordinal = 0),
                             to = @At(value = "FIELD",
                                      target = "Lcom/hbm/dim/CelestialBody;texture:Lnet/minecraft/util/ResourceLocation;")),
              require = 4)
    private void noDrawSunSquare3(Tessellator instance, double x, double y, double z, double u, double v) {

    }

    @Redirect(method = "renderSun",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;draw()I"),
              slice = @Slice(from = @At(value = "CONSTANT",
                                        args = "intValue=3553",
                                        ordinal = 0),
                             to = @At(value = "FIELD",
                                      target = "Lcom/hbm/dim/CelestialBody;texture:Lnet/minecraft/util/ResourceLocation;")),
              require = 2)
    private int noDrawSunSquare4(Tessellator instance) {
        return 0;
    }

    @Redirect(method = "render",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V"),
              slice = @Slice(from = @At(value = "INVOKE",
                                        target = "Lnet/minecraft/world/WorldProvider;isSkyColored()Z")),
              require = 1)
    private void disableHorizon(int list) {

    }
}
