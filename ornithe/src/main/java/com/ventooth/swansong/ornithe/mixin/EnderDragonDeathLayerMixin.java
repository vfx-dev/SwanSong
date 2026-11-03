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
import net.minecraft.client.render.entity.layer.EnderDragonDeathLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonDeathLayer.class)
public abstract class EnderDragonDeathLayerMixin {
    @Inject(method = "render(Lnet/minecraft/entity/living/mob/monster/boss/EnderDragonEntity;FFFFFFF)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/platform/Lighting;turnOff()V"),
            require = 1)
    private void swansong$whiteBeams(CallbackInfo ci) {
        if (SwanSongOrnithe.ENGINE.isInitializedImpl()) {
            OrnitheRender.bindWhiteTexture();
            OrnitheRender.fullBrightLightmap();
        }
    }
}
