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
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

@Mixin(RenderLiving.class)
public abstract class RenderLivingMixin {
    @WrapOperation(method = "func_110827_b",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/entity/EntityLiving;getLeashedToEntity()Lnet/minecraft/entity/Entity;"),
                   require = 1)
    private Entity graph_PushLeash(EntityLiving instance,
                                   Operation<Entity> original,
                                   @Share("graph_pushed") LocalBooleanRef graphPushed) {
        graphPushed.set(false);
        if (ShaderEngine.graph.isManaged()) {
            val leashedEntity = original.call(instance);
            if (leashedEntity == null) {
                return null;
            }

            RenderUtil.bindEmptyTexture();
            ShaderEngine.graph.push(StateGraph.Stack.Leash);
            graphPushed.set(true);

            return leashedEntity;
        } else {
            return original.call(instance);
        }
    }

    @Inject(method = "func_110827_b",
            at = @At("RETURN"),
            require = 1)
    private void graph_PopLeash(CallbackInfo ci, @Share("graph_pushed") LocalBooleanRef graphPushed) {
        if (graphPushed.get()) {
            ShaderEngine.graph.pop(StateGraph.Stack.Leash);
            graphPushed.set(false);
        }
    }
}
