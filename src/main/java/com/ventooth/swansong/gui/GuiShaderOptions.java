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

import com.ventooth.swansong.Share;
import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.shader.config.ConfigEntry;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.client.config.GuiButtonExt;

import java.util.function.Supplier;

public final class GuiShaderOptions extends GuiScreen {
    private static final int RESET_BUTTON = 1000;
    private static final int FULL_RESET_BUTTON = 1001;
    private static final int DONE_BUTTON = 1002;
    private static final int APPLY_BUTTON = 1003;
    private String title;

    private final Supplier<? extends GuiScreen> parentGui;
    private final ConfigEntry.RootScreen rootScreen;
    private final ConfigEntry.Screen configScreen;
    private GuiButton applyButton;
    private final TooltipRenderer tr = new TooltipRenderer();

    public GuiShaderOptions(GuiScreen parentGui, ConfigEntry.RootScreen configScreen) {
        this(() -> parentGui, configScreen, configScreen);
    }

    private GuiShaderOptions(Supplier<? extends GuiScreen> parentGui,
                             ConfigEntry.RootScreen root,
                             ConfigEntry.Screen configScreen) {
        this.title = "GuiShaderOptions";
        this.parentGui = parentGui;
        this.rootScreen = root;
        this.configScreen = configScreen;
    }

    @Override
    public void initGui() {
        this.title = configScreen.screenTitle();
        int baseId = 100;
        int baseX = 0;
        int baseY = 30;
        int stepY = 20;
        int btnWidth = 160;
        int btnHeight = 20;

        val configs = configScreen.entries();
        val configsLength = configs.size();

        int columns = configScreen.columnsHint();

        val colsMin = MathHelper.ceiling_double_int(configsLength / 9D);
        if (columns < colsMin) {
            columns = colsMin;
        }

        for (var i = 0; i < configsLength; i++) {
            val config = configs.get(i);

            // Null options used as spacers
            if (config == null) {
                continue;
            }

            val col = i % columns;
            val row = i / columns;
            val colWidth = Math.min(this.width / columns, 200);

            baseX = (this.width - colWidth * columns) / 2;
            val x = col * colWidth + 5 + baseX;
            val y = baseY + row * stepY;
            val w = colWidth - 10;


            final GuiButton btn;
            if (config instanceof ConfigEntry.Screen screen) {
                btn = new ScreenButton(baseId + i, x, y, w, btnHeight, screen);
            } else if (config instanceof ConfigEntry.Fixed fixed) {
                btn = new CfgFixed(baseId + i, x, y, w, btnHeight, fixed);
            } else if (config instanceof ConfigEntry.Switchable switchable) {
                btn = new CfgSwitchable(baseId + i, x, y, w, btnHeight, switchable);
            } else if (config instanceof ConfigEntry.Toggleable toggleable) {
                btn = new CfgToggleable(baseId + i, x, y, w, btnHeight, toggleable);
            } else if (config instanceof ConfigEntry.Draggable draggable) {
                btn = new CfgDraggable(baseId + i, x, y, w, btnHeight, draggable);
            } else {
                Share.log.error("Unknown config entry class: {}",
                                config.getClass()
                                      .getName());
                continue;
            }

            btn.enabled = true;
            this.buttonList.add(btn);
        }

        val subBtnWidth = btnWidth / 2 - 5;
        if (configScreen instanceof ConfigEntry.RootScreen) {
            int offset = width / 2 - btnWidth - 10;
            buttonList.add(new GuiButtonExt(RESET_BUTTON,
                                            offset,
                                            height / 6 + 168 + 11,
                                            subBtnWidth,
                                            btnHeight,
                                            I18n.format("controls.reset")));
            offset += subBtnWidth + 10;
            buttonList.add(new GuiButtonExt(FULL_RESET_BUTTON,
                                            offset,
                                            height / 6 + 168 + 11,
                                            subBtnWidth,
                                            btnHeight,
                                            I18n.format("gui.swansong.shaders.resetAll")));
        } else {
            buttonList.add(new GuiButtonExt(RESET_BUTTON,
                                            width / 2 - btnWidth - 10,
                                            height / 6 + 168 + 11,
                                            btnWidth,
                                            btnHeight,
                                            I18n.format("controls.reset")));
        }
        {
            int offset = width / 2 + 10;
            applyButton = new GuiButtonExt(APPLY_BUTTON,
                                           offset,
                                           height / 6 + 168 + 11,
                                           subBtnWidth,
                                           btnHeight,
                                           I18n.format("gui.swansong.shaders.apply"));
            applyButton.enabled = rootScreen.isModified();
            buttonList.add(applyButton);
            offset += subBtnWidth + 10;
            buttonList.add(new GuiButtonExt(DONE_BUTTON,
                                            offset,
                                            height / 6 + 168 + 11,
                                            subBtnWidth,
                                            btnHeight,
                                            I18n.format("gui.done")));
        }
    }

