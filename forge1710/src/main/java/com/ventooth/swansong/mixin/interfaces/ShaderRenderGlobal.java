/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.interfaces;

import com.ventooth.swansong.platform.ArchaicShaderEngine;
import com.ventooth.swansong.shader.StateGraph;

@Deprecated
public interface ShaderRenderGlobal {
    /// @return `true` if a **Shader Pack** is loaded, otherwise `false`
    boolean swan$shadersInitialized();

    /// @return `true` if a **Shader Pack** is active, otherwise `false`
    boolean swan$shadersActive();

    /// @return `true` if the current **Shader Pack** has a **Shadow Pass**, otherwise `false`
    boolean swan$shadowPassExists();

    /// @return `true` if the **Shadow Pass** is happening, otherwise `false`
    boolean swan$shadowPassActive();

    /// @return the global **Shader Engine**
    ///
    /// @throws IllegalStateException if {@link swan$shadersInitialized()} returns `false`
    ArchaicShaderEngine swan$shaderEngine() throws IllegalStateException;

    /// @return the global **Shader State Graph**
    ///
    /// @throws IllegalStateException if {@link swan$shadersInitialized()} returns `false`
    StateGraph swan$shaderStateGraph() throws IllegalStateException;
}
