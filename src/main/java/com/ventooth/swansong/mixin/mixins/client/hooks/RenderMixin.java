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
import com.ventooth.swansong.api.ShaderStateInfo;
import com.ventooth.swansong.shader.ShaderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

@Mixin(Render.class)
public abstract class RenderMixin {
    @WrapOperation(method = "doRenderShadowAndFire",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/entity/Render;renderShadow(Lnet/minecraft/entity/Entity;DDDFF)V"),
                   require = 1)
    private void wrap_EntityCircleShadow(Render thiz,
                                         Entity entity,
                                         double posX,
                                         double posY,
                                         double posZ,
                                         float yaw,
                                         float subTick,
                                         Operation<Void> original) {
        // TODO: Toggle for skipping the entity shadows if we do or don't have a shadow pass?
        if (ShaderStateInfo.shadowPassExists()) {
            return;
        }

        // Might render right after the entity was hit
        ShaderState.updateEntityColor(0F, 0F, 0F, 0F);
        // Just in case, ensure lighting is full bright
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

        original.call(thiz, entity, posX, posY, posZ, yaw, subTick);
    }

    @Inject(method = "func_147906_a",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/entity/Render;getFontRendererFromRenderManager()Lnet/minecraft/client/gui/FontRenderer;"),
            cancellable = true,
            require = 1)
    private void wrap_EntityNameTag(CallbackInfo ci) {
        // Don't render in shadow pass
        if (ShaderStateInfo.shadowPassActive()) {
            ci.cancel();
            return;
        }

        // Might render right after the entity was hit
        ShaderState.updateEntityColor(0F, 0F, 0F, 0F);
        // No texture for the initial box
        RenderUtil.bindEmptyTexture();
    }
}
