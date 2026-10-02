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

import com.ventooth.swansong.shader.config.ConfigEntry;
import com.ventooth.swansong.shader.loader.config.PackLocalizer;
import com.ventooth.swansong.sufrace.Texture2D;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// TODO: Migrate stuff from 'OldShaderEngine' here
// TODO: Add delegate methods for stuff like `.graph().isManaged()` etc
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ShaderEngine {
    protected boolean isInitializedImpl() {
        return OldShaderEngine.isInitialized();
    }

    public void firstInit() {
        OldShaderEngine.firstInit();
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

    public boolean shadowPassExists() {
        return OldShaderEngine.shadowPassExists();
    }

    public boolean doGraphLog() {
        return OldShaderEngine.DO_GRAPH_LOG;
    }

    public List<StateGraph.Node> graphLog() {
        return OldShaderEngine.graphLog;
    }

    public int prevFrameShaderSwitches() {
        return OldShaderEngine.prevFrameShaderSwitches;
    }

    public void unlockShader() {
        OldShaderEngine.unlockShader();
    }

    public void lockShader() {
        OldShaderEngine.lockShader();
    }

    public void blitDepth(Texture2D srcTex, Texture2D dstTex) {
        OldShaderEngine.blitDepth(srcTex, dstTex);
    }

    public void genMipmap(@Nullable Texture2D tex) {
        OldShaderEngine.genMipmap(tex);
    }

    public Logger log() {
        return OldShaderEngine.log;
    }

    public int remapBlockID(int blockID, int meta) {
        return OldShaderEngine.remapBlockID(blockID, meta);
    }

    public PackLocalizer locale() {
        return OldShaderEngine.locale();
    }

    public ConfigEntry.RootScreen configScreen() {
        return OldShaderEngine.configScreen();
    }
}
