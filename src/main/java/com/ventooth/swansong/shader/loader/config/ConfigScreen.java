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
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.client.resources.Locale;

public class ConfigScreen extends ConfigEntryBase implements ConfigEntry.Screen {
    public final ObjectList<@Nullable ConfigEntry> content;
    private final ObjectList<ConfigEntry> contentUnmodifiable;

    public ConfigScreen(Locale locale, String screenName, ObjectList<@Nullable ConfigEntry> content) {
        super(Localization.createScreen(locale, screenName));
        this.content = content;
        this.contentUnmodifiable = ObjectLists.unmodifiable(content);
    }

    @Override
    public String valueName() {
        throw new AssertionError();
    }

    @Override
    public String screenTitle() {
        return locale.name();
    }

    @Override
    public @Unmodifiable ObjectList<@Nullable ConfigEntry> entries() {
        return contentUnmodifiable;
    }

    @Override
    public int columnsHint() {
        return 2;
    }

    @Override
    public void reset() {
        for (val entry : this.content) {
            if (entry instanceof Mutable m) {
                m.resetToDefault();
            }
        }
    }
}
