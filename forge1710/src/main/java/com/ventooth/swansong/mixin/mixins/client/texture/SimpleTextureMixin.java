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
import com.ventooth.swansong.mixin.interfaces.PBRTextureHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

@Mixin(SimpleTexture.class)
public abstract class SimpleTextureMixin implements PBRTextureHolder {
    @Final
    @Shadow
    protected ResourceLocation textureLocation;

    @WrapOperation(method = "loadTexture",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/texture/TextureUtil;uploadTextureImageAllocate(ILjava/awt/image/BufferedImage;ZZ)I"),
                   require = 1)
    private int pbr_MarkLoaded(int textureId,
                               BufferedImage img,
                               boolean blur,
                               boolean clamp,
                               Operation<Integer> original) {
        swan$baseInit(this.textureLocation, img.getWidth(), img.getHeight(), blur, clamp);
        return original.call(textureId, img, blur, clamp);
    }

    @Override
    public boolean swan$supportsPbr() {
        return true;
    }
}
