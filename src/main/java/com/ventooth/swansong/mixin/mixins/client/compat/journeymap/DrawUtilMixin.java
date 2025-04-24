/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.journeymap;

import com.falsepattern.lib.util.RenderUtil;
import com.ventooth.swansong.api.ShaderStateInfo;
import journeymap.client.render.draw.DrawUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DrawUtil.class,
       remap = false)
public abstract class DrawUtilMixin {
    @Inject(method = {"drawRectangle(DDDDII)V", "drawGradientRect(DDDDLjava/lang/Integer;ILjava/lang/Integer;I)V"},
            at = @At(value = "HEAD"),
            require = 2)
    private static void drawRectangle(CallbackInfo ci) {
        if (ShaderStateInfo.isRendering()) {
            RenderUtil.bindEmptyTexture();
        }
    }
}
