/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.resources.pack;

import com.ventooth.swansong.shader.preprocessor.FSProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.WorldProvider;

@RequiredArgsConstructor
@Accessors(fluent = true,
           chain = false)
public abstract class ShaderPack implements FSProvider {
    @Getter
    protected final String name;

    @Override
    public String absolutize(String source, String path) {
        if (source != null && source.startsWith("/shaders")) {
            source = source.substring("/shaders".length());
        }
        val thePath = FSProvider.super.absolutize(source, path);
        if (thePath == null) {
            return null;
        }
        return "/shaders" + thePath;
    }

    public abstract @Nullable String getWorldSpecialization(@Nullable WorldProvider dimension);
}
