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

import com.falsepattern.lib.util.RenderUtil;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;

@Mixin(RenderManager.class)
public abstract class RenderManagerMixin {
    @Inject(method = "renderDebugBoundingBox",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void hook_BeginDebugAABBThing(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            if (ShaderEngine.graph.isShadowPass()) {
                // We don't allow any rendering of DEBUG bounding boxes in the shadow pass.
                ci.cancel();
                return;
            }

            ShaderEngine.graph.push(StateGraph.Stack.AABBOutline);

            // Needed to ensure no texture or lightmap being present
            RenderUtil.bindEmptyTexture();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        }
    }

    @Inject(method = "renderDebugBoundingBox",
            at = @At(value = "RETURN"),
            require = 1)
    private static void hook_EndDebugAABBThing(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.pop(StateGraph.Stack.AABBOutline);
        }
    }
}
