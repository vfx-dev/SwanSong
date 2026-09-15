/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.ntm;

import com.hbm.render.shader.Shader;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Shader.class,
       remap = false)
public class ShaderMixin {
    @Inject(method = "use",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL20;glUseProgram(I)V"),
            require = 1)
    private void push(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.push(StateGraph.Stack.ExternalShader);
        }
    }

    @Inject(method = "stop",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL20;glUseProgram(I)V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void pop(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.pop(StateGraph.Stack.ExternalShader);
        }
    }
}
