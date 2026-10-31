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
import com.ventooth.swansong.mixin.extensions.RendererLivingEntityExt;
import com.ventooth.swansong.platform.ArchaicShaderEngine;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;

@Mixin(RendererLivingEntity.class)
public abstract class RendererLivingEntityMixin {
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

    @WrapOperation(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                   at = @At(value = "INVOKE",
                            target = "Lorg/lwjgl/opengl/GL11;glColor4f(FFFF)V",
                            remap = false),
                   slice = @Slice(from = @At(value = "INVOKE",
                                             target = "Lnet/minecraft/client/renderer/entity/RendererLivingEntity;renderEquippedItems(Lnet/minecraft/entity/EntityLivingBase;F)V")),
                   require = 1)
    private void state_UpdateEntityColor(float r, float g, float b, float a, Operation<Void> original) {
        if (ArchaicShaderEngine.isInitialized() && ArchaicShaderEngine.get().graph().isManaged()) {
            ArchaicShaderEngine.get().state().updateEntityColor(r, g, b, a);
        }
        original.call(r, g, b, a);
    }

    @WrapOperation(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/entity/RendererLivingEntity;inheritRenderPass(Lnet/minecraft/entity/EntityLivingBase;IF)I"),
                   require = 1)
    private int fix_OverlayPassBlend(RendererLivingEntity thiz,
                                     EntityLivingBase entity,
                                     int pass,
                                     float subTick,
                                     Operation<Integer> original) {
        val result = original.call(thiz, entity, pass, subTick);
        if (ArchaicShaderEngine.isInitialized() && ArchaicShaderEngine.get().graph().isManaged()) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
        return result;
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glDepthFunc(I)V",
                     ordinal = 1,
                     remap = false),
            slice = @Slice(from = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/renderer/entity/RendererLivingEntity;renderEquippedItems(Lnet/minecraft/entity/EntityLivingBase;F)V")),
            require = 1)
    private void state_ResetEntityColor(CallbackInfo ci) {
        if (ArchaicShaderEngine.isInitialized()) {
            ArchaicShaderEngine.get().state().resetEntityColor();
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glDepthFunc(I)V",
                     ordinal = 0,
                     remap = false),
            require = 1)
    private void hook_BeginGlint(CallbackInfo ci) {
        RendererLivingEntityExt.beginGlint();
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glDepthFunc(I)V",
                     ordinal = 1,
                     remap = false),
            require = 1)
    private void hook_EndGlint(CallbackInfo ci) {
        RendererLivingEntityExt.endGlint();
    }

    @WrapWithCondition(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/renderer/OpenGlHelper;setActiveTexture(I)V"),
                       require = 4)
    private boolean skip_SetActiveTexture(int texture) {
        return !ArchaicShaderEngine.isInitialized();
    }
}
