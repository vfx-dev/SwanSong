/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader;

import com.ventooth.swansong.EnvInfo;
import com.ventooth.swansong.debug.DebugMarker;
import com.ventooth.swansong.resources.pack.InternalShaderPack;
import com.ventooth.swansong.shader.loader.IShaderPool;
import com.ventooth.swansong.shader.loader.ShaderLoader;
import com.ventooth.swansong.shader.loader.ShaderLoaderInParams;
import com.ventooth.swansong.shader.shaderobjects.BlitShader;
import com.ventooth.swansong.shader.shaderobjects.CompositeShader;
import com.ventooth.swansong.shader.shaderobjects.GBufferShader;
import com.ventooth.swansong.shader.shaderobjects.ManagedShader;
import com.ventooth.swansong.shader.shaderobjects.ShadowShader;
import com.ventooth.swansong.shader.uniform.GeneralUniforms;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Cleanup;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;

import java.util.List;


@Builder(builderClassName = "Builder",
         access = AccessLevel.PRIVATE)
public class ShaderBinding {
    @Getter
    private ManagedShader current;

    public final ObjectList<GBufferShader> gBufferList;
    public final ObjectList<ManagedShader> loadedShaders;

    public final GBufferShader basic;
    public final GBufferShader skybasic;

    public final GBufferShader textured;
    public final GBufferShader spidereyes;
    public final GBufferShader beaconbeam;
    public final GBufferShader clouds;
    public final GBufferShader skytextured;

    public final GBufferShader textured_lit;
    public final GBufferShader entities;
    public final GBufferShader weather;
    public final GBufferShader hand;
    public final GBufferShader hand_water;

    public final GBufferShader terrain;
    public final GBufferShader water;
    public final GBufferShader block;
    public final GBufferShader portal;

    public final @Nullable ShadowShader shadow;

    public final BlitShader blit_color_identical;
    public final BlitShader blit_depth_identical;
    public final BlitShader blit_color_mismatched;
    public final BlitShader blit_depth_mismatched;

    public final @Nullable ObjectList<CompositeShader> deferredList;
    public final @Nullable ObjectList<CompositeShader> compositeList;
    public final @Nullable CompositeShader _final;

    public static ShaderBinding init(IShaderPool shaderPool, @Nullable WorldProvider dimension) throws ShaderException {
        val b = builder();
        try {
            b.basic(b.gBuffer(shaderPool, ShaderTypes.gbuffers_basic));
            b.skybasic(b.gBuffer(shaderPool, ShaderTypes.gbuffers_skybasic));

            b.textured(b.gBuffer(shaderPool, ShaderTypes.gbuffers_textured));
            b.spidereyes(b.gBuffer(shaderPool, ShaderTypes.gbuffers_spidereyes));
            b.beaconbeam(b.gBuffer(shaderPool, ShaderTypes.gbuffers_beaconbeam));
            b.clouds(b.gBuffer(shaderPool, ShaderTypes.gbuffers_clouds));
            b.skytextured(b.gBuffer(shaderPool, ShaderTypes.gbuffers_skytextured));

            b.textured_lit(b.gBuffer(shaderPool, ShaderTypes.gbuffers_textured_lit));
            b.entities(b.gBuffer(shaderPool, ShaderTypes.gbuffers_entities));
            b.weather(b.gBuffer(shaderPool, ShaderTypes.gbuffers_weather));
            b.hand(b.gBuffer(shaderPool, ShaderTypes.gbuffers_hand));
            b.hand_water(b.gBuffer(shaderPool, ShaderTypes.gbuffers_hand_water));

            b.terrain(b.gBuffer(shaderPool, ShaderTypes.gbuffers_terrain));
            b.water(b.gBuffer(shaderPool, ShaderTypes.gbuffers_water));
            b.block(b.gBuffer(shaderPool, ShaderTypes.gbuffers_block));
            b.portal(b.gBuffer(shaderPool, ShaderTypes.gbuffers_portal));

            b.shadow(b.safeInit(ShadowShader.load(shaderPool, ShaderTypes.shadow, false)));


            {
                // TODO: This code is ASS, session terminated...
                val internalLoader = new ShaderLoader(InternalShaderPack.INSTANCE, dimension);

                internalLoader.inExpectedShaders = ShaderTypes.internal;
                internalLoader.inAttribs = ObjectLists.emptyList();
                internalLoader.inParams = ShaderLoaderInParams.builder()
                                                              .handDepth(1)
                                                              .build();
                internalLoader.inShaderConfig = null;
                internalLoader.inEnvInfo = EnvInfo.get();
                internalLoader.inMcUniforms = GeneralUniforms.getFuncRegistry();

                internalLoader.load(null);
                @Cleanup val internalPool = internalLoader.borrowOutShaderPool();
                b.blit_color_identical(b.safeInit(BlitShader.load(internalPool,
                                                                  ShaderTypes.blit_color_identical,
                                                                  true)));
                b.blit_depth_identical(b.safeInit(BlitShader.load(internalPool,
                                                                  ShaderTypes.blit_depth_identical,
                                                                  true)));
                b.blit_color_mismatched(b.safeInit(BlitShader.load(internalPool,
                                                                   ShaderTypes.blit_color_mismatched,
                                                                   true)));
                b.blit_depth_mismatched(b.safeInit(BlitShader.load(internalPool,
                                                                   ShaderTypes.blit_depth_mismatched,
                                                                   true)));
                internalLoader.reset();
            }
            b.deferredList(b.composite(shaderPool, ShaderTypes.deferredList));
            b.compositeList(b.composite(shaderPool, ShaderTypes.compositeList));
            b._final(b.composite(shaderPool, ShaderTypes._final));
            return b.lockAndBuild();
        } catch (Throwable t) {
            safeDeinit(b.loadedShaders);
            throw t;
        }
    }

