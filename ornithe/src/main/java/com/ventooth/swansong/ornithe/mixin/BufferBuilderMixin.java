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
import net.minecraft.client.render.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.Buffer;
import java.nio.IntBuffer;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin {
    @Shadow
    private IntBuffer intBuffer;
    @Shadow
    private int vertexCount;
    @Shadow
    private int drawMode;
    @Shadow
    private VertexFormat format;

    @Shadow
    protected abstract void grow(int amount);

    @Shadow
    protected abstract int getBufferIndex();

    @Inject(method = "vertices",
            at = @At("HEAD"),
            cancellable = true)
    private void swansong$restride(int[] vertices, CallbackInfo ci) {
        if (format != OrnitheVertexFormats.SHADER_BLOCK) {
            return;
        }
        ci.cancel();
        var stride = vertices.length / 4;
        grow(BlockVertexAttribs.INT_STRIDE * 4);
        ((Buffer) intBuffer).position(getBufferIndex());
        for (int i = 0; i < 4; i++) {
            intBuffer.put(vertices, i * stride, 7);
            ((Buffer) intBuffer).position(intBuffer.position() + BlockVertexAttribs.INT_STRIDE - 7);
            BlockVertexAttribs.endVertex(intBuffer, ++vertexCount, true);
        }
    }

    @Inject(method = "nextVertex",
            at = @At("TAIL"))
    private void swansong$endVertex(CallbackInfo ci) {
        if (format == OrnitheVertexFormats.SHADER_BLOCK) {
            BlockVertexAttribs.endVertex(intBuffer, vertexCount, drawMode == 7);
        }
    }
}
