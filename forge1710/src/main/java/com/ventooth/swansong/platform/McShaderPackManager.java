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

import com.ventooth.swansong.config.Configs;
import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.resources.pack.ShaderPack;
import com.ventooth.swansong.shader.loader.config.PackLocalizer;

import java.nio.file.Path;

public final class McShaderPackManager extends ShaderPackManager {
    public McShaderPackManager(Path minecraftDir) {
        super(minecraftDir);
    }

    @Override
    protected boolean referencePackEnabled() {
        return ShadersConfig.enableReferenceShaderPack;
    }

    @Override
    public PackLocalizer localizer(ShaderPack pack) {
        return new MinecraftPackLocalizer(pack);
    }

    @Override
    protected void onSettingsChanged() {
        ShadersConfig.CurrentShaderPack = getCurrentShaderPackName();
        ArchaicShaderEngine.get()
                           .scheduleShaderPackReload();
        Configs.syncConfigFile();
    }
}