    private Supplier<GuiShaderOptions> refresh() {
        val parentGui = this.parentGui;
        val rootScreen = this.rootScreen;
        val configScreen = this.configScreen;
        return () -> new GuiShaderOptions(parentGui, rootScreen, configScreen);
    }

    private GuiShaderOptions doRefresh() {
        val parentGui = this.parentGui;
        val rootScreen = this.rootScreen;
        val configScreen = this.configScreen;
        return new GuiShaderOptions(parentGui, rootScreen, configScreen);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (!guibutton.enabled) {
            return;
        }

        applyButton.enabled = rootScreen.isModified();

        if (guibutton instanceof ScreenButton screenBtn) {
            this.mc.displayGuiScreen(new GuiShaderOptions(refresh(), rootScreen, screenBtn.screen));
            return;
        }

        if (guibutton.id == FULL_RESET_BUTTON && configScreen == rootScreen) {
            this.mc.displayGuiScreen(new GuiConfirmReinit(refresh(), rootScreen));
            return;
        }

        if (guibutton.id == RESET_BUTTON) {
            configScreen.reset();
            this.mc.displayGuiScreen(doRefresh());
            return;
        }

        if (guibutton.id == APPLY_BUTTON) {
            if (rootScreen.isModified()) {
                rootScreen.save();
                ShaderPackManager.saveShaderSettings();
                this.mc.displayGuiScreen(doRefresh());
            }
            return;
        }

        if (guibutton.id == DONE_BUTTON) {
            if (rootScreen == configScreen && rootScreen.isModified()) {
                rootScreen.save();
                ShaderPackManager.saveShaderSettings();
            }
            this.mc.displayGuiScreen(this.parentGui.get());
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            if (configScreen == rootScreen && rootScreen.isModified()) {
                this.mc.displayGuiScreen(new GuiConfirmEscape(parentGui, rootScreen));
            } else {
                this.mc.displayGuiScreen(this.parentGui.get());
            }
        }
    }

