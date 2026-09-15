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

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;

import java.io.IOException;
import java.io.InputStream;


public final class DefaultShaderPack extends ShaderPack {
    public static final String NAME = "(default)";
    public static final DefaultShaderPack INSTANCE = new DefaultShaderPack();

    private DefaultShaderPack() {
        super(NAME);
    }

    @Override
    public @NotNull InputStream get(String path) throws IOException {
        //ResourcePack paths cannot have slash prefix
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }
        Share.log.trace("Reading shader from direct path: {}", path);
        val loc = new ResourceLocation("swansong", path);
        Share.log.trace("Shader from ResourceLocation: {}", loc);
        val is = Minecraft.getMinecraft()
                          .getResourceManager()
                          .getResource(loc)
                          .getInputStream();
        Share.log.trace("Yeah, the InputStream does exist for: {}", loc);
        return is;
    }

    @Override
    public boolean has(String path) throws IOException {
        //ResourcePack paths cannot have slash prefix
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }
        return Minecraft.getMinecraft()
                        .getResourceManager()
                        .getResource(new ResourceLocation("swansong", path)) != null;
    }

    @Override
    public @Nullable String getWorldSpecialization(@Nullable WorldProvider dimension) {
        return null;
    }
}
