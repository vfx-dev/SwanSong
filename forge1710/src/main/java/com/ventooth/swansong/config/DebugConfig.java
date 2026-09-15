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

@Config.Comment("Debug")
@Config(modid = Tags.MOD_ID,
        category = "01_debug")
@Config.LangKey
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugConfig {
    static {
        Configs.poke();
    }

    @Config.Name("ApplyVenterceptorLogFilters")
    @Config.Comment("Filter out spammy messages if Venterceptor GLDebug logging is in use.")
    @Config.LangKey("config.swansong.debug.ApplyVenterceptorLogFilters")
    @Config.DefaultBoolean(true)
    public static boolean ApplyVenterceptorLogFilters;

    @Config.Name("UseGLObjectLabels")
    @Config.Comment("If we should enable (or perhaps, disable?) tagged labels on created objects.")
    @Config.LangKey("config.swansong.debug.UseGLObjectLabels")
    @Config.DefaultBoolean(false)
    public static boolean UseGLObjectLabels;

    @Config.Name("UseGLDebugGroups")
    @Config.Comment("Useful little grouping for marking regions where calls and stuff happen.")
    @Config.LangKey("config.swansong.debug.UseGLDebugGroups")
    @Config.DefaultBoolean(false)
    public static boolean UseGLDebugGroups;

    @Config.DefaultBoolean(false)
    public static boolean GLDebugMarkers; // TODO: Desc

    @Config.DefaultBoolean(false)
    public static boolean DumpCompiledUniforms; // TODO: Desc
}

