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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.texture.TextureCompass;

@Mixin(TextureCompass.class)
public abstract class TextureCompassMixin {
    @WrapOperation(method = "updateCompass",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/texture/TextureUtil;uploadTextureMipmap([[IIIIIZZ)V"),
                   require = 1)
    private void pbr_UpdateCompass(int[][] data,
                                   int width,
                                   int height,
                                   int xoffset,
                                   int yoffset,
                                   boolean linear,
                                   boolean clamp,
                                   Operation<Void> original) {
        // TODO: Implement PBR hook
        original.call(data, width, height, xoffset, yoffset, linear, clamp);
    }
}
