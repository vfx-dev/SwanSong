/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.mc.GuiLightFix;


import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.client.GuiIngameForge;

@Mixin(GuiIngameForge.class)
public class GuiIngameForgeMixin {
    @Inject(method = "renderGameOverlay",
            at = @At("HEAD"),
            require = 1)
    private void disableLighting(float partialTicks, boolean hasScreen, int mouseX, int mouseY, CallbackInfo ci) {
        GL11.glDisable(GL11.GL_LIGHTING);
    }
}
