/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client;

import com.ventooth.swansong.mixin.interfaces.ShaderRenderGlobal;
import com.ventooth.swansong.platform.ArchaicShaderEngine;
import com.ventooth.swansong.shader.OldShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.RenderGlobal;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin implements ShaderRenderGlobal {
    @Unique
    // TODO: Proper shader engine init logic?
    private ArchaicShaderEngine swan$shaderEngine = new ArchaicShaderEngine();

    @Override
    public boolean swan$shadersInitialized() {
        // TODO: Later this would be a `swan$shaderEngine == null` check :)
        return OldShaderEngine.isInitialized();
    }

    @Override
    public boolean swan$shadersActive() {
        if (swan$shadersInitialized()) {
            return swan$shaderStateGraph().isManaged();
        }
        return false;
    }

    @Override
    public boolean swan$shadowPassExists() {
        if (swan$shadersInitialized()) {
            return OldShaderEngine.shadowPassExists();
        }
        return false;
    }

    @Override
    public boolean swan$shadowPassActive() {
        if (swan$shadersInitialized()) {
            return swan$shaderStateGraph().isShadowPass();
        }
        return false;
    }

    @Override
    public ArchaicShaderEngine swan$shaderEngine() throws IllegalStateException {
        if (!swan$shadersInitialized()) {
            throw new IllegalStateException("Shaders not Initialized");
        }
        return swan$shaderEngine;
    }

    @Override
    public StateGraph swan$shaderStateGraph() throws IllegalStateException {
        if (!swan$shadersInitialized()) {
            throw new IllegalStateException("Shaders not Initialized");
        }
        return swan$shaderEngine.graph();
    }
}
