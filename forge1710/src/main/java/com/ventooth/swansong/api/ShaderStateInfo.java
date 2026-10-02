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


import com.ventooth.swansong.platform.ArchaicShaderEngine;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.3.0")
public final class ShaderStateInfo {
    private ShaderStateInfo() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return True if the engine is currently initialized
     */
    public static boolean isInitialized() {
        return ArchaicShaderEngine.isInitialized();
    }

    /**
     * @return True if the engine is currently expecting to render something
     */
    public static boolean isRendering() {
        val engine = ArchaicShaderEngine.getNullable();
        if (engine == null) {
            return false;
        }
        return engine.graph()
                     .isManaged();
    }

    /**
     * @return True if the current shader pack has a shadow pass
     */
    public static boolean shadowPassExists() {
        val engine = ArchaicShaderEngine.getNullable();
        if (engine == null) {
            return false;
        }
        return engine.shadowPassExists();
    }

    /**
     * TODO: Hell you MEAN undefined if shadow pass doesn't exist?? Then how can it be active!!?
     *
     * @return True if we're currently rendering the shadow pass. False otherwise. Undefined if {@link #shadowPassExists()} is false.
     */
    public static boolean shadowPassActive() {
        val engine = ArchaicShaderEngine.getNullable();
        if (engine == null) {
            return false;
        }
        return engine.graph()
                     .isShadowPass();
    }
}
