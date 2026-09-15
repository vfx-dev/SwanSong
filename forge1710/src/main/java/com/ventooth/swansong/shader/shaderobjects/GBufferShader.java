/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.shaderobjects;

import com.ventooth.swansong.shader.ShaderException;
import com.ventooth.swansong.shader.ShaderSamplers;
import com.ventooth.swansong.shader.loader.CompiledProgram;
import com.ventooth.swansong.shader.loader.IShaderPool;
import com.ventooth.swansong.shader.uniform.GeneralUniforms;
import com.ventooth.swansong.shader.uniform.Uniform;
import lombok.val;
import org.jetbrains.annotations.Contract;

import net.minecraft.util.ResourceLocation;

import java.util.List;

public class GBufferShader extends ManagedShader {
    private static final List<Uniform<?>> RELEVANT_UNIFORMS = GeneralUniforms.getWith(ShaderSamplers.GBuffer.uniforms());

    public GBufferShader(ResourceLocation loc, CompiledProgram prog) {
        super(loc, prog);
    }

    @Contract("_,_,true->!null")
    public static GBufferShader load(IShaderPool pool, ResourceLocation loc, boolean essential) throws ShaderException {
        val shader = pool.borrowShader(loc, essential);
        if (shader == null) {
            if (essential) {
                throw new NullPointerException("Failed to load essential shader: " + loc);
            }
            return null;
        }

        return new GBufferShader(loc, shader);
    }

    @Override
    protected List<Uniform<?>> relevantUniforms() {
        return RELEVANT_UNIFORMS;
    }
}
