/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.avaritia;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import fox.spiteful.avaritia.render.ShaderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ShaderHelper.class,
       remap = false)
public abstract class ShaderHelperMixin {
    @WrapWithCondition(method = "useShader(ILfox/spiteful/avaritia/render/ShaderCallback;)V",
                       at = @At(value = "INVOKE",
                                target = "Lorg/lwjgl/opengl/ARBShaderObjects;glUseProgramObjectARB(I)V"),
                       require = 1)
    private static boolean fix_CosmicShader(int programObj) {
        if (ShaderEngine.graph.isManaged()) {
            if (programObj != 0) {
                ShaderEngine.graph.push(StateGraph.Stack.ExternalShader);
            } else {
                ShaderEngine.graph.pop(StateGraph.Stack.ExternalShader);
            }
            return false;
        } else {
            return true;
        }
    }
}
