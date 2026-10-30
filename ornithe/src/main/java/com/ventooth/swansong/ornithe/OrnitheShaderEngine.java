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
import com.ventooth.swansong.shader.ShadowPassRenderer;
import com.ventooth.swansong.shader.StateGraph;
import net.fabricmc.loader.api.FabricLoader;

public final class OrnitheShaderEngine extends ShaderEngine<OrnitheShaderState> {
    public OrnitheShaderEngine(SwanSongConfig config) {
        super(new OrnitheShaderState(),
              new OrnitheHostRenderer(new OrnitheShaderPackManager(FabricLoader.getInstance()
                                                                               .getGameDir(), config)),
              ShadowPassRenderer.NONE,
              StateGraph.Order.frame());
    }
}
