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

import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.resources.pack.ShaderPack;
import com.ventooth.swansong.shader.loader.config.PackLocalizer;
import net.ornithemc.osl.config.api.ConfigManager;

import java.nio.file.Path;

public final class OrnitheShaderPackManager extends ShaderPackManager {
    private final SwanSongConfig config;

    public OrnitheShaderPackManager(Path gameDir, SwanSongConfig config) {
        super(gameDir);
        this.config = config;
    }

    @Override
    protected boolean referencePackEnabled() {
        return config.enableReferenceShaderPack.get();
    }

    @Override
    public PackLocalizer localizer(ShaderPack pack) {
        return new OrnithePackLocalizer(pack);
    }

    @Override
    protected void onSettingsChanged() {
        config.currentShaderPack.set(getCurrentShaderPackName());
        ConfigManager.save(config);
        SwanSongOrnithe.ENGINE.scheduleShaderPackReload();
    }
}
