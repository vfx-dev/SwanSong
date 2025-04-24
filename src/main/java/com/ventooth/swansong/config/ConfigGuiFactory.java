/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.config;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.SimpleGuiFactory;
import com.ventooth.swansong.Tags;
import lombok.NoArgsConstructor;

import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.client.config.GuiConfig;

import static com.ventooth.swansong.config.Configs.cfgElements;

@NoArgsConstructor
@SuppressWarnings("unused")
public final class ConfigGuiFactory implements SimpleGuiFactory {
    private static final String CONFIG_GUI_NAME = Tags.MOD_NAME + " Config";

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return MyGuiConfig.class;
    }

    public static final class MyGuiConfig extends GuiConfig {
        public MyGuiConfig(GuiScreen parent) throws ConfigException {
            super(parent, cfgElements(), Tags.MOD_ID, false, false, CONFIG_GUI_NAME);
        }
    }
}