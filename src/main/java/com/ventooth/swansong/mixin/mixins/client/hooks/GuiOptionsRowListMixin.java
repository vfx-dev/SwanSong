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

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiOptionsRowList;
import net.minecraft.client.renderer.Tessellator;

@Mixin(GuiOptionsRowList.class)
public abstract class GuiOptionsRowListMixin extends GuiListExtended {
    public GuiOptionsRowListMixin(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
    }

    @Override
    protected void drawContainerBackground(Tessellator tessellator) {

    }
}
