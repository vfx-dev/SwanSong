/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.texture;

import com.ventooth.swansong.pbr.PBRTextureEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;

@Mixin(value = OpenGlHelper.class,
       priority = 1100) // Raised prio for RPLE Compat
public abstract class OpenGlHelperMixin {
    @Shadow
    public static int defaultTexUnit;

    @Inject(method = "setActiveTexture",
            at = @At("HEAD"))
    private static void pbr_CaptureActiveTextureSet(int texUnit, CallbackInfo ci) {
        PBRTextureEngine.isDefaultTexUnit(texUnit == OpenGlHelperMixin.defaultTexUnit);
    }
}
