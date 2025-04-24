/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.api;


import com.ventooth.swansong.shader.ShaderEngine;

@SuppressWarnings("unused")
public final class ShaderStateInfo {
    private ShaderStateInfo() {
    }

    /**
     * @return True if the engine is currently initialized
     */
    public static boolean isInitialized() {
        return ShaderEngine.isInitialized();
    }

    /**
     * @return True if the engine is currently expecting to render something
     */
    public static boolean isRendering() {
        return ShaderEngine.graph.isManaged();
    }

    /**
     * @return True if the current shader pack has a shadow pass
     */
    public static boolean shadowPassExists() {
        return ShaderEngine.shadowPassExists();
    }

    /**
     * @return True if we're currently rendering the shadow pass. False otherwise. Undefined if {@link #shadowPassExists()} is false.
     */
    public static boolean shadowPassActive() {
        return ShaderEngine.graph.isShadowPass();
    }
}
