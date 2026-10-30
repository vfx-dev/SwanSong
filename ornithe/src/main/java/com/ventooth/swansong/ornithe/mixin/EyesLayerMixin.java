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
import net.minecraft.client.render.entity.layer.EnderDragonEyesLayer;
import net.minecraft.client.render.entity.layer.EndermanEyesLayer;
import net.minecraft.client.render.entity.layer.SpiderEyesLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SpiderEyesLayer.class, EndermanEyesLayer.class, EnderDragonEyesLayer.class})
public abstract class EyesLayerMixin {
    @Inject(method = {"render(Lnet/minecraft/entity/living/mob/monster/SpiderEntity;FFFFFFF)V",
                      "render(Lnet/minecraft/entity/living/mob/monster/EndermanEntity;FFFFFFF)V",
                      "render(Lnet/minecraft/entity/living/mob/monster/boss/EnderDragonEntity;FFFFFFF)V"},
            at = @At("HEAD"),
            cancellable = true)
    private void swansong$pushEyes(CallbackInfo ci) {
        if (OrnitheRender.isManaged()) {
            if (OrnitheRender.isShadowPass()) {
                ci.cancel();
                return;
            }
            OrnitheRender.push(Stack.SpiderEyes);
        }
    }

    @Inject(method = {"render(Lnet/minecraft/entity/living/mob/monster/SpiderEntity;FFFFFFF)V",
                      "render(Lnet/minecraft/entity/living/mob/monster/EndermanEntity;FFFFFFF)V",
                      "render(Lnet/minecraft/entity/living/mob/monster/boss/EnderDragonEntity;FFFFFFF)V"},
            at = @At("RETURN"))
    private void swansong$popEyes(CallbackInfo ci) {
        OrnitheRender.pop(Stack.SpiderEyes);
    }
}
