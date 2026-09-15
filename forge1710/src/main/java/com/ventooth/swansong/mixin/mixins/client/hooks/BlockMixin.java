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

import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.block.Block;

@Mixin(Block.class)
public abstract class BlockMixin {
    @ModifyConstant(method = "getAmbientOcclusionLightValue()F",
                    constant = @Constant(floatValue = 0.2f),
                    require = 1)
    public float state_ambientOcclusionLevel(float constant) {
        return ShaderEngine.isInitialized() ? ShaderState.blockAoLight() : constant;
    }
}
