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
import com.ventooth.swansong.gui.Translatable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Config.Comment(Tags.MOD_NAME + " Shader Settings")
@Config(modid = Tags.MOD_ID,
        customPath = "../options_shaders.cfg",
        category = "shaders")
@Config.LangKey
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadersConfig {
    static {
        Configs.poke();
    }

    @Config.Name("CurrentShaderPack")
    @Config.Comment("TODO: CurrentShaderPack Desc")
    @Config.LangKey("config.swansong.shaders.CurrentShaderPack")
    @Config.DefaultString("(disabled)")
    public static String CurrentShaderPack;

    @Config.Name("AntiAliasing")
    @Config.Comment("TODO: AntiAliasing Desc")
    @Config.LangKey("config.swansong.shaders.AntiAliasing")
    @Config.DefaultEnum("DISABLED")
    public static AntiAliasingCfg AntiAliasing;

    @Config.Name("NormalMapping")
    @Config.Comment("TODO: NormalMapping Desc")
    @Config.LangKey("config.swansong.shaders.NormalMapping")
    @Config.DefaultEnum("ENABLED")
    public static NormalMappingCfg NormalMapping;

    @Config.Name("SpecularMapping")
    @Config.Comment("TODO: SpecularMapping Desc")
    @Config.LangKey("config.swansong.shaders.SpecularMapping")
    @Config.DefaultEnum("ENABLED")
    public static SpecularMappingCfg SpecularMapping;

    @Config.Name("RenderQuality")
    @Config.Comment("TODO: RenderQuality Desc")
    @Config.LangKey("config.swansong.shaders.RenderQuality")
    @Config.DefaultEnum("MULT_1_0")
    public static RenderQualityCfg RenderQuality;

    @Config.Name("ShadowQuality")
    @Config.Comment("TODO: ShadowQuality Desc")
    @Config.LangKey("config.swansong.shaders.ShadowQuality")
    @Config.DefaultEnum("MULT_1_0")
    public static ShadowQualityCfg ShadowQuality;

    @Config.Name("HandDepth")
    @Config.Comment("TODO: HandDepth Desc")
    @Config.LangKey("config.swansong.shaders.HandDepth")
    @Config.DefaultEnum("MULT_1_0")
    public static HandDepthCfg HandDepth;

    @Config.Name("OldHandLight")
    @Config.Comment("TODO: OldHandLight Desc")
    @Config.LangKey("config.swansong.shaders.OldHandLight")
    @Config.DefaultEnum("AUTO")
    public static OldHandLightCfg OldHandLight;

    @Config.Name("OldHandDepth")
    @Config.Comment("TODO: OldHandDepth Desc")
    @Config.LangKey("config.swansong.shaders.OldHandDepth")
    @Config.DefaultEnum("AUTO")
    public static OldHandDepthCfg OldHandDepth;

    @Config.DefaultBoolean(false)
    public static boolean LetMeUseDepthOfFieldPlease; // TODO: Desc

    @Config.Name("enableReferenceShaderPack")
    @Config.Comment({
            "Enable this if you want to use the built-in shaderpack.",
            "You should not do this unless FalsePattern/Ven told you to for testing," +
            "it WILL decrease your FPS if you use that shaderpack"
    })
    @Config.LangKey("config.swansong.shaders.EnableReferenceShaderpack")
    @Config.DefaultBoolean(false)
    public static boolean enableReferenceShaderPack;

    @RequiredArgsConstructor
    @Accessors(fluent = true,
               chain = false)
    public enum AntiAliasingCfg implements Translatable.BuiltinLocale {
        DISABLED("option.swansong.shaders.AntiAliasingCfg.DISABLED"),
        FXAA_2X("option.swansong.shaders.AntiAliasingCfg.FXAA_2X"),
        FXAA_4X("option.swansong.shaders.AntiAliasingCfg.FXAA_4X"),
        ;

        @Getter
        private final String langKey;
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true,
               chain = false)
    public enum NormalMappingCfg implements Translatable.BuiltinLocale {
        ENABLED("option.swansong.shaders.NormalMappingCfg.ENABLED", true),
        DISABLED("option.swansong.shaders.NormalMappingCfg.DISABLED", false),
        ;

        @Getter
        private final String langKey;
        public final boolean value;
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true,
               chain = false)
    public enum SpecularMappingCfg implements Translatable.BuiltinLocale {
        ENABLED("option.swansong.shaders.SpecularMappingCfg.ENABLED", true),
        DISABLED("option.swansong.shaders.SpecularMappingCfg.DISABLED", false),
        ;

        @Getter
        private final String langKey;
        public final boolean value;
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true,
               chain = false)
    public enum RenderQualityCfg implements Translatable.BuiltinLocale {
        MULT_0_5("option.swansong.shaders.RenderQualityCfg.MULT_0_5", 0.5F),
        MULT_0_7("option.swansong.shaders.RenderQualityCfg.MULT_0_7", 0.7F),
        MULT_1_0("option.swansong.shaders.RenderQualityCfg.MULT_1_0", 1.0F),
        MULT_1_5("option.swansong.shaders.RenderQualityCfg.MULT_1_5", 1.5F),
        MULT_2_0("option.swansong.shaders.RenderQualityCfg.MULT_2_0", 2.0F),
        ;

        @Getter
        private final String langKey;
        private final float value;

        public float get() {
            return value;
        }
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true,
               chain = false)
    public enum ShadowQualityCfg implements Translatable.BuiltinLocale {
        MULT_0_5("option.swansong.shaders.ShadowQualityCfg.MULT_0_5", 0.5F),
        MULT_0_7("option.swansong.shaders.ShadowQualityCfg.MULT_0_7", 0.7F),
        MULT_1_0("option.swansong.shaders.ShadowQualityCfg.MULT_1_0", 1.0F),
        MULT_1_5("option.swansong.shaders.ShadowQualityCfg.MULT_1_5", 1.5F),
        MULT_2_0("option.swansong.shaders.ShadowQualityCfg.MULT_2_0", 2.0F),
        ;

        @Getter
        private final String langKey;
        private final float value;

        public float get() {
            return value;
        }
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true,
               chain = false)
    public enum HandDepthCfg implements Translatable.BuiltinLocale {
        MULT_0_5("option.swansong.shaders.HandDepthCfg.MULT_0_5", 0.5F),
        MULT_1_0("option.swansong.shaders.HandDepthCfg.MULT_1_0", 1.0F),
        MULT_2_0("option.swansong.shaders.HandDepthCfg.MULT_2_0", 2.0F),
        ;

        @Getter
        private final String langKey;
        private final float value;

        public float get() {
            return value;
        }
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true,
               chain = false)
    public enum OldHandLightCfg implements Translatable.BuiltinLocale {
        AUTO("option.swansong.shaders.OldHandLightCfg.AUTO", null),
        ENABLED("option.swansong.shaders.OldHandLightCfg.ENABLED", true),
        DISABLED("option.swansong.shaders.OldHandLightCfg.DISABLED", false),
        ;

        @Getter
        private final String langKey;
        private final @Nullable Boolean value;

        public boolean getOrDefault(boolean def) {
            return value != null ? value : def;
        }
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true,
               chain = false)
    public enum OldHandDepthCfg implements Translatable.BuiltinLocale {
        AUTO("option.swansong.shaders.OldHandDepthCfg.AUTO", null),
        ENABLED("option.swansong.shaders.OldHandDepthCfg.ENABLED", true),
        DISABLED("option.swansong.shaders.OldHandDepthCfg.DISABLED", false),
        ;

        @Getter
        private final String langKey;
        private final @Nullable Boolean value;

        public boolean getOrDefault(boolean def) {
            return value != null ? value : def;
        }
    }
}

