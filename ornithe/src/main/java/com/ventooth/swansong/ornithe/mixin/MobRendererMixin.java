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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.ventooth.swansong.ornithe.OrnitheRender;
import com.ventooth.swansong.shader.StateGraph.Stack;
import net.minecraft.client.render.entity.MobRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobRenderer.class)
public abstract class MobRendererMixin {
    @WrapOperation(method = "renderRiders",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/entity/living/mob/MobEntity;getLeashHolder()Lnet/minecraft/entity/Entity;"))
    private Entity swansong$pushLeash(MobEntity mob, Operation<Entity> original, @Share("leash") LocalBooleanRef pushed) {
        var holder = original.call(mob);
        if (holder != null && OrnitheRender.isManaged()) {
            OrnitheRender.bindWhiteTexture();
            OrnitheRender.push(Stack.Leash);
            pushed.set(true);
        }
        return holder;
    }

    @Inject(method = "renderRiders",
            at = @At("RETURN"))
    private void swansong$popLeash(CallbackInfo ci, @Share("leash") LocalBooleanRef pushed) {
        if (pushed.get()) {
            OrnitheRender.pop(Stack.Leash);
        }
    }
}
