/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.oc;

import com.falsepattern.lib.util.RenderUtil;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import li.cil.oc.client.renderer.tileentity.RobotRenderer$;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RobotRenderer$.class)
public abstract class RobotRendererMixin {
    @WrapWithCondition(method = "renderTileEntityAt",
                       at = @At(value = "INVOKE",
                                target = "Lorg/lwjgl/opengl/GL11;glDisable(I)V",
                                remap = false))
    // Require not set, so it's less brittle...
    private boolean fix_RobotNameTag(int cap) {
        if (cap == GL11.GL_TEXTURE_2D) {
            RenderUtil.bindEmptyTexture();
        }
        return true;
    }
}
