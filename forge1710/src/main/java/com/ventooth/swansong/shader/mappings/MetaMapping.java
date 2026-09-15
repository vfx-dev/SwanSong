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

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

//TODO convert to record
public final class MetaMapping {
    private final int outputID;
    private final @Nullable IntSet pattern;

    public MetaMapping(int outputID, @Nullable IntSet pattern) {
        this.outputID = outputID;
        this.pattern = pattern;
    }

    public boolean matches(int meta) {
        if (pattern == null) {
            return true;
        }
        return pattern.contains(meta);
    }

    private boolean wildcard() {
        return pattern == null;
    }

    private static MetaMapping merge(MetaMapping a, @NotNull IntSet metas) {
        if (metas.isEmpty()) {
            return a;
        }
        int resLength = (a.pattern != null ? a.pattern.size() : 0) + metas.size();
        val newSet = resLength <= 8 ? new IntArraySet() : new IntOpenHashSet();
        if (a.pattern != null) {
            newSet.addAll(a.pattern);
        }
        newSet.addAll(metas);
        return new MetaMapping(a.outputID, newSet);
    }

    public static Status mergeInto(List<MetaMapping> mappings, int outputID, @Nullable IntSet metas) {
        boolean wildcard = metas == null;
        boolean merged = false;
        for (int i = 0, mappingsSize = mappings.size(); i < mappingsSize; i++) {
            val oldMapping = mappings.get(i);
            val oldWildcard = oldMapping.wildcard();
            if (oldWildcard && wildcard) {
                return Status.DuplicateWildcard;
            }
            if (!merged && oldMapping.outputID == outputID) {
                if (!oldWildcard) {
                    if (wildcard) {
                        mappings.set(i, new MetaMapping(outputID, null));
                    } else {
                        mappings.set(i, merge(oldMapping, metas));
                    }
                }
                merged = true;
            }
        }
        if (merged) {
            return Status.Merged;
        }
        mappings.add(new MetaMapping(outputID, metas));
        return Status.Added;
    }

    public int outputID() {
        return outputID;
    }

    public @Nullable IntSet pattern() {
        return pattern;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (MetaMapping) obj;
        return this.outputID == that.outputID && Objects.equals(this.pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputID, pattern);
    }

    @Override
    public String toString() {
        return "MetaMapping[" + "outputID=" + outputID + ", " + "pattern=" + pattern + ']';
    }


    public enum Status {
        Added,
        Merged,
        DuplicateWildcard
    }
}
