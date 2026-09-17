/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.sufrace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public final class TextureMeta {
    private static final Gson GSON = new GsonBuilder().create();

    @SerializedName("texture")
    public final Inner inner;

    public TextureMeta(boolean blur, boolean clamp) {
        this.inner = new Inner(blur, clamp);
    }

    public static void main(String[] args) {
        System.out.println(read("{\n" +
                                "  \"texture\":\n" +
                                "  {\n" +
                                "    \"blur\": true,\n" +
                                "    \"clamp\": false\n" +
                                "  }\n" +
                                "}"));
    }

    public static @Nullable TextureMeta read(String str) {
        try {
            return GSON.fromJson(str, TextureMeta.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean blur() {
        return inner.blur;
    }

    public boolean clamp() {
        return inner.clamp;
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    @AllArgsConstructor
    public static final class Inner {
        public final boolean blur;
        public final boolean clamp;
    }
}
