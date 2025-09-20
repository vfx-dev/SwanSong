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

import com.ventooth.swansong.mixin.interfaces.ShaderGameSettings;
import lombok.val;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

import java.io.PrintWriter;

@Mixin(GameSettings.class)
public abstract class GameSettingsMixin implements ShaderGameSettings {
    @Shadow
    public abstract float getOptionFloatValue(GameSettings.Options settingOption);

    @Unique
    private float swan$anaglyph;

    @Redirect(method = "loadOptions",
              at = @At(value = "INVOKE",
                       target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z",
                       ordinal = 1),
              slice = @Slice(from = @At(value = "CONSTANT",
                                        args = "stringValue=anaglyph3d")),
              require = 1)
    private boolean getSwanAnaglyph(String instance, Object o) {
        try {
            swan$anaglyph = Float.parseFloat(instance);
        } catch (Exception ignored) {
            swan$anaglyph = 0;
        }
        return false;
    }

    @Override
    public float swan$anaglyph() {
        return swan$anaglyph;
    }

    @Override
    public void swan$anaglyph(float value) {
        swan$anaglyph = value;
    }

    @Redirect(method = "getOptionOrdinalValue",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/settings/GameSettings;anaglyph:Z",
                       opcode = Opcodes.GETFIELD),
              require = 1)
    private boolean swanGuiAnaglyph(GameSettings instance) {
        return swan$anaglyph != 0;
    }

    @Redirect(method = "setOptionValue",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/settings/GameSettings;anaglyph:Z",
                       opcode = Opcodes.PUTFIELD),
              require = 1)
    private void noVanillaAnaglyph(GameSettings instance, boolean value) {

    }

    @Redirect(method = "setOptionValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/Minecraft;refreshResources()V"),
              require = 0, //Crashes for some reason in some packs https://github.com/vfx-dev/SwanSong/issues/1
              expect = 0)
    private void noAnaglyphReload(Minecraft instance) {

    }

    @Redirect(method = "saveOptions",
              at = @At(value = "INVOKE",
                       target = "Ljava/io/PrintWriter;println(Ljava/lang/String;)V",
                       ordinal = 0),
              slice = @Slice(from = @At(value = "CONSTANT",
                                        args = "stringValue=anaglyph3d:")),
              require = 1)
    private void swanSaveAnaglyph(PrintWriter instance, String s) {
        instance.println("anaglyph3d:" + swan$anaglyph);
    }

    @Inject(method = "setOptionFloatValue",
            at = @At("HEAD"),
            require = 1)
    private void setAnaglyph(GameSettings.Options settingsOption, float value, CallbackInfo ci) {
        if (settingsOption == GameSettings.Options.ANAGLYPH) {
            swan$anaglyph = value;
        }
    }

    @Inject(method = "getKeyBinding",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void getAnaglyphText(GameSettings.Options settingOption, CallbackInfoReturnable<String> cir) {
        if (settingOption != GameSettings.Options.ANAGLYPH) {
            return;
        }
        val base = I18n.format(settingOption.getEnumString()) + ": ";
        val value = getOptionFloatValue(settingOption);
        val percentage = (int) (value * 100);
        if (percentage == 0) {
            cir.setReturnValue(base + I18n.format("options.off"));
        } else if (percentage == 100) {
            cir.setReturnValue(base + I18n.format("swansong.anaglyph.standard"));
        } else if (percentage == 200) {
            cir.setReturnValue(base + I18n.format("swansong.anaglyph.wide"));
        } else {
            cir.setReturnValue(base + percentage + "%");
        }
    }

    @Inject(method = "getOptionFloatValue",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void getAnaglyph(GameSettings.Options settingOption, CallbackInfoReturnable<Float> cir) {
        if (settingOption == GameSettings.Options.ANAGLYPH) {
            cir.setReturnValue(swan$anaglyph);
        }
    }

}
