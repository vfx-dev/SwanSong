/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class MicroCache<Key, Value> {
    public static final int CACHE_HIT_NULL_KEY = -2;
    private static final int INITIAL_CAPACITY = 8 * 1024;
    private Object2IntMap<Key> cacheIdx = new Object2IntOpenHashMap<>(INITIAL_CAPACITY, 0.2f);
    private ObjectList<@NotNull Value> cache = new ObjectArrayList<>(INITIAL_CAPACITY);

    {
        cacheIdx.defaultReturnValue(-1);
    }

    public void purge() {
        if (cache.size() > INITIAL_CAPACITY) {
            cacheIdx = new Object2IntOpenHashMap<>(INITIAL_CAPACITY, 0.2f);
            cache = new ObjectArrayList<>(INITIAL_CAPACITY);
        } else {
            cacheIdx.clear();
            cache.clear();
        }
    }

    public int getIndex(@NotNull Key key) {
        return cacheIdx.getInt(key);
    }

    public @NotNull Value getByIndex(int key) {
        return cache.get(key);
    }

    public void store(@NotNull Key key, @Nullable Value value) {
        if (value == null) {
            cacheIdx.put(key, -2);
            return;
        }
        cacheIdx.put(key, cache.size());
        cache.add(value);
    }

    public @Nullable Value getCached(@NotNull Key key, @NotNull Function<@NotNull Key, @Nullable Value> getter) {
        val idx = getIndex(key);
        if (idx == CACHE_HIT_NULL_KEY) {
            return null;
        }
        if (idx >= 0) {
            return getByIndex(idx);
        }
        val v = getter.apply(key);
        store(key, v);
        return v;
    }
}
