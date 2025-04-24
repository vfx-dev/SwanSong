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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.Tessellator;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

@Mixin(Tessellator.class)
public interface TessellatorAccessor {
    @Accessor("byteBuffer")
    static ByteBuffer getTessByteBuf() {
        throw new AssertionError();
    }

    @Accessor("intBuffer")
    static IntBuffer getTessIntBuf() {
        throw new AssertionError();
    }

    @Accessor("floatBuffer")
    static FloatBuffer getTessFloatBuf() {
        throw new AssertionError();
    }

    @Accessor("shortBuffer")
    static ShortBuffer getTessShortBuf() {
        throw new AssertionError();
    }

    @Accessor(value = "rawBufferSize",
              remap = false)
    int rawBufferSize();

    @Accessor(value = "rawBufferSize",
              remap = false)
    void rawBufferSize(int value);
}
