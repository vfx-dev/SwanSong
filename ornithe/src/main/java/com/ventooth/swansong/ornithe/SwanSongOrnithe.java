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

import com.ventooth.swansong.EnvInfo;
import com.ventooth.swansong.gl.GLEnvProbe;
import net.fabricmc.loader.api.FabricLoader;
import net.ornithemc.osl.entrypoints.api.client.ClientModInitializer;
import net.ornithemc.osl.config.api.ConfigManager;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;

public final class SwanSongOrnithe implements ClientModInitializer {
    public static final SwanSongConfig CONFIG = new SwanSongConfig();
    public static final OrnitheShaderEngine ENGINE = new OrnitheShaderEngine(CONFIG);

    @Override
    public void initClient() {
        ConfigManager.register(CONFIG);
        MinecraftClientEvents.READY.register(minecraft -> {
            var version = FabricLoader.getInstance()
                                      .getModContainer("swansong")
                                      .orElseThrow()
                                      .getMetadata()
                                      .getVersion()
                                      .getFriendlyString();
            EnvInfo.init(GLEnvProbe.probe("1.8.9", version));
            ENGINE.shaderPacks()
                  .init(CONFIG.currentShaderPack.get());
            ENGINE.firstInit();
        });
    }
}
