/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.notfine;

import com.ventooth.swansong.gui.GuiShaders;
import jss.notfine.gui.GuiCustomMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiCustomMenu.class)
public abstract class GuiCustomMenuMixin extends GuiScreen {
    private static final int SHADER_BUTTON_ID = 290;

    @Inject(method = "initGui()V",
            at = @At(value = "TAIL"))
    private void swansong$addShadersButton(CallbackInfo ci) {
        // Add the Shaders Button to the bottom of Video Options
        final GuiButton shaderButton = new GuiButton(SHADER_BUTTON_ID,
                                                     this.width / 2 - 190,
                                                     this.height - 27,
                                                     70,
                                                     20,
                                                     "Shaders...");
        this.buttonList.add(shaderButton);
    }

    @Inject(method = "actionPerformed(Lnet/minecraft/client/gui/GuiButton;)V",
            at = @At(value = "HEAD"))
    private void swansong$actionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == SHADER_BUTTON_ID) {
            this.mc.gameSettings.saveOptions();
            this.mc.displayGuiScreen(new GuiShaders(this));
        }
    }
}
