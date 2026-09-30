/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// TODO: Migrate stuff from 'OldShaderEngine' here
// TODO: Add delegate methods for stuff like `.graph().isManaged()` etc
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ShaderEngine {
    public boolean isInitialized() {
        return OldShaderEngine.isInitialized();
    }

    public boolean shadowPassExists() {
        return OldShaderEngine.shadowPassExists();
    }

    public StateGraph graph() {
        return OldShaderEngine.graph;
    }
}
