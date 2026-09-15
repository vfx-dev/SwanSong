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

import com.falsepattern.lib.config.Config;
import com.ventooth.swansong.Tags;

@Config.Comment("Zoom options")
@Config(modid = Tags.MOD_ID,
        category = "zoom")
@Config.LangKey
public class ZoomConfig {
    @Config.Comment("Should zooming make a sound?")
    @Config.Name("Sound")
    @Config.LangKey
    @Config.DefaultBoolean(true)
    public static boolean Sound;
}