    @Override
    public void drawScreen(int x, int y, float f) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, title, width / 2, 15, 0xFFFFFF);
        super.drawScreen(x, y, f);
        for (val button : buttonList) {
            if (button instanceof ConfigButton cb &&
                x >= button.xPosition &&
                y >= button.yPosition &&
                x < button.xPosition + button.width &&
                y < button.yPosition + button.height) {
                tr.drawTooltip(this, x, y, cb.tooltip());
                return;
            }
        }
        tr.drawTooltip(this, x, y, ObjectLists.emptyList());
    }

    private abstract class ConfigButton extends GuiButtonExt {
        public ConfigButton(int id, int xPos, int yPos, int width, int height, String displayString) {
            super(id, xPos, yPos, width, height, displayString);
        }

        abstract ObjectList<String> tooltip();
    }

    private class ScreenButton extends ConfigButton {
        final ConfigEntry.Screen screen;

        ScreenButton(int id, int posX, int posY, int width, int height, ConfigEntry.Screen screen) {
            super(id, posX, posY, width, height, screen.optionName());
            this.screen = screen;
        }

        @Override
        ObjectList<String> tooltip() {
            return screen.description();
        }
    }

    private class CfgFixed extends ConfigButton {
        final ConfigEntry.Fixed fixed;

        CfgFixed(int id, int posX, int posY, int width, int height, ConfigEntry.Fixed fixed) {
            super(id, posX, posY, width, height, fixed.optionName() + ": " + fixed.valueName());
            this.fixed = fixed;
        }

        @Override
        ObjectList<String> tooltip() {
            return fixed.description();
        }
    }

    private class CfgSwitchable extends ConfigButton {
        final ConfigEntry.Switchable switchable;

        CfgSwitchable(int id, int posX, int posY, int width, int height, ConfigEntry.Switchable switchable) {
            super(id, posX, posY, width, height, "");
            this.switchable = switchable;
            displayString = coloredStr();
        }

        private String coloredStr() {
            return switchable.optionName() + ": " + Colors.forConfig(switchable) + switchable.valueName();
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                switchable.nextValue();
                displayString = coloredStr();
                return true;
            } else {
                return false;
            }
        }

        @Override
        ObjectList<String> tooltip() {
            return switchable.description();
        }
    }

    private class CfgToggleable extends ConfigButton {
        final ConfigEntry.Toggleable toggleable;

        CfgToggleable(int id, int posX, int posY, int width, int height, ConfigEntry.Toggleable toggleable) {
            super(id, posX, posY, width, height, "");
            this.toggleable = toggleable;
            displayString = coloredStr();
        }

        private String coloredStr() {
            return toggleable.optionName() + ": " + Colors.forConfig(toggleable) + toggleLang(toggleable.getValue());
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                toggleable.toggle();
                displayString = coloredStr();
                return true;
            } else {
                return false;
            }
        }

        static String toggleLang(boolean value) {
            return value ? "ON" : "OFF";
        }

        @Override
        ObjectList<String> tooltip() {
            return toggleable.description();
        }
    }

    private class CfgDraggable extends ConfigButton {
        final ConfigEntry.Draggable draggable;

        float sliderPos;
        boolean isDragging;

        CfgDraggable(int id, int posX, int posY, int width, int height, ConfigEntry.Draggable draggable) {
            super(id, posX, posY, width, height, "");
            this.draggable = draggable;

            this.sliderPos = this.draggable.getValue();
            this.isDragging = false;
            displayString = coloredStr();
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                this.isDragging = true;
                updatePos(mouseX);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void mouseReleased(int mouseX, int mouseY) {
            isDragging = false;
        }

        @Override
        public int getHoverState(boolean mouseOver) {
            return 0; // Set to grey out the button background
        }

        @Override
        protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
            if (visible) {
                if (isDragging) {
                    updatePos(mouseX);
                }
                // Pretty much also used as the draw hook, see where this func is called from kekw
                drawSlider(mc);
            }
        }

        private String coloredStr() {
            return draggable.optionName() + ": " + Colors.forConfig(draggable) + draggable.valueName();
        }

        void updatePos(int mouseX) {
            sliderPos = draggable.setValue((mouseX - (xPosition + 4F)) / (width - 8F));
            displayString = coloredStr();
        }

        void drawSlider(Minecraft mc) {
            mc.getTextureManager()
              .bindTexture(buttonTextures);
            GL11.glColor4f(1F, 1F, 1F, 1F);

            // Draw left half of the slider
            drawTexturedModalRect(xPosition + (int) (sliderPos * (width - 8F)), yPosition, 0, 66, 4, 20);
            // Draw right half of the slider
            drawTexturedModalRect(xPosition + (int) (sliderPos * (width - 8F)) + 4, yPosition, 196, 66, 4, 20);
        }

        @Override
        ObjectList<String> tooltip() {
            return draggable.description();
        }
    }
}
