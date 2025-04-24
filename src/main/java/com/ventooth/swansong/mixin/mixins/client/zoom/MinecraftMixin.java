/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.zoom;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.ventooth.swansong.zoom.FunkyZoom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @WrapWithCondition(method = "runTick",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"),
                       require = 1)
    public boolean zoom_AdjustZoom(InventoryPlayer instance, int scrollDir) {
        if (FunkyZoom.isActive()) {
            if (FunkyZoom.shouldAdjustZoom()) {
                FunkyZoom.adjustZoom(scrollDir);
                return false;
            }
        }
        // True means the user hotbar scrolling happens
        return true;
    }
}
