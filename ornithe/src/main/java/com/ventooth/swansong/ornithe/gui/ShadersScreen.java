/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */
package com.ventooth.swansong.ornithe.gui;

import com.ventooth.swansong.EnvInfo;
import com.ventooth.swansong.Share;
import com.ventooth.swansong.ornithe.SwanSongOrnithe;
import com.ventooth.swansong.resources.ShaderPackManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ListWidget;
import net.minecraft.client.resource.language.I18n;
import net.ornithemc.osl.config.api.ConfigManager;
import net.ornithemc.osl.config.api.config.option.FloatOption;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ShadersScreen extends Screen {
    private static final int BUTTON_ID_BROWSE = 201;
    private static final int BUTTON_ID_EXIT = 202;
    private static final int BUTTON_ID_OPTIONS = 203;
    private static final float[] QUALITIES = {0.5F, 0.7F, 1F, 1.5F, 2F};
    private static final float[] HAND_DEPTHS = {0.5F, 1F, 2F};

    private final Screen parent;
    private int updateTimer = -1;
    private PackList packs;
    private ButtonWidget optionsButton;

    public ShadersScreen(Screen parent) {
        this.parent = parent;
    }

    @Override
    public void init() {
        var config = SwanSongOrnithe.CONFIG;
        var btnWidth = 120;
        var btnX = width - btnWidth - 10;
        var listWidth = width - btnWidth - 20;
        packs = new PackList(listWidth);

        buttons.add(new OptionButton(0, btnX, 30, btnWidth, "RenderQuality", config.renderQuality, QUALITIES));
        buttons.add(new OptionButton(1, btnX, 50, btnWidth, "ShadowQuality", config.shadowQuality, QUALITIES));
        buttons.add(new OptionButton(2, btnX, 70, btnWidth, "HandDepth", config.handDepth, HAND_DEPTHS));

        var folderWidth = Math.min(150, listWidth / 2 - 10);
        buttons.add(new ButtonWidget(BUTTON_ID_BROWSE,
                                     listWidth / 4 - folderWidth / 2,
                                     height - 25,
                                     folderWidth,
                                     20,
                                     I18n.translate("gui.swansong.shaders.browse")));
        buttons.add(new ButtonWidget(BUTTON_ID_EXIT,
                                     listWidth / 4 * 3 - folderWidth / 2,
                                     height - 25,
                                     folderWidth,
                                     20,
                                     I18n.translate("gui.swansong.shaders.exit")));
        optionsButton = new ButtonWidget(BUTTON_ID_OPTIONS,
                                         btnX,
                                         height - 25,
                                         btnWidth,
                                         20,
                                         I18n.translate("gui.swansong.shaders.options"));
        optionsButton.active = SwanSongOrnithe.ENGINE.configScreen() != null;
        buttons.add(optionsButton);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (!button.active) {
            return;
        }
        if (button instanceof OptionButton option) {
            option.cycle();
            return;
        }
        switch (button.id) {
            case BUTTON_ID_BROWSE -> openShaderPacksDir();
            case BUTTON_ID_EXIT -> minecraft.openScreen(parent);
            case BUTTON_ID_OPTIONS -> {
                var configScreen = SwanSongOrnithe.ENGINE.configScreen();
                if (configScreen != null) {
                    minecraft.openScreen(new ShaderOptionsScreen(this, configScreen));
                }
            }
            default -> packs.buttonClicked(button);
        }
    }

    @Override
    public void handleMouse() {
        super.handleMouse();
        packs.handleMouse();
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        renderBackground();
        packs.render(mouseX, mouseY, tickDelta);
        drawCenteredString(textRenderer, I18n.translate("gui.swansong.shaders.title"), width / 2, 15, 0xFFFFFF);
        var env = EnvInfo.get();
        var info = "OpenGL: " + env.glVersionStr + ", " + env.glVendorStr + ", " + env.glRendererStr;
        if (textRenderer.getWidth(info) < width - 5) {
            drawCenteredString(textRenderer, info, width / 2, height - 40, 0x808080);
        } else {
            drawString(textRenderer, info, 5, height - 40, 0x808080);
        }
        super.render(mouseX, mouseY, tickDelta);
    }

    @Override
    public void tick() {
        optionsButton.active = SwanSongOrnithe.ENGINE.configScreen() != null;
        if (--updateTimer <= 0) {
            packs.update();
            updateTimer += 20;
        }
    }

    private static void openShaderPacksDir() {
        var dir = SwanSongOrnithe.ENGINE.shaderPacks()
                                        .getShaderPacksDir()
                                        .toAbsolutePath()
                                        .toString();
        var os = System.getProperty("os.name")
                       .toLowerCase(Locale.ROOT);
        var command = os.contains("mac") ? new String[]{"open", dir}
                                         : os.contains("win") ? new String[]{"explorer", dir}
                                                              : new String[]{"xdg-open", dir};
        try {
            new ProcessBuilder(command).start();
        } catch (IOException e) {
            Share.log.error("Failed to open shaderpacks directory", e);
        }
    }

    private static final class OptionButton extends ButtonWidget {
        private final String key;
        private final FloatOption option;
        private final float[] values;

        OptionButton(int id, int x, int y, int width, String key, FloatOption option, float[] values) {
            super(id, x, y, width, 20, "");
            this.key = key;
            this.option = option;
            this.values = values;
            updateMessage();
        }

        void cycle() {
            var next = values[0];
            for (int i = 0; i < values.length; i++) {
                if (values[i] == option.get()) {
                    next = values[(i + 1) % values.length];
                }
            }
            option.set(next);
            ConfigManager.save(SwanSongOrnithe.CONFIG);
            updateMessage();
        }

        private void updateMessage() {
            message = I18n.translate("config.swansong.shaders." + key) + ": " + option.get() + "x";
        }
    }

    private final class PackList extends ListWidget {
        private List<String> names;
        private int selectedIndex;

        PackList(int width) {
            super(ShadersScreen.this.minecraft, width, ShadersScreen.this.height, 30, ShadersScreen.this.height - 50, 18);
            update();
            var selectedY = selectedIndex * entryHeight;
            var middle = (maxY - minY) / 2;
            if (selectedY > middle) {
                scroll(selectedY - middle);
            }
            setScrollButtonIds(7, 8);
        }

        void update() {
            var manager = SwanSongOrnithe.ENGINE.shaderPacks();
            manager.refreshShaderPackNames();
            names = manager.getShaderPackNames();
            selectedIndex = 0;
            var current = manager.getCurrentShaderPackName();
            for (int i = 0; i < names.size(); i++) {
                if (Objects.equals(names.get(i), current)) {
                    selectedIndex = i;
                    break;
                }
            }
        }

        @Override
        public int getRowWidth() {
            return width - 20;
        }

        @Override
        protected int size() {
            return names.size();
        }

        @Override
        protected void entryClicked(int index, boolean doubleClick, int mouseX, int mouseY) {
            if (index != selectedIndex) {
                selectedIndex = index;
                SwanSongOrnithe.ENGINE.shaderPacks()
                                      .setShaderPackByName(names.get(index));
            }
        }

        @Override
        protected boolean isEntrySelected(int index) {
            return index == selectedIndex;
        }

        @Override
        protected int getScrollbarPosition() {
            return width - 6;
        }

        @Override
        protected int getHeight() {
            return size() * 18;
        }

        @Override
        protected void renderBackground() {
        }

        @Override
        protected void renderEntry(int index, int x, int y, int rowHeight, int mouseX, int mouseY) {
            var label = names.get(index);
            int color;
            if (label.equals(ShaderPackManager.DISABLED_SHADER_PACK_NAME)) {
                label = I18n.translate("options.off");
                color = 0xE04040;
            } else if (SwanSongOrnithe.ENGINE.shaderPacks()
                                             .isReferencePack(label)) {
                label = I18n.translate("gui.swansong.shaders.default");
                color = 0xA0A0A0;
            } else {
                color = 0xE0E0E0;
            }
            drawCenteredString(textRenderer, label, width / 2, y + 1, color);
        }
    }
}
