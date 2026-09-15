/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.settings.GameSettings;

@Mixin(GameSettings.Options.class)
public class GameSettings_OptionsMixin {
    private GameSettings_OptionsMixin(String key,
                                      int index,
                                      String translation,
                                      boolean isFloat,
                                      boolean isBoolean,
                                      float valMin,
                                      float valMax,
                                      float valStep) {
    }

    @Redirect(method = "<clinit>",
              at = @At(value = "NEW",
                       target = "(Ljava/lang/String;ILjava/lang/String;ZZ)Lnet/minecraft/client/settings/GameSettings$Options;",
                       ordinal = 0),
              slice = @Slice(from = @At(value = "CONSTANT",
                                        args = "stringValue=options.viewBobbing")),
              require = 1)
    private static GameSettings.Options hackAnaglyph(String key,
                                                     int index,
                                                     String translation,
                                                     boolean isFloat,
                                                     boolean isBoolean) {
        return (GameSettings.Options) (Object) new GameSettings_OptionsMixin(key,
                                                                             index,
                                                                             translation,
                                                                             true,
                                                                             false,
                                                                             0,
                                                                             2,
                                                                             0);
    }
}
