/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.notfine;

import com.ventooth.swansong.mixin.interfaces.ShaderGameSettings;
import jss.notfine.gui.NotFineGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

@Mixin(NotFineGameOptionPages.class)
public abstract class NotFineGameOptionPagesMixin {
    @Shadow(remap = false)
    @Final
    private static MinecraftOptionsStorage vanillaOpts;

    @Redirect(method = "other",
              at = @At(value = "INVOKE",
                       target = "Lme/jellysquid/mods/sodium/client/gui/options/OptionImpl$Builder;build()Lme/jellysquid/mods/sodium/client/gui/options/OptionImpl;",
                       ordinal = 0),
              slice = @Slice(from = @At(value = "CONSTANT",
                                        args = "stringValue=options.anaglyph")),
              remap = false,
              require = 1)
    private static OptionImpl<GameSettings, Integer> anaglyphSlider(OptionImpl.Builder<GameSettings, Integer> instance) {
        return OptionImpl.createBuilder(int.class, vanillaOpts)
                         .setName(I18n.format("options.anaglyph"))
                         .setTooltip(I18n.format("sodium.options.anaglyph.tooltip"))
                         .setControl(opt -> new SliderControl(opt, 0, 200, 1, value -> {
                             if (value == 0) {
                                 return I18n.format("options.off");
                             } else if (value == 100) {
                                 return I18n.format("swansong.anaglyph.standard");
                             } else if (value == 200) {
                                 return I18n.format("swansong.anaglyph.wide");
                             } else {
                                 return value + "%";
                             }
                         }))
                         .setBinding((opts, value) -> ((ShaderGameSettings)opts).swan$anaglyph(value / 100f), opts -> (int) (((ShaderGameSettings)opts).swan$anaglyph() * 100f))
                         .setImpact(OptionImpact.EXTREME)
                         .setEnabled(true)
                         .build();
    }
}
