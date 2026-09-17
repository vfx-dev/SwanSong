/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.info;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Screen(@NotNull ObjectSet<String> sliders,
                     @Nullable ObjectList<String> mainPage,
                     @NotNull Object2ObjectMap<String, ObjectList<String>> subPages) {

}
