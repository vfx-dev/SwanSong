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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderFish;

@Mixin(value = RenderFish.class,
       priority = 1100)// Because of NotFine and the MCP:F patches...
public abstract class RenderFishMixin {
    @WrapOperation(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/Tessellator;draw()I",
                            ordinal = 1),
                   require = 1)
    private int hook_WrapLeash(Tessellator tess, Operation<Integer> original) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.push(StateGraph.Stack.Leash);
            RenderUtil.bindEmptyTexture();
            val ret = original.call(tess);
            ShaderEngine.graph.pop(StateGraph.Stack.Leash);
            return ret;
        } else {
            return original.call(tess);
        }
    }
}
