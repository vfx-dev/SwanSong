/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.preprocessor;

import it.unimi.dsi.fastutil.ints.IntList;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ShaderStage2Meta {
    public final @Nullable
    @Unmodifiable IntList renderTargets;
    /**
     * All of these kinds of defines:
     * <pre>
     * #define OPTION
     * //#define OPTION
     * #define OPTION 0.0 // [0.0 1.0 2.0]
     * </pre>
     * <p>
     * All values are readonly! You can only modify these in stage 1.
     * <p>
     * You can use these to configure shader engine state.
     */
    public final @NotNull
    @Unmodifiable List<Option> defines;
    /**
     * Named lookup for {@link #defines}
     */
    public final @NotNull
    @Unmodifiable Map<String, Option> definesByName;
    /**
     * All of these kinds of constants:
     * <pre>
     *     const int/float/etc name = 0.0;
     *     const int/float/etc name = 0.0; // [0.0 1.0 2.0]
     * </pre>
     * <p>
     * Modifiable values, only contains constants that were kept after the preprocessing.
     * <p>
     * You can apply configuration values here.
     * <p>
     * You can use these to configure shader engine state.
     * <p>
     * null if the preprocessor is not in glsl mode.
     */
    public final @Unmodifiable List<Option> consts;
    /**
     * Named lookup for {@link #consts}.
     * <p>
     * null if the preprocessor is not in glsl mode.
     */
    public final @Unmodifiable Map<String, Option> constsByName;
}
