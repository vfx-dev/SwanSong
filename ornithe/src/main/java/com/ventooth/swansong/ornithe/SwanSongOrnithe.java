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

import com.ventooth.swansong.Share;
import com.ventooth.swansong.shader.ShaderTypes;
import com.ventooth.swansong.uniforms.Type;
import com.ventooth.swansong.uniforms.UniformDef;
import com.ventooth.swansong.uniforms.compiler.UniformCodegen;
import net.fabricmc.api.ClientModInitializer;

import java.util.List;

public final class SwanSongOrnithe implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        var uniforms = new UniformCodegen().generate(List.of(new UniformDef("smoke", Type.Float, "1.0 + 1.0", true)));
        Share.log.info("SwanSong engine loaded: {} programs, uniform codegen errors: {}",
                       ShaderTypes.general.size(),
                       uniforms.errors);
    }
}
