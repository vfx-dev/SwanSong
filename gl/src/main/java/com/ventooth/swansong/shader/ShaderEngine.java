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
    protected boolean isInitializedImpl() {
        return OldShaderEngine.isInitialized();
    }

    public StateGraph graph() {
        return OldShaderEngine.graph;
    }

    public void beginRenderAllPre() {
        OldShaderEngine.beginRenderAllPre();
    }

    public void beginRenderAll() {
        OldShaderEngine.beginRenderAll();
    }

    public void endRenderAll() {
        OldShaderEngine.endRenderAll();
    }

    public void scheduleShaderPackReload() {
        OldShaderEngine.scheduleShaderPackReload();
    }

    public void scheduleFramebufferResize() {
        OldShaderEngine.scheduleFramebufferResize();
    }

    public void runDeferredPipeline() {
        OldShaderEngine.runDeferredPipeline();
    }

    public void beginRenderWorld() {
        OldShaderEngine.beginRenderWorld();
    }

    public void preRenderLast() {
        OldShaderEngine.preRenderLast();
    }

    public void finishRenderFinal() {
        OldShaderEngine.finishRenderFinal();
    }
}
