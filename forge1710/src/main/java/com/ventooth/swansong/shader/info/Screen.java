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

import java.util.Objects;

//TODO convert to record
public final class Screen {
    private final @NotNull ObjectSet<String> sliders;
    private final @Nullable ObjectList<String> mainPage;
    private final @NotNull Object2ObjectMap<String, ObjectList<String>> subPages;

    public Screen(@NotNull ObjectSet<String> sliders,
                  @Nullable ObjectList<String> mainPage,
                  @NotNull Object2ObjectMap<String, ObjectList<String>> subPages) {
        this.sliders = sliders;
        this.mainPage = mainPage;
        this.subPages = subPages;
    }

    public @NotNull ObjectSet<String> sliders() {
        return sliders;
    }

    public @Nullable ObjectList<String> mainPage() {
        return mainPage;
    }

    public @NotNull Object2ObjectMap<String, ObjectList<String>> subPages() {
        return subPages;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (Screen) obj;
        return Objects.equals(this.sliders, that.sliders) &&
               Objects.equals(this.mainPage, that.mainPage) &&
               Objects.equals(this.subPages, that.subPages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sliders, mainPage, subPages);
    }

    @Override
    public String toString() {
        return "Screen[" + "sliders=" + sliders + ", " + "mainPage=" + mainPage + ", " + "subPages=" + subPages + ']';
    }

}
