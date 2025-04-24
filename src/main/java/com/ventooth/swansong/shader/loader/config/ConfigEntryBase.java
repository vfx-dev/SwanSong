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

import com.ventooth.swansong.shader.config.ConfigEntry;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Unmodifiable;

@RequiredArgsConstructor
public abstract class ConfigEntryBase implements ConfigEntry {
    protected final Localization locale;

    @Override
    public String optionName() {
        return locale.name();
    }

    @Override
    public @Unmodifiable ObjectList<String> description() {
        return locale.description();
    }
}
