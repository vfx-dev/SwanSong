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

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.ventooth.swansong.mixin.interfaces.PBRTextureHolder;
import com.ventooth.swansong.pbr.PBRTextureEngine;
import com.ventooth.swansong.shader.ShaderEngine;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.TextureManager;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
    @Inject(method = "bindTexture",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureUtil;bindTexture(I)V"),
            require = 1)
    private void pbr_CaptureTex(CallbackInfo ci,
                                @Local Object object,
                                @Share("pbr_holder") LocalRef<@Nullable PBRTextureHolder> pbr_holder) {
        if (ShaderEngine.graph.isManaged()) {
            if (object instanceof PBRTextureHolder pbrHolder) {
                pbr_holder.set(pbrHolder);
            } else {
                pbr_holder.set(null);
            }
        }
    }

    @Inject(method = "bindTexture",
            at = @At(value = "RETURN"),
            require = 1)
    private void pbr_BindTex(CallbackInfo ci, @Share("pbr_holder") LocalRef<@Nullable PBRTextureHolder> pbr_holder) {
        if (ShaderEngine.graph.isManaged()) {
            PBRTextureEngine.bindPbrTex(pbr_holder.get());
        }
    }
}
