/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.thermal;

import cofh.thermalexpansion.block.ender.TileTesseract;
import cofh.thermalexpansion.render.RenderTesseractStarfield;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.ventooth.swansong.shader.ShaderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.tileentity.TileEntity;

@Mixin(RenderTesseractStarfield.class)
public abstract class RenderTesseractStarfieldMixin {
    @Shadow(remap = false)
    public abstract void renderTileEntityAt(TileTesseract tileTesseract, double v, double v1, double v2, float v3);

    /**
     * @author FalsePattern
     * @reason Shader Compat
     */
    @Overwrite
    public void renderTileEntityAt(TileEntity par1, double x, double y, double z, float partialTicks) {
        if (((TileTesseract) par1).isActive) {
            renderTileEntityAt((TileTesseract) par1, x, y, z, partialTicks);
        }
    }
}
