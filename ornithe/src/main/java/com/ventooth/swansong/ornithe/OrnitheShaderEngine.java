/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.ornithe;

import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import net.fabricmc.loader.api.FabricLoader;

import static com.ventooth.swansong.shader.StateGraph.Node.*;

public final class OrnitheShaderEngine extends ShaderEngine<OrnitheShaderState> {
    private static final StateGraph.Order STAGE_ORDER = StateGraph.Order.frame()
            .edge(ShadowBegin, ShadowChunk0)
            .edge(ShadowChunk0, ShadowEntities0, ShadowChunk1)
            .edge(ShadowEntities0, ShadowEntities0, ShadowBlockEntities0)
            .edge(ShadowBlockEntities0, ShadowChunk1)
            .edge(ShadowChunk1, ShadowLast)
            .edge(RenderBegin, RenderSkyBasic, RenderClouds, RenderChunk0)
            .edge(RenderSkyBasic, RenderSkyBasic, RenderSkyTextured, RenderClouds, RenderChunk0)
            .edge(RenderSkyTextured, RenderSkyBasic, RenderSkyTextured, RenderClouds, RenderChunk0)
            .edge(RenderClouds, RenderChunk0, RenderLast)
            .edge(RenderChunk0, RenderWeatherEntities0, RenderSelectionBox, RenderBlockDamage)
            .edge(RenderWeatherEntities0, RenderEntities0)
            .edge(RenderEntities0, RenderBlockEntities0)
            .edge(RenderBlockEntities0, RenderSelectionBox)
            .edge(RenderSelectionBox, RenderBlockDamage)
            .edge(RenderBlockDamage, RenderParticlesLit, RenderWeather)
            .edge(RenderParticlesLit, RenderParticles)
            .edge(RenderParticles, RenderWeather)
            .edge(RenderWeather, RenderHand0)
            .edge(RenderHand0, RenderChunk1)
            .edge(RenderChunk1, RenderClouds, RenderLast);

    public OrnitheShaderEngine(SwanSongConfig config) {
        super(new OrnitheShaderState(),
              new OrnitheHostRenderer(new OrnitheShaderPackManager(FabricLoader.getInstance()
                                                                               .getGameDir(), config)),
              new OrnitheShadowPassRenderer(),
              STAGE_ORDER);
    }
}
