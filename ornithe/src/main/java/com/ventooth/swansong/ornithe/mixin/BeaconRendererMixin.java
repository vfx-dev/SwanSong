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

import com.ventooth.swansong.ornithe.OrnitheRender;
import com.ventooth.swansong.shader.StateGraph.Stack;
import net.minecraft.client.render.block.entity.BeaconRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconRenderer.class)
public abstract class BeaconRendererMixin {
    @Inject(method = "render(Lnet/minecraft/block/entity/BeaconBlockEntity;DDDFI)V",
            at = @At("HEAD"))
    private void swansong$pushBeacon(CallbackInfo ci) {
        OrnitheRender.push(Stack.Beacon);
    }

    @Inject(method = "render(Lnet/minecraft/block/entity/BeaconBlockEntity;DDDFI)V",
            at = @At("RETURN"))
    private void swansong$popBeacon(CallbackInfo ci) {
        OrnitheRender.pop(Stack.Beacon);
    }
}
