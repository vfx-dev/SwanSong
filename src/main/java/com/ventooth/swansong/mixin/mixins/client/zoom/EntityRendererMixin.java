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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.ventooth.swansong.zoom.FunkyZoom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.MouseFilter;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow
    private MouseFilter mouseFilterXAxis;
    @Shadow
    private MouseFilter mouseFilterYAxis;

    @ModifyExpressionValue(method = {"updateRenderer", "updateCameraAndRender"},
                           at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/client/settings/GameSettings;smoothCamera:Z"),
                           require = 2)
    private boolean zoom_DoSmoothCamera(boolean original) {
        if (original || FunkyZoom.doSmoothCamera()) {
            return true;
        }

        if (FunkyZoom.shouldResetSmoothCamera()) {
            this.mouseFilterXAxis = new MouseFilter();
            this.mouseFilterYAxis = new MouseFilter();
        }
        return false;
    }

    @ModifyReturnValue(method = "getFOVModifier",
                       at = @At("TAIL"),
                       require = 1)
    public float zoom_TweakFov(float original) {
        if (FunkyZoom.isActive()) {
            return FunkyZoom.tweakFov(original);
        }
        return original;
    }
}
