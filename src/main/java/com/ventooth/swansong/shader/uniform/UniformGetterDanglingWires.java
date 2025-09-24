/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.uniform;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.List;

@Deprecated
public class UniformGetterDanglingWires {
    public static @Nullable List<Uniform<?>> customUniforms;

    // @formatter:off
    // FIXED
    // TODO: PROP Needs_Test_Shader
    public static Vector2ic terrainTextureSize() {return new Vector2i();}

    // TODO: PROP Needs_Test_Shader
    public static int       terrainIconSize() {return 1;}

    // @formatter:on
}
