/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.gui;

import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.shader.config.ConfigEntry;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.util.function.Supplier;

public class GuiConfirmReinit extends GuiScreen {
    private final Supplier<? extends GuiScreen> parentScreen;
    private final ConfigEntry.RootScreen rootScreen;
    private final String text;

    public GuiConfirmReinit(Supplier<? extends GuiScreen> parentScreen, ConfigEntry.RootScreen rootScreen) {
        this.parentScreen = parentScreen;
        this.rootScreen = rootScreen;
        text = I18n.format("gui.swansong.shaders.resetAll.query");
    }

    @Override
    public void initGui() {
        this.buttonList.add(new GuiOptionButton(0, this.width / 2 - 155, this.height - 38, I18n.format("gui.yes")));
        this.buttonList.add(new GuiOptionButton(1,
                                                this.width / 2 - 155 + 160,
                                                this.height - 38,
                                                I18n.format("gui.no")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int spaceAvailable = this.height - 38 - 20;
        int spaceRequired = Math.min(spaceAvailable, 20);

        int offset = 10 + (spaceAvailable - spaceRequired) / 2;

        this.drawCenteredString(this.fontRendererObj, text, this.width / 2, offset, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled && (button.id == 0 || button.id == 1)) {
            if (button.id == 0) {
                rootScreen.fullReset();
                rootScreen.save();
                ShaderPackManager.saveShaderSettings();
            }
            this.mc.displayGuiScreen(parentScreen.get());
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            this.mc.displayGuiScreen(parentScreen.get());
        }
    }
}
