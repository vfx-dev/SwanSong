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

import com.ventooth.swansong.Share;
import com.ventooth.swansong.shader.BufferNameUtil;
import com.ventooth.swansong.shader.CompositeTextureData;
import com.ventooth.swansong.shader.ShaderSamplers;
import com.ventooth.swansong.shader.loader.CompiledProgram;
import com.ventooth.swansong.shader.loader.IShaderPool;
import com.ventooth.swansong.shader.uniform.GeneralUniforms;
import com.ventooth.swansong.shader.uniform.Uniform;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.Contract;

import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Accessors(fluent = true,
           chain = false)
public class CompositeShader extends ManagedShader {
    private static final List<Uniform<?>> RELEVANT_UNIFORMS = GeneralUniforms.getWith(ShaderSamplers.Composite.uniforms());

    @Getter
    private final Set<CompositeTextureData> mipmapEnabled; // TODO: Do we make this sorted?

    public CompositeShader(ResourceLocation loc, CompiledProgram prog) {
        super(loc, prog);

        val mipmapEnabled = EnumSet.noneOf(CompositeTextureData.class);
        for (val name : prog.mipmapEnabled()) {
            val index = BufferNameUtil.gbufferIndexFromName(name);
            if (index == null) {
                // TODO: This needs to be moved some place where the Report is accessible
                Share.log.debug("{} bad mipmap setting: {}MipmapEnabled", loc, name);
            } else {
                mipmapEnabled.add(index);
                Share.log.trace("{}->{}MipmapEnabled:{}", loc, name, index);
            }
        }
        if (mipmapEnabled.isEmpty()) {
            this.mipmapEnabled = Collections.emptySet();
        } else {
            this.mipmapEnabled = Collections.unmodifiableSet(mipmapEnabled);
        }
    }

    @Contract("_,_,true->!null")
    public static CompositeShader load(IShaderPool pool, ResourceLocation loc, boolean essential) {
        val shader = pool.borrowShader(loc, essential);
        if (shader == null) {
            if (essential) {
                throw new NullPointerException("Failed to load essential shader: " + loc);
            }
            return null;
        }

        return new CompositeShader(loc, shader);
    }

    @Override
    protected List<Uniform<?>> relevantUniforms() {
        return RELEVANT_UNIFORMS;
    }
}
