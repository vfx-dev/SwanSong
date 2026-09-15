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

import Reika.DragonAPI.IO.DelegateFontRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DelegateFontRenderer.class)
public abstract class DelegateFontRendererMixin {
    @Inject(method = "drawString",
            at = @At("RETURN"),
            require = 2)
    private void fix_resetColor(CallbackInfoReturnable<Integer> cir) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    @Inject(method = "drawSplitString",
            at = @At("RETURN"),
            require = 1)
    private void fix_resetColor(CallbackInfo ci) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }
}
