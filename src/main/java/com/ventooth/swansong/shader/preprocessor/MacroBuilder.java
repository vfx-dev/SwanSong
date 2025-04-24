/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.preprocessor;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class MacroBuilder {
    private final @NotNull Object2ObjectMap<String, Option.Value> macros = new Object2ObjectLinkedOpenHashMap<>();

    public void add(String macro) {
        macros.put(macro, Option.Value.Bool.True);
    }

    public void add(String macro, Option.Value value) {
        macros.put(macro, value);
    }

    public void add(String macro, int value) {
        macros.put(macro, new Option.Value.Int(value));
    }

    public void add(String macro, double value) {
        macros.put(macro, new Option.Value.Dbl(value));
    }

    public void add(String macro, String value) {
        macros.put(macro, Option.Value.detect(value));
    }

    public void addAll(Object2ObjectMap<String, Option.Value> macros) {
        this.macros.putAll(macros);
    }

    public @NotNull @Unmodifiable Object2ObjectMap<String, Option.Value> get() {
        return Object2ObjectMaps.unmodifiable(macros);
    }
}
