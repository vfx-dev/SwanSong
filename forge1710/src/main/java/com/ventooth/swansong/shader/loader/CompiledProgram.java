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

import com.ventooth.swansong.gl.GLProgram;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.ResourceLocation;

import java.util.Objects;

//TODO convert to record
public final class CompiledProgram {
    private final @NotNull String path;
    private final @NotNull GLProgram program;
    private final @NotNull ObjectList<String> mipmapEnabled;
    private final @Nullable IntList renderTargets;
    private final @NotNull ResourceLocation actualShaderType;

    public CompiledProgram(@NotNull String path,
                           @NotNull GLProgram program,
                           @NotNull ObjectList<String> mipmapEnabled,
                           @Nullable IntList renderTargets,
                           @NotNull ResourceLocation actualShaderType) {
        this.path = path;
        this.program = program;
        this.mipmapEnabled = mipmapEnabled;
        this.renderTargets = renderTargets;
        this.actualShaderType = actualShaderType;
    }

    public @NotNull String path() {
        return path;
    }

    public @NotNull GLProgram program() {
        return program;
    }

    public @NotNull ObjectList<String> mipmapEnabled() {
        return mipmapEnabled;
    }

    public @Nullable IntList renderTargets() {
        return renderTargets;
    }

    public @NotNull ResourceLocation actualShaderType() {
        return actualShaderType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (CompiledProgram) obj;
        return Objects.equals(this.path, that.path) &&
               Objects.equals(this.program, that.program) &&
               Objects.equals(this.mipmapEnabled, that.mipmapEnabled) &&
               Objects.equals(this.renderTargets, that.renderTargets) &&
               Objects.equals(this.actualShaderType, that.actualShaderType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, program, mipmapEnabled, renderTargets, actualShaderType);
    }

    @Override
    public String toString() {
        return "CompiledProgram[" +
               "path=" +
               path +
               ", " +
               "program=" +
               program +
               ", " +
               "mipmapEnabled=" +
               mipmapEnabled +
               ", " +
               "renderTargets=" +
               renderTargets +
               ']';
    }

}
