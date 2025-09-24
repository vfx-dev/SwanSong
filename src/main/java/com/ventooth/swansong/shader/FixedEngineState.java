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
import com.ventooth.swansong.Share;
import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.pbr.PBRTextureEngine;
import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.resources.pack.DefaultShaderPack;
import com.ventooth.swansong.resources.pack.InternalShaderPack;
import com.ventooth.swansong.resources.pack.ShaderPack;
import com.ventooth.swansong.shader.config.ConfigEntry;
import com.ventooth.swansong.shader.loader.MultiShaderPool;
import com.ventooth.swansong.shader.loader.ShaderLoader;
import com.ventooth.swansong.shader.loader.ShaderLoaderInParams;
import com.ventooth.swansong.shader.loader.ShaderLoaderOutParams;
import com.ventooth.swansong.shader.mappings.BlockIDRemapper;
import com.ventooth.swansong.shader.texbuf.BufferConfig;
import com.ventooth.swansong.shader.uniform.CompiledUniforms;
import com.ventooth.swansong.shader.uniform.GeneralUniforms;
import com.ventooth.swansong.shader.uniform.UniformGetterDanglingWires;
import com.ventooth.swansong.todo.tess.DanglingWiresTess;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.client.resources.Locale;
import net.minecraft.world.WorldProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Builder(access = AccessLevel.PRIVATE)
class FixedEngineState {
    public final @Nullable WorldProvider dimension;

    public final @NotNull ShaderPack pack;

    public final @NotNull ShaderBinding manager;

    public final @NotNull Map<CompositeTextureData, BufferConfig> colorDrawBufferConfigs;

    public final @Nullable BlockIDRemapper remapper;

    public final @Nullable DepthSampler depthSampler;

    public final @Nullable ShadowProperties shadow;

    // Null when no compiled uniforms were present
    public final @Nullable CompiledUniforms compiledUniforms;

    public final @NotNull
    @Unmodifiable ObjectList<ShaderLoaderOutParams.StagedTexture> textures;

    public final @Nullable String noiseTexPath;
    public final @Nullable Integer noiseTexSize;

    public final ConfigEntry.RootScreen configScreen;
    public final Locale locale;

    private static ShaderLoader createLoader(ShaderPack pack, @Nullable WorldProvider dimension) {
        val loader = new ShaderLoader(pack, dimension);
        loader.inExpectedShaders = ShaderTypes.general;
        loader.inAttribs = DanglingWiresTess.attribs;
        loader.inParams = ShaderLoaderInParams.builder()
                                              .handDepth(ShadersConfig.HandDepth.get())
                                              .renderQuality(ShadersConfig.RenderQuality.get())
                                              .shadowQuality(ShadersConfig.ShadowQuality.get())
                                              .build();
        loader.inShaderConfig = ShaderPackManager.readShaderPackConfig();
        loader.inEnvInfo = EnvInfo.get();
        loader.inMcUniforms = GeneralUniforms.getFuncRegistry();
        return loader;
    }

