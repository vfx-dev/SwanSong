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

@Config.Comment("Compatibility Fixes")
@Config(modid = Tags.MOD_ID,
        category = "02_compat")
@Config.LangKey
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompatConfig {
    @Config.Name("NEI_OverlayFix")
    @Config.Comment("NotEnoughItems: Fixes broken chunk grid and mob spawn overlays (F7/F9)")
    @Config.LangKey("config.swansong.compat.NEI_OverlayFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean NEI_OverlayFix;

    @Config.Name("OC_ScreenFix")
    @Config.Comment("OpenComputers: Fixes flickering screens")
    @Config.LangKey("config.swansong.compat.OC_ScreenFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean OC_ScreenFix;

    @Config.Name("OC_HologramFix")
    @Config.Comment("OpenComputers: Fixes dark/flickering holograms")
    @Config.LangKey("config.swansong.compat.OC_HologramFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean OC_HologramFix;

    @Config.Name("OC_RobotFix")
    @Config.Comment("OpenComputers: Fixes funky robot nametags")
    @Config.LangKey("config.swansong.compat.OC_RobotFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean OC_RobotFix;

    @Config.Name("AV_CosmicShader")
    @Config.Comment("Avaritia: Fancy (and hopefully) non-flickering Infinity Gear")
    @Config.LangKey("config.swansong.compat.AV_CosmicShader")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean AV_CosmicShader;

    @Config.Name("AV_HeavenArrowFix")
    @Config.Comment("Avaritia: Fixes the arrows from the 'Longbow of the Heavens' not being fullbright")
    @Config.LangKey("config.swansong.compat.AV_HeavenArrowFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean AV_HeavenArrowFix;

    @Config.Name("TC_EldritchObeliskFix")
    @Config.Comment("ThaumCraft: Fixes the portal effect on the eldritch obelisk")
    @Config.LangKey("config.swansong.compat.TC_EldritchObeliskFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean TC_EldritchObeliskFix;

    @Config.Name("TC_VisOverlayFix")
    @Config.Comment("ThaumCraft: Fixes the vis info overlay on nodes and other stuff")
    @Config.LangKey("config.swansong.compat.TC_VisOverlayFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean TC_VisOverlayFix;

    @Config.Name("THEX_TesseractStarfieldFix")
    @Config.Comment("Thermal Expansion: Fixes the starfield effect on the Tesseract")
    @Config.LangKey("config.swansong.compat.THEX_TesseractStarfieldFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean THEX_TesseractStarfieldFix;

    @Config.Name("JM_WaypointBeaconFix")
    @Config.Comment("JourneyMap: Fixes waypoint beacon text flickering and bloom bleed")
    @Config.LangKey("config.swansong.compat.JM_WaypointBeaconFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean JM_WaypointBeaconFix;

    @Config.Name("NTM_SkyFix")
    @Config.Comment("HBM NTM Space: Fixes the sky renderer")
    @Config.LangKey("config.swansong.compat.NTM_SkyFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean NTM_SkyFix;

    @Config.Name("MC_GuiLightFix")
    @Config.Comment("Minecraft: Fixes guis turning \"dark\" in some modpacks")
    @Config.LangKey("config.swansong.compat.MC_GuiLightFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean MC_GuiLightFix;

    @Config.Name("MC_BoundingBoxFixes")
    @Config.Comment({"Minecraft: Fixes render bounding boxes for some tile entities (end portals, sign)",
                     "Improves the game performance SIGNIFICANTLY if you have a lot of signs on your base"})
    @Config.LangKey("config.swansong.compat.MC_BoundingBoxFixes")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean MC_BoundingBoxFixes;

    @Config.Ignore // TODO: Wire config
    public static boolean MC_IncreasedMaxTextureSize = true;

    @Config.Ignore // TODO: Wire config
    public static boolean MC_FixReadImageException = true;

    @Config.Ignore // TODO: Wire config
    public static boolean MC_ProperTextureDelete = true;

    @Config.Ignore // TODO: Wire config
    public static boolean MC_SpriteTranslucencyFix = true;

    @Config.Ignore // TODO: Wire config
    public static boolean DA_TextRenderingColorFix = true; // This was breaking the popup box stuff

    @Config.Name("MC_LightMapCoordClamp")
    @Config.Comment({"Minecraft: Clamps fixed coordinates being set for the lightmap to: 0F-240F",
                     "Fixes problems where certain things end up rendering pitch black"})
    @Config.LangKey("config.swansong.compat.MC_LightMapCoordClamp")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean MC_LightMapCoordClamp;

    @Config.Name("Botania_SkyFix")
    @Config.Comment("Fixes the broken skybox in botania Garden of Glass")
    @Config.LangKey("config.swansong.compat.Botania_SkyFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean Botania_SkyFix;

    @Config.Name("Galacticraft_SkyFix")
    @Config.Comment("Fixes the broken skybox in GalactiCraft")
    @Config.LangKey("config.swansong.compat.Galacticraft_SkyFix")
    @Config.DefaultBoolean(true)
    @Config.RequiresMcRestart
    public static boolean Galacticraft_SkyFix;
}
