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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    private ItemStack itemToRender;

    @WrapWithCondition(method = "renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V",
                       at = @At(value = "INVOKE",
                                target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V",
                                remap = false),
                       remap = false,
                       require = 2)
    private boolean skip_DepthMask(boolean flag) {
        return !ShaderEngine.isInitialized() && flag;
    }

    @Inject(method = "updateEquippedItem",
            at = @At("RETURN"),
            require = 1)
    private void state_UpdateHeldItem(CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            ShaderState.setHeldItem(itemToRender);
        }
    }
}
