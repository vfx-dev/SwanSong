/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.mc.ProperTextureDelete;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
    @Final
    @Shadow
    public Map<ResourceLocation, ITextureObject> mapTextureObjects;

    @Inject(method = "deleteTexture",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureUtil;deleteTexture(I)V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void fix_DeleteFromMapPostDelete(ResourceLocation textureLocation, CallbackInfo ci) {
        mapTextureObjects.remove(textureLocation);
    }
}
