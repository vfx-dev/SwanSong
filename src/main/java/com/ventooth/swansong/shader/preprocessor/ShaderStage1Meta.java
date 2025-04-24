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

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ShaderStage1Meta {
    /**
     * For macros such as MC_VERSION
     */
    public final @NotNull MacroBuilder extraMacros = new MacroBuilder();

    /**
     * All of these kinds of defines:
     * <pre>
     * #define OPTION
     * //#define OPTION
     * #define OPTION 0.0 // [0.0 1.0 2.0]
     * </pre>
     * <p>
     * Used for detecting available config options.
     * <p>
     * You can only modify these in stage 1.
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
     * All values are readonly! You can only modify these in stage 2.
     * <o>
     * Used for detecting available config options.
     * <p>
     * Do not configure state with these in stage 1.
     * <p>
     * null if the preprocessor is not in glsl mode.
     */
    public final @Unmodifiable List<Option> consts;
    /**
     * Named lookup for {@link #consts}. null if the preprocessor is not in glsl mode.
     */
    public final @Unmodifiable Map<String, Option> constsByName;

    /**
     * The glsl file name indexes. The list index is the opengl source index, and the string is the source file.
     * Print these out if an opengl error happens so that you can find what file the error came from.
     */
    public final @NotNull
    @Unmodifiable List<String> fileNameIndices;
}
