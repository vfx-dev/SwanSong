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
import com.ventooth.swansong.resources.pack.DefaultShaderPack;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.config.ConfigEntry;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;

import java.util.List;
import java.util.Objects;

public final class GuiShaders extends GuiScreen {
    private static final int BUTTON_ID_BROWSE = 201;
    private static final int BUTTON_ID_EXIT = 202;
    private static final int BUTTON_ID_OPTIONS = 203;

    private final GuiScreen parentGui;
    private final String screenTitle;

    private int updateTimer = -1;
    private GuiSlotShaders slots;

    private GuiButton optionsButton;
    static @Nullable ConfigEntry.RootScreen configScreen;

    public GuiShaders(GuiScreen parentGui) {
        this.parentGui = parentGui;
        this.screenTitle = I18n.format("gui.swansong.shaders.title");
    }

    @Override
    public void initGui() {
        val btnWidth = 120;
        val btnHeight = 20;
        val btnX = width - btnWidth - 10;
        val baseY = 30;
        val stepY = 20;
        val shaderListWidth = width - btnWidth - 20;
        this.slots = new GuiSlotShaders(shaderListWidth, height, baseY, height - 50, 16);

        GuiButtonShaders.addButtons(this.buttonList, btnX, baseY, stepY, btnWidth, btnHeight, ShaderEngine.locale());

        val btnFolderWidth = Math.min(150, shaderListWidth / 2 - 10);
        buttonList.add(new GuiButton(BUTTON_ID_BROWSE,
                                     shaderListWidth / 4 - btnFolderWidth / 2,
                                     height - 25,
                                     btnFolderWidth,
                                     btnHeight,
                                     I18n.format("gui.swansong.shaders.browse")));
        buttonList.add(new GuiButton(BUTTON_ID_EXIT,
                                     shaderListWidth / 4 * 3 - btnFolderWidth / 2,
                                     height - 25,
                                     btnFolderWidth,
                                     btnHeight,
                                     I18n.format("gui.swansong.shaders.exit")));

        configScreen = ShaderEngine.configScreen();
        optionsButton = new GuiButton(BUTTON_ID_OPTIONS,
                                      btnX,
                                      height - 25,
                                      btnWidth,
                                      btnHeight,
                                      I18n.format("gui.swansong.shaders.options"));
        optionsButton.enabled = configScreen != null;
        buttonList.add(optionsButton);

        updateButtons();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }

        if (button instanceof GuiButtonShaders cfgButton) {
            cfgButton.onClick();
            return;
        }

        switch (button.id) {
            case BUTTON_ID_BROWSE:
                ShaderPackManager.openShaderPacksDir();
                break;
            case BUTTON_ID_EXIT:
                this.mc.displayGuiScreen(this.parentGui);
                break;
            case BUTTON_ID_OPTIONS:
                if (configScreen != null) {
                    this.mc.displayGuiScreen(new GuiShaderOptions(this, configScreen));
                }
                break;
            default:
                this.slots.actionPerformed(button);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        slots.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(screenTitle, width / 2, 15, 0xFFFFFF);

        val version = GL11.glGetString(GL11.GL_VERSION);
        val vendor = GL11.glGetString(GL11.GL_VENDOR);
        val renderer = GL11.glGetString(GL11.GL_RENDERER);
        val info = "OpenGL: " + version + ", " + vendor + ", " + renderer;
        val infoWidth = fontRendererObj.getStringWidth(info);
        if (infoWidth < width - 5) {
            drawCenteredString(info, width / 2, height - 40, 0x808080);
        } else {
            drawString(info, 5, height - 40, 0x808080);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        configScreen = ShaderEngine.configScreen();
        optionsButton.enabled = configScreen != null;

        updateTimer--;
        if (updateTimer <= 0) {
            slots.updateList();
            updateTimer += 20;
        }
    }

    public void updateButtons() {
        for (val guiButton : buttonList) {
            if (guiButton instanceof GuiButtonShaders cfgButton) {
                cfgButton.update();
            }
        }
    }

    private void drawCenteredString(String text, int posX, int posY, int color) {
        drawCenteredString(fontRendererObj, text, posX, posY, color);
    }

    private void drawString(String text, int posX, int posY, int color) {
        drawString(fontRendererObj, text, posX, posY, color);
    }

    // region Shader List
    public final class GuiSlotShaders extends GuiSlot {
        @UnmodifiableView
        private List<String> shaderPackNames;
        private int selectedIndex;

        public GuiSlotShaders(int width, int height, int top, int bottom, int slotHeight) {
            super(GuiShaders.this.mc, width, height, top, bottom, slotHeight);
            this.updateList();
            val posYSelected = this.selectedIndex * slotHeight;
            val wMid = (bottom - top) / 2;

            if (posYSelected > wMid) {
                scrollBy(posYSelected - wMid);
            }

            // Register the scroll wheel for scrolling
            registerScrollButtons(7, 8);
        }

        @Override
        public int getListWidth() {
            return this.width - 20;
        }

        public void updateList() {
            ShaderPackManager.refreshShaderPackNames();
            shaderPackNames = ShaderPackManager.getShaderPackNames();
            selectedIndex = 0;

            val currentName = ShaderPackManager.getCurrentShaderPackName();
            val size = shaderPackNames.size();
            for (var i = 0; i < size; i++) {
                if (Objects.equals(shaderPackNames.get(i), currentName)) {
                    this.selectedIndex = i;
                    break;
                }
            }
        }

        @Override
        protected int getSize() {
            return this.shaderPackNames.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClicked, int mouseX, int mouseY) {
            if (index == this.selectedIndex) {
                return;
            }

            this.selectedIndex = index;
            ShaderPackManager.setShaderPackByName(this.shaderPackNames.get(index));
            GuiShaders.this.updateButtons();
        }

        @Override
        protected boolean isSelected(int index) {
            return index == this.selectedIndex;
        }

        @Override
        protected int getScrollBarX() {
            return this.width - 6;
        }

        @Override
        protected int getContentHeight() {
            return this.getSize() * 18;
        }

        @Override
        protected void drawBackground() {
        }

        @Override
        protected void drawContainerBackground(Tessellator tessellator) {

        }

        @Override
        protected void drawSlot(int index, int posX, int posY, int contentY, Tessellator tess, int mouseX, int mouseY) {
            String label = this.shaderPackNames.get(index);
            if (label.equals(DefaultShaderPack.NAME)) {
                label = I18n.format("gui.swansong.shaders.default");
            }

            GuiShaders.this.drawCenteredString(label, this.width / 2, posY + 1, 0xe0e0e0);
        }
    }
    // endregion
}