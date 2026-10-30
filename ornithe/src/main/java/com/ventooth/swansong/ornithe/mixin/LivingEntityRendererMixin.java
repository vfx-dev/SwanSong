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
import com.ventooth.swansong.ornithe.SwanSongOrnithe;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.living.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Shadow
    protected abstract int getOverlayColor(LivingEntity entity, float brightness, float tickDelta);

    @Inject(method = "setupOverlayColor(Lnet/minecraft/entity/living/LivingEntity;FZ)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void swansong$entityColor(LivingEntity entity, float tickDelta, boolean alwaysRender, CallbackInfoReturnable<Boolean> cir) {
        if (!OrnitheRender.isManaged()) {
            return;
        }
        var color = getOverlayColor(entity, entity.getBrightness(tickDelta), tickDelta);
        var tinted = (color >> 24 & 0xFF) > 0;
        var hurt = entity.damagedTimer > 0 || entity.deathTicks > 0;
        var state = SwanSongOrnithe.ENGINE.state();
        if (hurt && (tinted || alwaysRender)) {
            state.updateEntityColor(1F, 0F, 0F, 0.3F);
        } else if (tinted) {
            state.updateEntityColor((color >> 16 & 0xFF) / 255F,
                                    (color >> 8 & 0xFF) / 255F,
                                    (color & 0xFF) / 255F,
                                    1F - (color >> 24 & 0xFF) / 255F);
        } else {
            state.resetEntityColor();
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "tearDownOverlayColor",
            at = @At("HEAD"),
            cancellable = true)
    private void swansong$resetEntityColor(CallbackInfo ci) {
        if (OrnitheRender.isManaged()) {
            OrnitheRender.resetEntityColor();
            ci.cancel();
        }
    }
}
