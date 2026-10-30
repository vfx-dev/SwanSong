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
import net.ornithemc.osl.config.api.ConfigScope;
import net.ornithemc.osl.config.api.LoadingPhase;
import net.ornithemc.osl.config.api.config.BaseConfig;
import net.ornithemc.osl.config.api.config.option.BooleanOption;
import net.ornithemc.osl.config.api.config.option.FloatOption;
import net.ornithemc.osl.config.api.config.option.StringOption;
import net.ornithemc.osl.config.api.serdes.FileSerializerType;
import net.ornithemc.osl.config.api.serdes.SerializerTypes;

public final class SwanSongConfig extends BaseConfig {
    public final StringOption currentShaderPack = new StringOption("currentShaderPack",
                                                                   "The selected shader pack",
                                                                   ShaderPackManager.DISABLED_SHADER_PACK_NAME);
    public final BooleanOption enableReferenceShaderPack = new BooleanOption("enableReferenceShaderPack",
                                                                             "List SwanSong's built-in shader pack",
                                                                             false);
    public final FloatOption renderQuality = new FloatOption("renderQuality",
                                                             "Scale of the shader render targets",
                                                             1F,
                                                             v -> v >= 0.5F && v <= 2F);
    public final FloatOption shadowQuality = new FloatOption("shadowQuality",
                                                             "Scale of the shadow map",
                                                             1F,
                                                             v -> v >= 0.5F && v <= 2F);
    public final FloatOption handDepth = new FloatOption("handDepth",
                                                         "Depth scale of the first person hand",
                                                         1F,
                                                         v -> v >= 0.5F && v <= 2F);
    public final BooleanOption allowDepthOfField = new BooleanOption("allowDepthOfField",
                                                                     "Let shader packs use depth of field",
                                                                     false);

    @Override
    public String getNamespace() {
        return "swansong";
    }

    @Override
    public String getName() {
        return "swansong";
    }

    @Override
    public String getSaveName() {
        return "swansong.json";
    }

    @Override
    public ConfigScope getScope() {
        return ConfigScope.GLOBAL;
    }

    @Override
    public LoadingPhase getLoadingPhase() {
        return LoadingPhase.START;
    }

    @Override
    public FileSerializerType<?> getType() {
        return SerializerTypes.JSON;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void init() {
        registerOptions("shaders",
                        currentShaderPack,
                        enableReferenceShaderPack,
                        renderQuality,
                        shadowQuality,
                        handDepth,
                        allowDepthOfField);
    }
}
