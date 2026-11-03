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
import net.minecraft.client.render.entity.LightningBoltRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBoltRenderer.class)
public abstract class LightningBoltRendererMixin {
    @Inject(method = "render(Lnet/minecraft/entity/global/LightningBoltEntity;DDDFF)V",
            at = @At("HEAD"),
            require = 1)
    private void swansong$whiteBolt(CallbackInfo ci) {
        if (SwanSongOrnithe.ENGINE.isInitializedImpl()) {
            OrnitheRender.bindWhiteTexture();
            OrnitheRender.fullBrightLightmap();
        }
    }
}
