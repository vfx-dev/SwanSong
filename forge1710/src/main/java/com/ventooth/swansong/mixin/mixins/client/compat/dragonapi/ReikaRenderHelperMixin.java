/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.dragonapi;

import Reika.DragonAPI.Libraries.Rendering.ReikaRenderHelper;

import com.falsepattern.lib.util.RenderUtil;
import com.ventooth.swansong.shader.ShaderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;

@Mixin(value = ReikaRenderHelper.class, remap = false)
public abstract class ReikaRenderHelperMixin {
    @Inject(method = "prepareGeoDraw",
            at = @At("RETURN"),
            require = 1)
    private static void fix_bindEmptyTex(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            RenderUtil.bindEmptyTexture();
        }
    }

    @Inject(method = {"disableEntityLighting",
                      "disableLighting"},
            at = @At("RETURN"),
            require = 2)
    private static void fix_disableLighting(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        }
    }

    @Inject(method = "enableEntityLighting",
            at = @At("RETURN"),
            require = 1)
    private static void fix_enableLighting(CallbackInfo ci) {
        // TODO: This may break stuff.
        if (ShaderEngine.graph.isManaged()) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                                                  OpenGlHelper.lastBrightnessX,
                                                  OpenGlHelper.lastBrightnessY);
        }
    }
}
