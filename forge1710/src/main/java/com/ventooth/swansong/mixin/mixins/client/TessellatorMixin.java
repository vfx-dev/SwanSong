/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client;

import com.ventooth.swansong.tessellator.ShaderTess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.Tessellator;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin {
    @Unique
    private ShaderTess swansong$shaderTess = null;

    @Inject(method = "startDrawing",
            at = @At("HEAD"),
            require = 1)
    private void initShaderTess(CallbackInfo ci) {
        if (swansong$shaderTess == null) {
            swansong$shaderTess = new ShaderTess((Tessellator) (Object) this);
        }
    }

}
