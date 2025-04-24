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

import com.falsepattern.lib.util.RenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLightningBolt;

@Mixin(RenderLightningBolt.class)
public abstract class RenderLightningBoltMixin {
    @Inject(method = "doRender(Lnet/minecraft/entity/effect/EntityLightningBolt;DDDFF)V",
            at = @At("HEAD"),
            require = 1)
    private void fixStolenLightning(CallbackInfo ci) {
        RenderUtil.bindEmptyTexture();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
    }
}
