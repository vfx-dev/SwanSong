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

import com.ventooth.swansong.Share;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.WorldProvider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public final class InternalShaderPack extends ShaderPack {
    public static final String NAME = "(internal)";
    public static final InternalShaderPack INSTANCE = new InternalShaderPack();

    private InternalShaderPack() {
        super(NAME);
    }

    @Override
    public @NotNull InputStream get(String path) throws IOException {
        path = fixPath(path);
        Share.log.trace("Reading shader from fallback path: {}", path);
        val entry = ModJarContainer.getEntry(path);
        if (entry == null) {
            throw new FileNotFoundException("path=" + path);
        }
        val is = ModJarContainer.getInputStream(entry);
        Share.log.trace("Yeah, the InputStream does exist for: {}", path);
        return is;
    }

    @Override
    public boolean has(String path) {
        return ModJarContainer.getEntry(fixPath(path)) != null;
    }

    private static String fixPath(String path) {
        return "assets/swansong" + path;
    }

    @Override
    public @Nullable String getWorldSpecialization(@Nullable WorldProvider dimension) {
        return null;
    }
}
