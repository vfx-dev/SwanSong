/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record ShaderId(@NotNull String namespace, @NotNull String path) {
    public static final String DEFAULT_NAMESPACE = "minecraft";

    public static ShaderId of(@NotNull String id) {
        val separator = id.indexOf(':');
        if (separator < 0) {
            return new ShaderId(DEFAULT_NAMESPACE, id);
        }
        return of(id.substring(0, separator), id.substring(separator + 1));
    }

    public static ShaderId of(@NotNull String namespace, @NotNull String path) {
        return new ShaderId(namespace.toLowerCase(Locale.ROOT), path);
    }

    @Override
    public String toString() {
        return namespace + ':' + path;
    }
}
