/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.thaumcraft;

import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.client.lib.RenderEventHandler;

@Mixin(value = RenderEventHandler.class,
       remap = false)
public abstract class RenderEventHandlerMixin {
    @Inject(method = "blockHighlight",
            at = @At("HEAD"),
            require = 1)
    private void preBlockHighlight(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.push(StateGraph.Stack.BlockHighlightTextured);
            GL11.glDepthMask(true);
        }
    }
    @Inject(method = "blockHighlight",
            at = @At("RETURN"),
            require = 1)
    private void postBlockHighlight(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.pop(StateGraph.Stack.BlockHighlightTextured);
            GL11.glDepthMask(false);
        }
    }
}
