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

import com.ventooth.swansong.ornithe.OrnitheVertexFormats;
import com.ventooth.swansong.tessellator.BlockVertexAttribs;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.BufferUploader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferUploader.class)
public abstract class BufferUploaderMixin {
    @Inject(method = "end",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glDrawArrays(III)V",
                     remap = false))
    private void swansong$enableAttribs(BufferBuilder builder, CallbackInfo ci) {
        if (builder.getFormat() == OrnitheVertexFormats.SHADER_BLOCK) {
            BlockVertexAttribs.enableClient(builder.getBuffer());
        }
    }

    @Inject(method = "end",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glDrawArrays(III)V",
                     shift = At.Shift.AFTER,
                     remap = false))
    private void swansong$disableAttribs(BufferBuilder builder, CallbackInfo ci) {
        if (builder.getFormat() == OrnitheVertexFormats.SHADER_BLOCK) {
            BlockVertexAttribs.disable();
        }
    }
}
