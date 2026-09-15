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

import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;

@Mixin(TileEntityBeaconRenderer.class)
public abstract class TileEntityBeaconRendererMixin {
    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityBeacon;DDDF)V",
            at = @At("HEAD"),
            require = 1)
    private void hook_BeginBeacon(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.push(StateGraph.Stack.Beacon);
        }
    }

    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityBeacon;DDDF)V",
            at = @At("RETURN"),
            require = 1)
    private void hook_EndBeacon(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.pop(StateGraph.Stack.Beacon);
        }
    }
}
