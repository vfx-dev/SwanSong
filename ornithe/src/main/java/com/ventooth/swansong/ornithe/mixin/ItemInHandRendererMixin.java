/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */
package com.ventooth.swansong.ornithe.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.ventooth.swansong.ornithe.SwanSongOrnithe;
import net.minecraft.block.Block;
import net.minecraft.client.render.ItemInHandRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    private ItemStack itemInHand;

    @Shadow
    protected abstract boolean isTranslucent(Block block);

    @WrapWithCondition(method = "render",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/render/platform/GlStateManager;depthMask(Z)V"),
                       require = 2)
    private boolean swansong$skipDepthMask(boolean mask) {
        return !SwanSongOrnithe.ENGINE.isInitializedImpl();
    }

    @Inject(method = "tick",
            at = @At("RETURN"))
    private void swansong$heldItemTranslucent(CallbackInfo ci) {
        if (SwanSongOrnithe.ENGINE.isInitializedImpl()) {
            SwanSongOrnithe.ENGINE.state()
                                  .setHeldItemTranslucent(itemInHand != null && isTranslucent(Block.byItem(itemInHand.getItem())));
        }
    }
}
