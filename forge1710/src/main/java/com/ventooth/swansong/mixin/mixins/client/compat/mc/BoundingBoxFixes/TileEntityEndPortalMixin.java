/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.mc.BoundingBoxFixes;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.AxisAlignedBB;

@Mixin(TileEntityEndPortal.class)
public abstract class TileEntityEndPortalMixin extends TileEntity {
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord + 0.75, zCoord, xCoord + 1, yCoord + 0.75, zCoord + 1);
    }
}
