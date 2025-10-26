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
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.renderer.Tessellator;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin_RequireFoamFix {
    @Dynamic
    @ModifyConstant(method = "getVertexState_foamfix_old",
                    constant = @Constant(intValue = 32),
                    remap = false,
                    require = 1)
    private int hackStride(int constant) {
        return ShaderTess.vertexStrideByte();
    }
}
