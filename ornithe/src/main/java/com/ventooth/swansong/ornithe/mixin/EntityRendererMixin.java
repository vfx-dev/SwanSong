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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.ventooth.swansong.ornithe.OrnitheRender;
import com.ventooth.swansong.ornithe.SwanSongOrnithe;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @WrapWithCondition(method = "postRender",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderShadow(Lnet/minecraft/entity/Entity;DDDFF)V"))
    private boolean swansong$circleShadow(EntityRenderer<?> renderer, Entity entity, double dx, double dy, double dz, float yaw, float tickDelta) {
        if (!SwanSongOrnithe.ENGINE.isInitializedImpl()) {
            return true;
        }
        if (SwanSongOrnithe.ENGINE.shadowPassExists()) {
            return false;
        }
        OrnitheRender.resetEntityColor();
        OrnitheRender.fullBrightLightmap();
        return true;
    }

    @Inject(method = "renderNameTag(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/entity/EntityRenderer;getTextRenderer()Lnet/minecraft/client/render/TextRenderer;"),
            cancellable = true)
    private void swansong$nameTag(CallbackInfo ci) {
        if (OrnitheRender.isManaged()) {
            if (OrnitheRender.isShadowPass()) {
                ci.cancel();
                return;
            }
            OrnitheRender.resetEntityColor();
            OrnitheRender.bindWhiteTexture();
        }
    }
}
