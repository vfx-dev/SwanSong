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
import com.ventooth.swansong.mixin.extensions.RendererLivingEntityExt;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import lombok.val;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;

@Mixin(RendererLivingEntity.class)
public abstract class RendererLivingEntityMixin_ModernWarfare {
    @Unique
    private static final String MW_RENDER_MODEL_INTERCEPTOR = "Lcom/vicmatskiv/weaponlib/compatibility/Interceptors;renderArmorLayer(Lnet/minecraft/client/model/ModelBase;Lnet/minecraft/entity/Entity;FFFFFF)V";
    @Unique
    private static final String DYNAMIC_COMMENT = "Applied by: [com.vicmatskiv.weaponlib.core.WeaponlibClassTransformer$DoRenderMethodVisitor]";

    @Dynamic(DYNAMIC_COMMENT)
    @WrapOperation(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                   at = @At(value = "INVOKE",
                            target = MW_RENDER_MODEL_INTERCEPTOR,
                            remap = false,
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
        if (ShaderEngine.graph.isManaged() &&
            RendererLivingEntityExt.isSpiderEyes(self, entity, modelBase, passRef.get())) {
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

    @Dynamic(DYNAMIC_COMMENT)
    @WrapWithCondition(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
                       at = @At(value = "INVOKE",
                                target = MW_RENDER_MODEL_INTERCEPTOR,
                                remap = false),
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