    public static @NotNull FixedEngineState init(@Nullable WorldProvider dimension, @Nullable Report report) {
        val b = builder();
        b.dimension = dimension;
        try {
            ShaderPackManager.cleanDebugDir();
        } catch (IOException ignored) {
        }

        b.pack = ShaderPackManager.createShaderPack();
        b.remapper = BlockIDRemapper.createRemapper(b.pack);

        if (report != null) {
            report.name = b.pack.name();
        }

        val mainLoader = createLoader(b.pack, dimension);

        val loaders = new ArrayList<ShaderLoader>();
        if (b.pack != DefaultShaderPack.INSTANCE) {
            loaders.add(createLoader(DefaultShaderPack.INSTANCE, dimension));
        }
        loaders.add(createLoader(InternalShaderPack.INSTANCE, dimension));

        mainLoader.load(report);

        b.configScreen = mainLoader.outConfigScreen;
        b.locale = mainLoader.outLocale;

        b.compiledUniforms = mainLoader.outCompiledUniforms;

        if (b.compiledUniforms != null) {
            UniformGetterDanglingWires.customUniforms = b.compiledUniforms.wrapUniforms();
        } else {
            UniformGetterDanglingWires.customUniforms = null;
        }

        val outParams = mainLoader.outParams;

        b.textures = outParams.textures;

        b.noiseTexPath = outParams.noiseTexture;
        b.noiseTexSize = outParams.noiseTextureResolution;

        PBRTextureEngine.init();

        // TODO: Check if the shader actually needs center depth before populating, this call is not free.
        if (true) {
            b.depthSampler = new DepthSampler();
            b.depthSampler.init();
        }

        try (val shaderPool = new MultiShaderPool(mainLoader.borrowOutShaderPool(), loaders, report)) {
            b.manager = ShaderBinding.init(shaderPool, dimension);

            if (b.manager.shadow != null) {
                b.shadow = ShadowProperties.from(outParams, ShadersConfig.ShadowQuality.get());
            }
            ShaderState.applyParams(outParams);
        } catch (ShaderException e) {
            throw new AssertionError(e);
        }

        val configBuilders = new EnumMap<CompositeTextureData, BufferConfig.Builder>(CompositeTextureData.class);

        for (val shader : b.manager.loadedShaders) {
            val renderTargets = shader.renderTargets();
            val size = renderTargets.size();
            val info = report == null ? null : report.foundShaders.computeIfAbsent(shader.srcPath(),
                                                                                   path -> new Report.ShaderInfo());
            for (var i = 0; i < size; i++) {
                val renderTargetIndex = renderTargets.getInt(i);
                val renderTarget = DrawBuffers.textureFromColorTexIndex(renderTargetIndex);
                if (renderTarget == null) {
                    Share.log.warn("Invalid render target index: {}", renderTargetIndex);
                    continue;
                }
                val configBuilder = configBuilders.computeIfAbsent(renderTarget,
                                                                   index -> BufferConfig.builder()
                                                                                        .name(index.name()));
                if (info != null) {
                    info.renderTargets.add(configBuilder.name);
                }
            }
        }

        for (val disabled : outParams.bufferClearDisabled) {
            val index = BufferNameUtil.gbufferIndexFromName(disabled);
            if (index == null) {
                Share.log.info("INVALID color gbuffer name: {} tried to set disabled clear", disabled);
                continue;
            }
            val builder = configBuilders.get(index);
            if (builder == null) {
                Share.log.info("Ignored gbuffer color thing: {}  clear (not used?)", disabled);
                continue;
            }
            builder.clear(false);
        }

        for (val entry : Object2ObjectMaps.fastIterable(outParams.bufferFormat)) {
            val nameStr = entry.getKey();
            val formatStr = entry.getValue();

            val index = BufferNameUtil.gbufferIndexFromName(nameStr);
            if (index == null) {
                Share.log.warn("INVALID color gbuffer name: {} tried to set format={}", nameStr, formatStr);
                continue;
            }

            val builder = configBuilders.get(index);
            if (builder == null) {
                if (report != null) {
                    report.drawBuffers.put(index.name(), new Report.DrawBufferInfo(true, formatStr));
                }
                continue;
            }

            val format = BufferNameUtil.gbufferFormatFromName(formatStr);
            if (format == -1) {
                Share.log.warn("Color gbuffer: {} tried to set INVALID format={}", formatStr, nameStr);
                continue;
            }
            if (report != null) {
                report.drawBuffers.put(index.name(), new Report.DrawBufferInfo(false, formatStr));
            }
            builder.format(format);
        }

        val bufferConfigs = new EnumMap<CompositeTextureData, BufferConfig>(CompositeTextureData.class);

        configBuilders.forEach((tex, builder) -> {
            bufferConfigs.put(tex, builder.build());
        });

        b.colorDrawBufferConfigs = Collections.unmodifiableMap(bufferConfigs);

        loaders.forEach(ShaderLoader::reset);

        return b.build();
    }

    public void deinit() {
        if (depthSampler != null) {
            depthSampler.deinit();
        }

        PBRTextureEngine.deinit();
        manager.deinit();
    }
}
