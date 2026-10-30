/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */
package com.ventooth.swansong.ornithe.mixin;

import com.ventooth.swansong.ornithe.gui.ShadersScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.options.VideoOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoOptionsScreen.class)
public abstract class VideoOptionsScreenMixin extends Screen {
    private static final int SHADERS_BUTTON_ID = 190;

    @Inject(method = "init",
            at = @At("TAIL"))
    private void swansong$addShadersButton(CallbackInfo ci) {
        buttons.add(new ButtonWidget(SHADERS_BUTTON_ID, width / 2 - 190, height - 27, 70, 20, "Shaders..."));
    }

    @Inject(method = "buttonClicked",
            at = @At("HEAD"))
    private void swansong$openShaders(ButtonWidget button, CallbackInfo ci) {
        if (button.active && button.id == SHADERS_BUTTON_ID) {
            minecraft.options.save();
            minecraft.openScreen(new ShadersScreen(this));
        }
    }
}
