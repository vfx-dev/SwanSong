/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.mc.LightMapCoordClamp;

import com.falsepattern.lib.util.MathUtil;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;

@Mixin(value = OpenGlHelper.class,
       priority = 1100) // Raised prio for RPLE Compat
public abstract class OpenGlHelperMixin {
    @Inject(method = "setLightmapTextureCoords",
            at = @At("HEAD"))
    private static void clampTheThings(CallbackInfo ci,
                                       @Local(argsOnly = true,
                                              ordinal = 0) LocalFloatRef u,
                                       @Local(argsOnly = true,
                                              ordinal = 1) LocalFloatRef v) {
        // Some mods will try to do 255 (because, you know, that might make sense?) but that breaks stuff?
        u.set(MathUtil.clamp(u.get(), 0F, 240F));
        v.set(MathUtil.clamp(v.get(), 0F, 240F));
    }
}
