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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;

@Mixin(RendererLivingEntity.class)
public abstract class RendererLivingEntityMixin {
    @Shadow
    protected abstract int getColorMultiplier(EntityLivingBase entity, float brightness, float subTick);

    @WrapOperation(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/entity/RendererLivingEntity;shouldRenderPass(Lnet/minecraft/entity/EntityLivingBase;IF)I"),
                   require = 1)
    private int share_CaptureEntityRenderPass(RendererLivingEntity thiz,
                                              EntityLivingBase entity,
                                              int pass,
                                              float subTick,
                                              Operation<Integer> original,
                                              @Share("passRef") LocalIntRef passRef) {
        passRef.set(pass);
        return original.call(thiz, entity, pass, subTick);
    }

    // TODO: Mob damage kinda works, but looks horribly wrong. Try hitting a skeleton to see what I mean.
    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V",
                     remap = false),
            require = 1)
    private void state_UpdateEntityColor(EntityLivingBase entity,
                                         double posX,
                                         double posY,
                                         double posZ,
                                         float yaw,
                                         float subTick,
                                         CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            val brightness = entity.getBrightness(subTick);
            val color = this.getColorMultiplier(entity, brightness, subTick);

            if (entity.hurtTime > 0 || entity.deathTime > 0) {
                // If the entity is hurt, apply red tint
                ShaderState.updateEntityColor(brightness, 0F, 0F, 0.4F);
            } else if ((color >> 24 & 0xFF) > 0) {
                val a = (float) (color >> 24 & 0xFF) / 255F;
                val r = (float) (color >> 16 & 0xFF) / 255F;
                val g = (float) (color >> 8 & 0xFF) / 255F;
                val b = (float) (color & 0xFF) / 255F;

                // If the entity color alpha is more than zero, apply that instead
                ShaderState.updateEntityColor(r, g, b, 1F - a);
            } else {
                // Otherwise ensure the color is reset (entity color is additive)
                ShaderState.updateEntityColor(0F, 0F, 0F, 0F);
            }
        }
    }

    @WrapWithCondition(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/renderer/OpenGlHelper;setActiveTexture(I)V"),
                       require = 4)
    private boolean skip_SetActiveTexture(int texture) {
        return !ShaderEngine.isInitialized();
    }
}
