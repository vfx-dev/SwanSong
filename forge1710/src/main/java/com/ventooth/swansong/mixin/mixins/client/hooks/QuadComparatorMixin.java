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

import com.ventooth.swansong.tessellator.ShaderTess;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.util.QuadComparator;

import static com.ventooth.swansong.tessellator.ShaderVert.POSITION_X_STRIDE_OFFSET;
import static com.ventooth.swansong.tessellator.ShaderVert.POSITION_Y_STRIDE_OFFSET;
import static com.ventooth.swansong.tessellator.ShaderVert.POSITION_Z_STRIDE_OFFSET;

@Mixin(value = QuadComparator.class,
       priority = 900) // Lower prio, so FalseTweaks can override it
public abstract class QuadComparatorMixin {
    @Shadow
    private int[] field_147627_d;

    @Shadow
    private float field_147630_a;

    @Shadow
    private float field_147628_b;

    @Shadow
    private float field_147629_c;

    /**
     * @author FalsePattern
     * @reason Fix offsets
     */
    @Overwrite
    public int compare(Integer A, Integer B) {
        val stride = ShaderTess.vertexStrideInt();
        int a = A;
        int b = B;
        val pX = this.field_147630_a;
        val pY = this.field_147628_b;
        val pZ = this.field_147629_c;
        val buf = this.field_147627_d;
        val X1 = POSITION_X_STRIDE_OFFSET;
        val X2 = stride + POSITION_X_STRIDE_OFFSET;
        val X3 = 2 * stride + POSITION_X_STRIDE_OFFSET;
        val X4 = 3 * stride + POSITION_X_STRIDE_OFFSET;
        val Y1 = POSITION_Y_STRIDE_OFFSET;
        val Y2 = stride + POSITION_Y_STRIDE_OFFSET;
        val Y3 = 2 * stride + POSITION_Y_STRIDE_OFFSET;
        val Y4 = 3 * stride + POSITION_Y_STRIDE_OFFSET;
        val Z1 = POSITION_Z_STRIDE_OFFSET;
        val Z2 = stride + POSITION_Z_STRIDE_OFFSET;
        val Z3 = 2 * stride + POSITION_Z_STRIDE_OFFSET;
        val Z4 = 3 * stride + POSITION_Z_STRIDE_OFFSET;
        float a1x = Float.intBitsToFloat(buf[a + X1]) - pX;
        float a1y = Float.intBitsToFloat(buf[a + X2]) - pY;
        float a1z = Float.intBitsToFloat(buf[a + X3]) - pZ;
        float a2x = Float.intBitsToFloat(buf[a + X4]) - pX;
        float a2y = Float.intBitsToFloat(buf[a + Y1]) - pY;
        float a2z = Float.intBitsToFloat(buf[a + Y2]) - pZ;
        float a3x = Float.intBitsToFloat(buf[a + Y3]) - pX;
        float a3y = Float.intBitsToFloat(buf[a + Y4]) - pY;
        float a3z = Float.intBitsToFloat(buf[a + Z1]) - pZ;
        float a4x = Float.intBitsToFloat(buf[a + Z2]) - pX;
        float a4y = Float.intBitsToFloat(buf[a + Z3]) - pY;
        float a4z = Float.intBitsToFloat(buf[a + Z4]) - pZ;
        float b1x = Float.intBitsToFloat(buf[b + X1]) - pX;
        float b1y = Float.intBitsToFloat(buf[b + X2]) - pY;
        float b1z = Float.intBitsToFloat(buf[b + X3]) - pZ;
        float b2x = Float.intBitsToFloat(buf[b + X4]) - pX;
        float b2y = Float.intBitsToFloat(buf[b + Y1]) - pY;
        float b2z = Float.intBitsToFloat(buf[b + Y2]) - pZ;
        float b3x = Float.intBitsToFloat(buf[b + Y3]) - pX;
        float b3y = Float.intBitsToFloat(buf[b + Y4]) - pY;
        float b3z = Float.intBitsToFloat(buf[b + Z1]) - pZ;
        float b4x = Float.intBitsToFloat(buf[b + Z2]) - pX;
        float b4y = Float.intBitsToFloat(buf[b + Z3]) - pY;
        float b4z = Float.intBitsToFloat(buf[b + Z4]) - pZ;
        float ax = (a1x + a2x + a3x + a4x) * 0.25F;
        float ay = (a1y + a2y + a3y + a4y) * 0.25F;
        float az = (a1z + a2z + a3z + a4z) * 0.25F;
        float bx = (b1x + b2x + b3x + b4x) * 0.25F;
        float by = (b1y + b2y + b3y + b4y) * 0.25F;
        float bz = (b1z + b2z + b3z + b4z) * 0.25F;
        float aLen = ax * ax + ay * ay + az * az;
        float bLen = bx * bx + by * by + bz * bz;
        return Float.compare(bLen, aLen);
    }
}
