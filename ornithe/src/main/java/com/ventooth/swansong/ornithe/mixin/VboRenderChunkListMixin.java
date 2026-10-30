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

import com.ventooth.swansong.tessellator.BlockVertexAttribs;
import net.minecraft.client.render.world.VboRenderChunkList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VboRenderChunkList.class)
public abstract class VboRenderChunkListMixin {
    @ModifyConstant(method = "prepareDrawState",
                    constant = @Constant(intValue = 28),
                    require = 4)
    private int swansong$stride(int stride) {
        return BlockVertexAttribs.STRIDE;
    }

    @Inject(method = "prepareDrawState",
            at = @At("TAIL"))
    private void swansong$attribs(CallbackInfo ci) {
        BlockVertexAttribs.enableVbo();
    }

    @Inject(method = "render",
            at = @At("TAIL"))
    private void swansong$disableAttribs(CallbackInfo ci) {
        BlockVertexAttribs.disable();
    }
}
