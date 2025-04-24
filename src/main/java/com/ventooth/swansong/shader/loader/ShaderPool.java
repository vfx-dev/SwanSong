/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.loader;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.ResourceLocation;

@RequiredArgsConstructor
public class ShaderPool implements IShaderPool {
    private volatile Object2ObjectMap<ResourceLocation, CompiledProgram> programs = new Object2ObjectOpenHashMap<>();
    private volatile ObjectSet<ResourceLocation> borrowed = new ObjectOpenHashSet<>();
    private volatile boolean dead = false;
    private volatile ObjectSet<String> disabled;

    public synchronized void insertShader(ResourceLocation loc, CompiledProgram program) {
        if (dead) {
            throw new IllegalStateException("Shader pool was deinitialized!");
        }
        val wasBorrowed = borrowed.remove(loc);
        val old = programs.put(loc, program);
        if (old != null && !wasBorrowed) {
            old.program()
               .glDeleteProgram();
        }
    }

    @Override
    public synchronized @Nullable CompiledProgram borrowShader(ResourceLocation loc, boolean essential) {
        if (dead) {
            throw new IllegalStateException("Shader pool was deinitialized!");
        }
        var shader = programs.get(loc);
        if (shader == null) {
            return null;
        }
        if (disabled != null && disabled.contains(shader.path())) {
            return null;
        }
        borrowed.add(loc);
        return shader;
    }

    public synchronized void setDisabled(ObjectSet<String> disabled) {
        this.disabled = disabled;
    }

    @Override
    public synchronized void close() {
        if (dead) {
            return;
        }
        dead = true;
        Object2ObjectMaps.fastForEach(programs, entry -> {
            if (borrowed.contains(entry.getKey())) {
                return;
            }
            entry.getValue()
                 .program()
                 .glDeleteProgram();
        });
        programs = null;
        borrowed = null;
        disabled = null;
    }
}
