/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.journeymap;

import journeymap.client.forge.helper.impl.RenderHelper_1_7_10;
import journeymap.client.render.draw.DrawUtil;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = RenderHelper_1_7_10.class,
       remap = false)
public abstract class RenderHelperMixin {
    /**
     * @author Ven
     * @reason Some stuff in {@link DrawUtil} messes up int/float conversions on the alpha, which ends up breaking BSL bloom??
     */
    @Overwrite
    public void glColor4f(float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, Math.min(a, 1F));
    }
}
