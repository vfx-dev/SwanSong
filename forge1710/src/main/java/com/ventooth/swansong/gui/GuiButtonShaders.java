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

import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.resources.ShaderPackManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.Locale;

import java.util.List;

public abstract class GuiButtonShaders extends GuiButton {
    protected final Locale locale;

    public GuiButtonShaders(int id,
                            int posX,
                            int posY,
                            int stepY,
                            int width,
                            int height,
                            Translatable lang,
                            Locale locale) {
        super(id, posX, posY + (stepY * id), width, height, lang.format(locale));
        this.locale = locale;
    }

    public abstract void onClick();

    public void update() {
        // TODO: Override this to toggle if a button is enabled or nah
    }

    public static void addButtons(List<GuiButton> buttonList,
                                  int posX,
                                  int posY,
                                  int stepY,
                                  int width,
                                  int height,
                                  Locale locale) {
        buttonList.add(new AntiAliasing(posX, posY, stepY, width, height, locale));
        buttonList.add(new NormalMapping(posX, posY, stepY, width, height, locale));
        buttonList.add(new SpecularMapping(posX, posY, stepY, width, height, locale));
        buttonList.add(new RenderQuality(posX, posY, stepY, width, height, locale));
        buttonList.add(new ShadowQuality(posX, posY, stepY, width, height, locale));
        buttonList.add(new HandDepth(posX, posY, stepY, width, height, locale));
        buttonList.add(new OldHandLight(posX, posY, stepY, width, height, locale));
        buttonList.add(new OldHandDepth(posX, posY, stepY, width, height, locale));
    }

    public static final class AntiAliasing extends GuiButtonShaders {
        public static int ID = 0;

        private AntiAliasing(int posX, int posY, int stepY, int width, int height, Locale locale) {
            super(ID, posX, posY, stepY, width, height, ShadersConfig.AntiAliasing, locale);
        }

        @Override
        public void onClick() {
            ShadersConfig.AntiAliasing = nextValue(ShadersConfig.AntiAliasingCfg.values(), ShadersConfig.AntiAliasing);
            this.displayString = ShadersConfig.AntiAliasing.format(locale);
            ShaderPackManager.saveShaderSettings();
        }
    }

    public static final class NormalMapping extends GuiButtonShaders {
        public static int ID = 1;

        private NormalMapping(int posX, int posY, int stepY, int width, int height, Locale locale) {
            super(ID, posX, posY, stepY, width, height, ShadersConfig.NormalMapping, locale);
        }

        @Override
        public void onClick() {
            ShadersConfig.NormalMapping = nextValue(ShadersConfig.NormalMappingCfg.values(),
                                                    ShadersConfig.NormalMapping);
            this.displayString = ShadersConfig.NormalMapping.format(locale);
            ShaderPackManager.saveShaderSettings();
            Minecraft.getMinecraft()
                     .refreshResources();
        }
    }

    public static final class SpecularMapping extends GuiButtonShaders {
        public static int ID = 2;

        private SpecularMapping(int posX, int posY, int stepY, int width, int height, Locale locale) {
            super(ID, posX, posY, stepY, width, height, ShadersConfig.SpecularMapping, locale);
        }

        @Override
        public void onClick() {
            ShadersConfig.SpecularMapping = nextValue(ShadersConfig.SpecularMappingCfg.values(),
                                                      ShadersConfig.SpecularMapping);
            this.displayString = ShadersConfig.SpecularMapping.format(locale);
            ShaderPackManager.saveShaderSettings();
            Minecraft.getMinecraft()
                     .refreshResources();
        }
    }

    public static final class RenderQuality extends GuiButtonShaders {
        public static int ID = 3;

        private RenderQuality(int posX, int posY, int stepY, int width, int height, Locale locale) {
            super(ID, posX, posY, stepY, width, height, ShadersConfig.RenderQuality, locale);
        }

        @Override
        public void onClick() {
            ShadersConfig.RenderQuality = nextValue(ShadersConfig.RenderQualityCfg.values(),
                                                    ShadersConfig.RenderQuality);
            this.displayString = ShadersConfig.RenderQuality.format(locale);
            ShaderPackManager.saveShaderSettings();
        }
    }

    public static final class ShadowQuality extends GuiButtonShaders {
        public static int ID = 4;

        private ShadowQuality(int posX, int posY, int stepY, int width, int height, Locale locale) {
            super(ID, posX, posY, stepY, width, height, ShadersConfig.ShadowQuality, locale);
        }

        @Override
        public void onClick() {
            ShadersConfig.ShadowQuality = nextValue(ShadersConfig.ShadowQualityCfg.values(),
                                                    ShadersConfig.ShadowQuality);
            this.displayString = ShadersConfig.ShadowQuality.format(locale);
            ShaderPackManager.saveShaderSettings();
        }
    }

    public static final class HandDepth extends GuiButtonShaders {
        public static int ID = 5;

        private HandDepth(int posX, int posY, int stepY, int width, int height, Locale locale) {
            super(ID, posX, posY, stepY, width, height, ShadersConfig.HandDepth, locale);
        }

        @Override
        public void onClick() {
            ShadersConfig.HandDepth = nextValue(ShadersConfig.HandDepthCfg.values(), ShadersConfig.HandDepth);
            this.displayString = ShadersConfig.HandDepth.format(locale);
            ShaderPackManager.saveShaderSettings();
        }
    }

    public static final class OldHandLight extends GuiButtonShaders {
        public static int ID = 6;

        private OldHandLight(int posX, int posY, int stepY, int width, int height, Locale locale) {
            super(ID, posX, posY, stepY, width, height, ShadersConfig.OldHandLight, locale);
        }

        @Override
        public void onClick() {
            ShadersConfig.OldHandLight = nextValue(ShadersConfig.OldHandLightCfg.values(), ShadersConfig.OldHandLight);
            this.displayString = ShadersConfig.OldHandLight.format(locale);
            ShaderPackManager.saveShaderSettings();
        }
    }

    public static final class OldHandDepth extends GuiButtonShaders {
        public static int ID = 7;

        private OldHandDepth(int posX, int posY, int stepY, int width, int height, Locale locale) {
            super(ID, posX, posY, stepY, width, height, ShadersConfig.OldHandDepth, locale);
        }

        @Override
        public void onClick() {
            ShadersConfig.OldHandDepth = nextValue(ShadersConfig.OldHandDepthCfg.values(), ShadersConfig.OldHandDepth);
            this.displayString = ShadersConfig.OldHandDepth.format(locale);
            ShaderPackManager.saveShaderSettings();
        }
    }

    private static <T extends Enum<T>> T nextValue(T[] values, T current) {
        return values[(current.ordinal() + 1) % values.length];
    }
}
