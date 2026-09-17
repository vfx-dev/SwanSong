/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.compile;

import com.ventooth.swansong.gl.GLProgram;
import com.ventooth.swansong.shader.ShaderId;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record CompiledProgram(@NotNull String path,
                              @NotNull GLProgram program,
                              @NotNull ObjectList<String> mipmapEnabled,
                              @Nullable IntList renderTargets,
                              @NotNull ShaderId actualShaderType) {
}
