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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Config.Comment("Toggles for all SwanSong modules")
@Config(modid = Tags.MOD_ID,
        category = "00_modules")
@Config.LangKey
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModuleConfig {
    static {
        Configs.poke();
    }

    @Config.Name("Debug")
    @Config.Comment("Debug helpers of all sorts")
    @Config.LangKey("config.swansong.modules.Debug")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean Debug;

    @Config.Name("ThreadedScreenshots")
    @Config.Comment("Just a neat little thing to make screenshots faster")
    @Config.LangKey("config.swansong.modules.ThreadedScreenshots")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean ThreadedScreenshots;

    @Config.Name("FunkyZoom")
    @Config.Comment("Like imagine you can squint and see further")
    @Config.LangKey("config.swansong.modules.FunkyZoom")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean FunkyZoom;

    @Config.Name("FastTextRender")
    @Config.Comment("Faster text rendering (incompatible with SmoothFont)")
    @Config.LangKey("config.swansong.modules.FastTextRender")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean FastTextRender;
}
