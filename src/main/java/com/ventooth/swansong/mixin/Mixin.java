/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin;

import com.falsepattern.lib.mixin.v2.MixinHelper;
import com.falsepattern.lib.mixin.v2.SidedMixins;
import com.falsepattern.lib.mixin.v2.TaggedMod;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import com.ventooth.swansong.Tags;
import com.ventooth.swansong.config.CompatConfig;
import com.ventooth.swansong.config.ModuleConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.intellij.lang.annotations.Language;

import java.util.function.BooleanSupplier;

import static com.falsepattern.lib.mixin.v2.MixinHelper.avoid;
import static com.falsepattern.lib.mixin.v2.MixinHelper.builder;
import static com.falsepattern.lib.mixin.v2.MixinHelper.require;
import static com.ventooth.swansong.mixin.TargetMod.Avaritia;
import static com.ventooth.swansong.mixin.TargetMod.DragonAPI;
import static com.ventooth.swansong.mixin.TargetMod.FoamFix;
import static com.ventooth.swansong.mixin.TargetMod.JourneyMap;
import static com.ventooth.swansong.mixin.TargetMod.ModernWarfare;
import static com.ventooth.swansong.mixin.TargetMod.NotFine;
import static com.ventooth.swansong.mixin.TargetMod.NuclearTech;
import static com.ventooth.swansong.mixin.TargetMod.OpenComputers;
import static com.ventooth.swansong.mixin.TargetMod.RotaryCraft;
import static com.ventooth.swansong.mixin.TargetMod.SmoothFont;
import static com.ventooth.swansong.mixin.TargetMod.Thaumcraft;
import static com.ventooth.swansong.mixin.TargetMod.ThermalExpansion;
import static com.ventooth.swansong.mixin.TargetMod.Zume;

@SuppressWarnings("UnstableApiUsage")
@Accessors(fluent = false)
@RequiredArgsConstructor
public enum Mixin implements IMixins {
    //@formatter:off
    Base(Phase.EARLY,
         client("GuiVideoSettingsMixin",
                "EntityRendererMixin",
                "TessellatorMixin",
                "TessellatorAccessor",
                "WorldRendererMixin")),
    Base_NoFoamFix(Phase.EARLY,
                   avoid(FoamFix),
                   client("TessellatorMixin_AvoidFoamFix")),
    Base_FoamFix(Phase.EARLY,
                 require(FoamFix),
                 client("TessellatorMixin_RequireFoamFix")),
    Base_NotFine(Phase.EARLY,
                 require(NotFine),
                 client("compat.notfine.GuiCustomMenuMixin",
                        "compat.notfine.NotFineGameOptionPagesMixin")),

    Texture(Phase.EARLY,
            client("texture.TextureAtlasSpriteMixin",
                   "texture.TextureClockMixin",
                   "texture.TextureCompassMixin",
                   "texture.TextureMapMixin",
                   "texture.AbstractTextureMixin",
                   "texture.SimpleTextureMixin",
                   "texture.TextureManagerMixin",
                   "texture.OpenGlHelperMixin")),

    Hooks(Phase.EARLY,
          client("hooks.FontRendererMixin",
                 "hooks.EntityRendererMixin",
                 "hooks.ItemRendererMixin",
                 "hooks.RenderFishMixin",
                 "hooks.RenderGlobalMixin",
                 "hooks.RenderManagerMixin",
                 "hooks.RenderLivingMixin",
                 "hooks.RenderMixin",
                 "hooks.TileEntityBeaconRendererMixin",
                 "hooks.RendererLivingEntityMixin",
                 "hooks.MixinRenderBlocks",
                 "hooks.BlockMixin",
                 "hooks.RenderEndPortalMixin",
                 "hooks.MinecraftMixin",
                 "hooks.GameSettingsMixin",
                 "hooks.GameSettings_OptionsMixin",
                 "hooks.RenderLightningBoltMixin",
                 "hooks.RenderDragonMixin",
                 "hooks.QuadComparatorMixin",
                 "hooks.GuiOptionsRowListMixin")),
    Hooks_AvoidModernWarfare(Phase.EARLY,
                             avoid(ModernWarfare),
                             client("hooks.RendererLivingEntityMixin_AvoidModernWarfare")),
    Hooks_RequireModernWarfare(Phase.EARLY,
                               require(ModernWarfare),
                               client("hooks.RendererLivingEntityMixin_RequireModernWarfare")),

    Debug(Phase.EARLY,
          () -> ModuleConfig.Debug,
          client("debug.MinecraftMixin",
                 "debug.ProfilerMixin")),

    Screenshot(Phase.EARLY,
               () -> ModuleConfig.ThreadedScreenshots,
               client("screenshot.MinecraftMixin")),

    Zoom(Phase.EARLY,
         () -> ModuleConfig.FunkyZoom,
         avoid(Zume),
         client("zoom.MinecraftMixin",
                "zoom.EntityRendererMixin")),

    FastTextRender(Phase.EARLY,
                   () -> ModuleConfig.FastTextRender,
                   avoid(SmoothFont),
                   client("FastTextRender.FontRendererMixin",
                          "FastTextRender.GuiIngameForgeMixin")),

    Compat_OC_HologramFix(Phase.EARLY,
                          () -> CompatConfig.OC_HologramFix,
                          require(OpenComputers),
                          client("compat.oc.HologramRendererMixin")),
    Compat_OC_RobotFix(Phase.EARLY,
                       () -> CompatConfig.OC_RobotFix,
                       require(OpenComputers),
                       client("compat.oc.RobotRendererMixin")),
    Compat_OC_ScreenFix(Phase.EARLY,
                        () -> CompatConfig.OC_ScreenFix,
                        require(OpenComputers),
                        client("compat.oc.TextureFontRendererMixin")),

