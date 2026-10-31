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

import com.ventooth.swansong.mixin.extensions.RendererLivingEntityExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.RenderItem;

@Mixin(RenderItem.class)
public abstract class RenderItemMixin {
    @Inject(method = "renderDroppedItem(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/util/IIcon;IFFFFI)V",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glDepthFunc(I)V",
                     ordinal = 0,
                     remap = false),
            remap = false,
            require = 1)
    private void hook_BeginGlint(CallbackInfo ci) {
        RendererLivingEntityExt.beginGlint();
    }

    @Inject(method = "renderDroppedItem(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/util/IIcon;IFFFFI)V",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glDepthFunc(I)V",
                     ordinal = 1,
                     remap = false),
            remap = false,
            require = 1)
    private void hook_EndGlint(CallbackInfo ci) {
        RendererLivingEntityExt.endGlint();
    }
}
