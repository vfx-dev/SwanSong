/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.platform;

import com.ventooth.swansong.mixin.extensions.RenderGlobalExt;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;

import static com.ventooth.swansong.shader.StateGraph.Node.*;

public class ArchaicShaderEngine extends ShaderEngine<ArchaicShaderState> {
    private static final StateGraph.Order STAGE_ORDER = StateGraph.Order.frame()
            .edge(ShadowBegin, ShadowChunk0)
            .edge(ShadowChunk0, ShadowEntities0, ShadowChunk1)
            .edge(ShadowEntities0, ShadowBlockEntities0)
            .edge(ShadowBlockEntities0, ShadowEntities0, ShadowChunk1)
            .edge(ShadowChunk1, ShadowEntities1, ShadowLast)
            .edge(ShadowEntities1, ShadowBlockEntities1)
            .edge(ShadowBlockEntities1, ShadowEntities1, ShadowLast)
            .edge(RenderBegin, RenderSkyBasic, RenderClouds, RenderChunk0)
            .edge(RenderSkyBasic, RenderSkyBasic, RenderSkyTextured, RenderClouds, RenderChunk0)
            .edge(RenderSkyTextured, RenderSkyBasic, RenderSkyTextured, RenderClouds, RenderChunk0)
            .edge(RenderClouds, RenderChunk0, RenderLast)
            .edge(RenderChunk0, RenderWeatherEntities0, RenderSelectionBox)
            .edge(RenderWeatherEntities0, RenderEntities0)
            .edge(RenderEntities0, RenderBlockEntities0)
            .edge(RenderBlockEntities0, RenderWeatherEntities0, RenderSelectionBox)
            .edge(RenderSelectionBox, RenderBlockDamage)
            .edge(RenderBlockDamage, RenderParticlesLit)
            .edge(RenderParticlesLit, RenderParticles)
            .edge(RenderParticles, RenderWeather)
            .edge(RenderWeather, RenderHand0)
            .edge(RenderHand0, RenderChunk1)
            .edge(RenderChunk1, RenderWeatherEntities1, RenderClouds, RenderLast)
            .edge(RenderWeatherEntities1, RenderEntities1)
            .edge(RenderEntities1, RenderBlockEntities1)
            .edge(RenderBlockEntities1, RenderWeatherEntities1, RenderClouds, RenderLast);

    public ArchaicShaderEngine() {
        super(new ArchaicShaderState(), new McHostRenderer(), new McShadowPassRenderer(), STAGE_ORDER);
    }

    /// TODO: Currently we have a 'middle' state where the engine may be either null or not-initialized.
    ///  As far as the hooks care, both of these mean: 'no ShaderPack loaded'.
    ///  Later this method should be renamed to 'isShaderPackLoaded()` for clarity.
    ///  And only used in the context where we have a piece of logic that doesn't use the engine,
    ///  but only needs to know if a pack is loaded or not.
    ///
    /// @return `true` if the shader engine has been set and is initialized
    ///
    /// @apiNote Call this before a [ArchaicShaderEngine#get()] if said call may occur before the engine is created!
    public static boolean isInitialized() {
        val engine = getNullable();
        if (engine == null) {
            return false;
        }
        return engine.isInitializedImpl();
    }

    /// TODO: This method should probably be removed in favor of `getNullable()`
    ///
    /// @return the global shader engine reference
    ///
    /// @throws IllegalStateException if the engine is not initialized
    public static ArchaicShaderEngine get() throws IllegalStateException {
        val engine = getNullable();
        if (engine == null) {
            throw new IllegalStateException("No engine attached");
        }
        return engine;
    }

    /// TODO: Should be the preferred way of getting an engine reference.
    ///  In the future, this method returning `null` will denote only that a ShaderPack is not currently loaded.
    ///
    /// @return the global shader engine reference, or `null` if it has not been set
    ///
    /// @throws IllegalStateException if the engine is not initialized
    public static @Nullable ArchaicShaderEngine getNullable() {
        val mc = Minecraft.getMinecraft();
        if (mc == null) {
            return null;
        }
        val rg = mc.renderGlobal;
        if (!(rg instanceof RenderGlobalExt rgExt)) {
            return null;
        }
        return rgExt.swan$engine();
    }
}
