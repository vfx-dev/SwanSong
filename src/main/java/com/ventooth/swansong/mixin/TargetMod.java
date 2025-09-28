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

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@RequiredArgsConstructor
@Accessors(fluent = false)
public enum TargetMod implements ITargetMod {
    //coremods
    SmoothFont("bre.smoothfont.asm.CorePlugin"),
    FoamFix("pl.asie.foamfix.coremod.FoamFixCore"),
    OpenComputers("li.cil.oc.common.launch.TransformerLoader"),
    DragonAPI("Reika.DragonAPI.Auxiliary.DragonAPIASMHandler"),
    RotaryCraft("Reika.RotaryCraft.Auxiliary.RotaryASMHandler"),
    NotFine("jss.notfine.mixinplugin.NotFineEarlyMixins"),
    Zume("dev.nolij.zume.api.config.v1.ZumeConfig"),
    ModernWarfare("com.vicmatskiv.weaponlib.core.WeaponlibCorePlugin"),
    //regular mods,
    Avaritia("fox.spiteful.avaritia.Avaritia"),
    Thaumcraft("thaumcraft.common.Thaumcraft"),
    ThermalExpansion("cofh.thermalexpansion.ThermalExpansion"),
    JourneyMap("journeymap.common.Journeymap"),
    NuclearTech("com.hbm.main.MainRegistry"),
    ;

    TargetMod(@Language(value = "JAVA",
                        prefix = "import ",
                        suffix = ";") @NotNull String className) {
        this(className, null);
    }

    TargetMod(@Language(value = "JAVA",
                        prefix = "import ",
                        suffix = ";") @NotNull String className, @Nullable Consumer<TargetModBuilder> cfg) {
        builder = new TargetModBuilder();
        builder.setTargetClass(className);
        if (cfg != null) {
            cfg.accept(builder);
        }
    }

    @Getter
    private final TargetModBuilder builder;
}
