/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.gui;

import lombok.val;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;

public interface Translatable {
    String langKey();

    default String format(Locale locale, Object... args) {
        val langKey = langKey();
        var str = locale == null ? langKey : locale.formatMessage(langKey(), args);
        if (langKey.equals(str)) {
            str = I18n.format(langKey, args);
        }
        return str;

    }

    /**
     * Let Minecraft Handle the translation
     */
    interface BuiltinLocale extends Translatable {
        @Override
        default String format(Locale locale, Object... args) {
            return I18n.format(langKey(), args);
        }
    }
}