    public void deinit() {
        if (current != null) {
            current.end(true);
            current = null;
        }

        safeDeinit(loadedShaders);
    }

    public boolean use(@Nullable ManagedShader shader) {
        if (shader == current) {
            return false;
        }
        if (current != null) {
            current.end(shader == null);
            current = null;
        }

        if (shader != null) {
            shader.begin();
            current = shader;
            if (DebugMarker.isEnabled()) {
                DebugMarker.GENERIC.insert("Bind Shader: " + shader.loc());
            }
        }
        return true;
    }

    @SuppressWarnings("unused") //Lombok builder override
    private static class Builder {
        public Builder() {
            gBufferList = new ObjectArrayList<>();
            loadedShaders = new ObjectArrayList<>();
        }

        private ShaderBinding lockAndBuild() {
            gBufferList = ObjectLists.unmodifiable(gBufferList);
            loadedShaders = ObjectLists.unmodifiable(loadedShaders);
            return build();
        }

        private @NotNull GBufferShader gBuffer(IShaderPool pool, ResourceLocation loc) throws ShaderException {
            val shader = safeInit(GBufferShader.load(pool, loc, true));
            gBufferList.add(shader);
            return shader;
        }

        private @Nullable ObjectList<CompositeShader> composite(IShaderPool pool, List<ResourceLocation> locs) {
            val list = new ObjectArrayList<CompositeShader>();
            for (val loc : locs) {
                val shader = composite(pool, loc);
                if (shader != null) {
                    list.add(shader);
                }
            }
            if (list.isEmpty()) {
                return null;
            }
            return list;
        }

        private @Nullable CompositeShader composite(IShaderPool pool, ResourceLocation loc) {
            return safeInit(CompositeShader.load(pool, loc, false));
        }

        @Contract("!null -> !null; null -> null")
        private @Nullable <T extends ManagedShader> T safeInit(@Nullable T shader) {
            if (shader == null) {
                return null;
            }
            shader.init();
            loadedShaders.add(shader);
            return shader;
        }
    }

    private static void safeDeinit(List<? extends ManagedShader> shaders) {
        if (shaders == null) {
            return;
        }
        for (val shader : shaders) {
            if (shader != null) {
                shader.deinit();
            }
        }
    }
}
