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

import com.ventooth.swansong.shader.config.ConfigEntry;

import java.awt.Color;

public final class Colors {
    private Colors() {
    }

    public static final int COLOR_DEFAULT_RGB = new Color(0xDDDDDD).getRGB();
    public static final int COLOR_RED_RGB = new Color(0xFF2020).getRGB();
    public static final int COLOR_RECT_BORDER = new Color(0x868DA9).getRGB();
    public static final int COLOR_RECT_FILL = new Color(0x080D3D).getRGB();
    public static final String COLOR_RED_TEXT = "§c";
    public static final String COLOR_GREEN_TEXT = "§a";
    public static final String COLOR_BLUE_TEXT = "§b";
    public static final String COLOR_YELLOW_TEXT = "§e";

    public static String forConfig(ConfigEntry.Mutable cfg) {
        return (cfg.isDefault() ? cfg.isModified() ? COLOR_YELLOW_TEXT : ""
                                : cfg.isModified() ? COLOR_GREEN_TEXT : COLOR_BLUE_TEXT);
    }
}
