/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.loader.config;

import com.falsepattern.lib.util.MathUtil;
import com.ventooth.swansong.shader.config.ConfigEntry;
import com.ventooth.swansong.shader.preprocessor.Option;
import lombok.val;

import net.minecraft.client.resources.Locale;

public /*sealed*/ abstract class ConfigChoice extends ConfigEntryBase implements ConfigEntry.Mutable {
    protected final int index;
    protected final Option option;
    protected final ConfigRootScreen root;

    public ConfigChoice(Locale locale, Option option, int index, ConfigRootScreen root) {
        super(Localization.create(locale, false, option.name, null /*TODO*/, option.valueStrings()));
        this.index = index;
        this.option = option;
        this.root = root;
    }

    @Override
    public void resetToDefault() {
        option.setToDefault();
    }

    @Override
    public boolean isModified() {
        return root.isModified(index);
    }

    @Override
    public boolean isDefault() {
        return option.isDefaultValue();
    }

    @Override
    public String valueName() {
        val name = option.getCurrentValue()
                         .toString();
        return locale.options()
                     .getOrDefault(name, name);
    }

    public static class Switchable extends ConfigChoice implements ConfigEntry.Switchable {
        public Switchable(Locale locale, Option option, int index, ConfigRootScreen root) {
            super(locale, option, index, root);
        }

        @Override
        public void nextValue() {
            option.nextValue();
        }

        @Override
        public void prevValue() {
            option.prevValue();
        }
    }

    public static class Draggable extends ConfigChoice implements ConfigEntry.Draggable {
        private final int max;
        private final float scaler;

        public Draggable(Locale locale, Option option, int index, ConfigRootScreen root) {
            super(locale, option, index, root);
            max = option.getValueCount() - 1;
            scaler = (float) max;
        }

        @Override
        public float setValue(float value) {
            value = MathUtil.clamp(value, 0, 1);
            var index = MathUtil.clamp(Math.round(value * scaler), 0, max);
            option.setValueIndex(index);
            return index / scaler;
        }

        @Override
        public float getValue() {
            return option.getValueIndex() / scaler;
        }
    }

    public static class Toggleable extends ConfigChoice implements ConfigEntry.Toggleable {
        public Toggleable(Locale locale, Option opt, int index, ConfigRootScreen root) {
            super(locale, opt, index, root);
            if (!opt.isToggle()) {
                throw new AssertionError();
            }
        }

        @Override
        public boolean toggle() {
            option.setCurrentValue(((Option.Value.Bool) option.getCurrentValue()).toggle());
            return getValue();
        }

        @Override
        public void setValue(boolean value) {
            option.setCurrentValue(Option.Value.Bool.of(value));
        }

        @Override
        public boolean getValue() {
            return ((Option.Value.Bool) option.getCurrentValue()).boolValue();
        }

        @Override
        public String valueName() {
            throw new AssertionError();
        }
    }
}
