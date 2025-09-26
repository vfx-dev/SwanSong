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
import com.ventooth.swansong.api.ShaderStateInfo;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import com.ventooth.swansong.shader.StateGraph;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
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

    @WrapOperation(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V",
                            ordinal = 0),
                   require = 1)
    private void hook_WrapSpiderEyes(ModelBase modelBase,
                                     Entity entity,
                                     float limbSwing,
                                     float limbSwingAmount,
                                     float ageInTicks,
                                     float netHeadYaw,
                                     float headPitch,
                                     float scale,
                                     Operation<Void> original,
                                     @Share("pass_ref") LocalIntRef passRef) {
        //separated out here because intellij screams due to the unsafe cast when inlined
        val self = (RendererLivingEntity) (Object) this;
        if (ShaderEngine.graph.isManaged() && swan$isSpiderEyes(self, entity, modelBase, passRef.get())) {
            if (ShaderStateInfo.shadowPassActive()) {
                return;
            }
            ShaderEngine.graph.push(StateGraph.Stack.SpiderEyes);
            original.call(modelBase, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            ShaderEngine.graph.pop(StateGraph.Stack.SpiderEyes);
        } else {
            original.call(modelBase, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    /**
     * Explicitly extracted from the rest of the code to allow for better compat/detection in the future.
     *
     * @param render    this renderer
     * @param entity    the current entity
     * @param modelBase the model reference
     * @param pass      the entity render pass from 0-3
     *
     * @return {@code true} if this render call should be treated as spider eyes
     */
    @Unique
    private static boolean swan$isSpiderEyes(RendererLivingEntity render,
                                             Entity entity,
                                             ModelBase modelBase,
                                             int pass) {
        // Currently only Spider/Enderman/Dragon RENDERERS on pass 0
        if (pass == 0) {
            return render instanceof RenderSpider || render instanceof RenderEnderman || render instanceof RenderDragon;
        } else {
            return false;
        }
    }

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
        if (!ShaderEngine.isInitialized()) {
            return;
        }
        // TODO: Mob damage kinda works, but looks horribly wrong. Try hitting a skeleton to see what I mean.
        if (entity.hurtTime > 0 || entity.deathTime > 0) {
            val brightness = entity.getBrightness(subTick);
            ShaderState.updateEntityColor(brightness, 0F, 0F, 0.4F);
            return;
        }

        val brightness = entity.getBrightness(subTick);
        val color = this.getColorMultiplier(entity, brightness, subTick);
        if ((color >> 24 & 0xFF) > 0) {
            val a = (float) (color >> 24 & 0xFF) / 255F;
            val r = (float) (color >> 16 & 0xFF) / 255F;
            val g = (float) (color >> 8 & 0xFF) / 255F;
            val b = (float) (color & 0xFF) / 255F;
            ShaderState.updateEntityColor(r, g, b, 1F - a);
            return;
        }

        ShaderState.updateEntityColor(0F, 0F, 0F, 0F);
    }

    @WrapWithCondition(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/renderer/OpenGlHelper;setActiveTexture(I)V"),
                       require = 4)
    private boolean skip_SetActiveTexture(int texture) {
        return !ShaderEngine.isInitialized();
    }

    @WrapWithCondition(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"),
                       slice = @Slice(from = @At(value = "INVOKE",
                                                 target = "Lnet/minecraft/client/renderer/entity/RendererLivingEntity;renderEquippedItems(Lnet/minecraft/entity/EntityLivingBase;F)V")),
                       require = 1)
    private boolean skip_VanillaMobHurtRender(ModelBase instance,
                                              Entity entity,
                                              float limbSwing,
                                              float limbSwingAmount,
                                              float ageInTicks,
                                              float netHeadYaw,
                                              float headPitch,
                                              float scale) {
        return !ShaderEngine.isInitialized();
    }
}
