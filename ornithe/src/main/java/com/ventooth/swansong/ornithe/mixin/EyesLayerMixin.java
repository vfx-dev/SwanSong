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
import com.ventooth.swansong.ornithe.OrnitheRender;
import com.ventooth.swansong.shader.StateGraph.Stack;
import net.minecraft.client.render.entity.layer.EnderDragonEyesLayer;
import net.minecraft.client.render.entity.layer.EndermanEyesLayer;
import net.minecraft.client.render.entity.layer.SpiderEyesLayer;
import net.minecraft.client.render.platform.GlStateManager;
import org.lwjgl.opengl.GL11;
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
        if (OrnitheRender.isManaged()) {
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        }
        OrnitheRender.pop(Stack.SpiderEyes);
    }

    @WrapOperation(method = {"render(Lnet/minecraft/entity/living/mob/monster/SpiderEntity;FFFFFFF)V",
                             "render(Lnet/minecraft/entity/living/mob/monster/EndermanEntity;FFFFFFF)V",
                             "render(Lnet/minecraft/entity/living/mob/monster/boss/EnderDragonEntity;FFFFFFF)V"},
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/render/platform/GlStateManager;disableAlphaTest()V"))
    private void swansong$eyesAlphaTest(Operation<Void> original) {
        if (OrnitheRender.isManaged()) {
            GlStateManager.enableAlphaTest();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
        } else {
            original.call();
        }
    }

    @WrapOperation(method = {"render(Lnet/minecraft/entity/living/mob/monster/SpiderEntity;FFFFFFF)V",
                             "render(Lnet/minecraft/entity/living/mob/monster/EndermanEntity;FFFFFFF)V",
                             "render(Lnet/minecraft/entity/living/mob/monster/boss/EnderDragonEntity;FFFFFFF)V"},
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/render/platform/GlStateManager;blendFunc(II)V"))
    private void swansong$eyesBlend(int src, int dst, Operation<Void> original) {
        if (OrnitheRender.isManaged()) {
            original.call(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            original.call(src, dst);
        }
    }
}
