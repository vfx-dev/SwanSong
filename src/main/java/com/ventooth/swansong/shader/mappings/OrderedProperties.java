/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.mappings;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class OrderedProperties extends Properties implements Iterable<Map.Entry<String, String>> {
    private final Map<String, String> map = new LinkedHashMap<>();

    @Override
    public @NotNull Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet()
                  .iterator();
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        val old = super.put(key, value);
        if (!(key instanceof String strKey)) {
            return old;
        }
        if (!(value instanceof String strValue)) {
            return old;
        }
        map.put(strKey, strValue);
        return old;
    }
}