    // TODO: Order these better, splitting up the configs by what they fix
    Compat_DA_TextRenderingColorFix(Phase.EARLY,
                                    () -> CompatConfig.DA_TextRenderingColorFix,
                                    require(DragonAPI),
                                    client("compat.dragonapi.DelegateFontRendererMixin",
                                           "compat.dragonapi.ReikaRenderHelperMixin")),
    Compat_RC_SideOverlayAlphaFix(Phase.EARLY,
                                  () -> CompatConfig.DA_TextRenderingColorFix,
                                  require(RotaryCraft),
                                  client("compat.rotarycraft.RotaryTERendererMixin",
                                         "compat.rotarycraft.IORendererMixin")),

    Compat_AV_CosmicShader(Phase.LATE,
                           () -> CompatConfig.AV_CosmicShader,
                           require(Avaritia),
                           client("compat.avaritia.ShaderHelperMixin")),

    Compat_AV_HeavenArrowFix(Phase.LATE,
                             () -> CompatConfig.AV_HeavenArrowFix,
                             require(Avaritia),
                             client("compat.avaritia.RenderHeavenArrowMixin")),

    Compat_TC_EldritchObeliskFix(Phase.LATE,
                                 () -> CompatConfig.TC_EldritchObeliskFix,
                                 require(Thaumcraft),
                                 client("compat.thaumcraft.TileEldritchObeliskRendererMixin")),

    Compat_TC_VisOverlayFix(Phase.LATE,
                            () -> CompatConfig.TC_VisOverlayFix,
                            require(Thaumcraft),
                            client("compat.thaumcraft.RenderEventHandlerMixin")),

    Compat_TC_CultistFloatyLineFix(Phase.LATE,
                                   () -> CompatConfig.TC_CultistFloatyLineFix,
                                   require(Thaumcraft),
                                   client("compat.thaumcraft.EntityCultistClericMixin",
                                          "compat.thaumcraft.RenderCultistMixin")),

    Compat_THEX_TesseractStarfieldFix(Phase.LATE,
                                      () -> CompatConfig.THEX_TesseractStarfieldFix,
                                      require(ThermalExpansion),
                                      client("compat.thermal.RenderTesseractStarfieldMixin")),

    Compat_JM_WaypointBeaconFix(Phase.LATE,
                                () -> CompatConfig.JM_WaypointBeaconFix,
                                require(JourneyMap),
                                client("compat.journeymap.DrawUtilMixin",
                                       "compat.journeymap.RenderHelperMixin")),

    Compat_MC_GuiLightFix(Phase.EARLY,
                          () -> CompatConfig.MC_GuiLightFix,
                          client("compat.mc.GuiLightFix.GuiIngameForgeMixin")),

    Compat_MC_BoundingBox(Phase.EARLY,
                          () -> CompatConfig.MC_BoundingBoxFixes,
                          client("compat.mc.BoundingBoxFixes.TileEntityEndPortalMixin",
                                 "compat.mc.BoundingBoxFixes.TileEntitySignMixin")),

    Compat_MC_LightMapCoordClamp(Phase.EARLY,
                                 () -> CompatConfig.MC_LightMapCoordClamp,
                                 client("compat.mc.LightMapCoordClamp.OpenGlHelperMixin")),

    Compat_MC_IncreasedMaxTextureSize(Phase.EARLY,
                                      () -> CompatConfig.MC_IncreasedMaxTextureSize,
                                      client("compat.mc.IncreasedMaxTextureSize.MinecraftMixin")),

    Compat_MC_FixReadImageException(Phase.EARLY,
                                    () -> CompatConfig.MC_FixReadImageException,
                                    client("compat.mc.FixReadImageException.TextureUtilMixin")),

    Compat_MC_ProperTextureDelete(Phase.EARLY,
                                  () -> CompatConfig.MC_ProperTextureDelete,
                                  client("compat.mc.ProperTextureDelete.TextureManagerMixin")),

    Compat_MC_SpriteTranslucencyFix(Phase.EARLY,
                                    () -> CompatConfig.MC_SpriteTranslucencyFix,
                                    client("compat.mc.SpriteTranslucencyFix.TextureAtlasSpriteMixin")),

    Compat_NTM_SkyFix(Phase.LATE,
                      () -> CompatConfig.NTM_SkyFix,
                      require(NuclearTech),
                      client("compat.ntm.ShaderMixin",
                             "compat.ntm.SkyProviderCelestialMixin")),

    Compat_EventHijacking(Phase.EARLY,
                          client("compat.EventHijacking.ASMEventHandlerMixin")),
    //@formatter:on

    //region boilerplate
    ;
    @Getter
    private final MixinBuilder builder;

    Mixin(Phase phase, SidedMixins... mixins) {
        this(builder(mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, SidedMixins... mixins) {
        this(builder(cond, mixins).setPhase(phase));
    }

    Mixin(Phase phase, TaggedMod mod, SidedMixins... mixins) {
        this(builder(mod, mixins).setPhase(phase));
    }

    Mixin(Phase phase, TaggedMod[] mods, SidedMixins... mixins) {
        this(builder(mods, mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, TaggedMod mod, SidedMixins... mixins) {
        this(builder(cond, mod, mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, TaggedMod[] mods, SidedMixins... mixins) {
        this(builder(cond, mods, mixins).setPhase(phase));
    }

    private static SidedMixins common(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".mixin.mixins.common.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.common(mixins);
    }

    private static SidedMixins client(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".mixin.mixins.client.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.client(mixins);
    }

    private static SidedMixins server(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".mixin.mixins.server.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.server(mixins);
    }
    //endregion
}
