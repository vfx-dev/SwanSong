/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.texbuf;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.debug.GLDebugGroups;
import com.ventooth.swansong.shader.BufferNameUtil;
import com.ventooth.swansong.shader.CompositeTextureData;
import com.ventooth.swansong.shader.DrawBuffers;
import com.ventooth.swansong.shader.Report;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShadersCompositeMesh;
import com.ventooth.swansong.shader.shaderobjects.CompositeShader;
import com.ventooth.swansong.sufrace.Framebuffer;
import com.ventooth.swansong.sufrace.Texture2D;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CompositePipeline implements Runnable {
    private final ObjectList<Runnable> steps;
    private final ColorBuffers aux;

    public static CompositePipeline buildPipeline(String name,
                                                  String auxPrefix,
                                                  @NotNull Map<CompositeTextureData, BufferConfig> gBufferConfigs,
                                                  @NotNull DrawBuffers buffers,
                                                  @NotNull ObjectList<CompositeShader> shaders,
                                                  int width,
                                                  int height,
                                                  @Nullable Report report) {
        val info =
                report == null ? null : report.pipelines.computeIfAbsent(name, n -> new Report.CompositePipelineInfo());
        val aux = genAux(auxPrefix, gBufferConfigs, shaders, width, height);
        if (info != null) {
            for (val tex : aux.getAllTextures()
                              .values()) {
                info.auxTextures.add(new Report.TextureInfo(tex));
            }
        }
        val steps = new ObjectArrayList<Runnable>();

        val auxTex = new BitSet();

        val inputMask = new BitSet();
        val inputs = new ObjectArrayList<CompositeTextureData>();
        val outputs = new ObjectArrayList<CompositeTextureData>();
        for (int i = 0, shadersSize = shaders.size(); i < shadersSize; i++) {
            var shader = shaders.get(i);
            inputMask.clear();
            inputs.clear();
            outputs.clear();
            for (val uniform : shader.getUniforms()
                                     .values()) {
                val index = BufferNameUtil.gbufferIndexFromName(uniform.name());
                if (index != null) {
                    val auxIndex = DrawBuffers.colorTexIndexFromTexture(index);
                    if (auxIndex >= 0) {
                        inputMask.set(index.gpuIndex());
                    }
                    inputs.add(index);
                }
            }
            val iter = shader.renderTargets()
                             .intIterator();
            while (iter.hasNext()) {
                val index = iter.nextInt();
                val id = DrawBuffers.textureFromColorTexIndex(index);
                outputs.add(id);
            }
            val inputTex = new EnumMap<CompositeTextureData, Texture2D>(CompositeTextureData.class);
            val outputTex = new EnumMap<CompositeTextureData, Texture2D>(CompositeTextureData.class);

            for (val input: inputs) {
                val auxIndex = DrawBuffers.colorTexIndexFromTexture(input);
                Texture2D tex;
                if (auxIndex >= 0 && auxTex.get(auxIndex)) {
                    tex = aux.get(input);
                } else {
                    tex = buffers.getByID(input);
                }

                // TODO: This needs to be fixed properly, happens if a shader samples a gbuffer it does not write to.
                if (tex != null) {
                    inputTex.put(input, tex);
                }
            }

            for (val output: outputs) {
                val auxIndex = DrawBuffers.colorTexIndexFromTexture(output);
                assert auxIndex >= 0;
                val outIntoAux = inputMask.get(output.gpuIndex()) && !auxTex.get(auxIndex);
                auxTex.set(auxIndex, outIntoAux);
                val tex = outIntoAux ? aux.get(output) : buffers.getByID(output);
                outputTex.put(output, tex);
            }

            if (shader.framebuffer != null) {
                shader.framebuffer.deinit();
                shader.framebuffer = null;
            }

            val mipInputs = new ObjectArrayList<Texture2D>();
            for (val mip: shader.mipmapEnabled()) {
                val mipInput = inputTex.get(mip);
                if (mipInput == null) {
                    Share.log.debug("Failed to setup composite mipmap: [{}->colortex{}]", shader.loc(), mip);
                } else {
                    mipInputs.add(mipInput);
                }
            }

            if (info != null) {
                val stage = new Report.CompositeStageInfo(shader.srcPath());
                info.stages.add(stage);
                for (val mipmap : mipInputs) {
                    stage.mipmaps.add(mipmap.name());
                }
                for (val input : inputTex.entrySet()) {
                    stage.inputs.put(input.getKey(),
                                     input.getValue()
                                          .name());
                }
                for (val output : outputTex.entrySet()) {
                    stage.outputs.put(output.getKey(),
                                      output.getValue()
                                            .name());
                }
            }

            if (!mipInputs.isEmpty()) {
                steps.add(new MipmapStep(mipInputs));
            }

            shader.framebuffer = Framebuffer.create(name + i, Collections.unmodifiableMap(outputTex));
            steps.add(new CompositeStep(Collections.unmodifiableMap(inputTex), shader));
        }
        val maxAux = auxTex.length();
        if (maxAux > 0) {
            val src = new Int2ObjectArrayMap<Texture2D>();
            val dst = new Int2ObjectArrayMap<Texture2D>();

            val pb = info == null ? null : new Report.CompositePostBlit();
            for (int i = 0; i < maxAux; i++) {
                if (auxTex.get(i)) {
                    val id = DrawBuffers.textureFromColorTexIndex(i);
                    val a = aux.get(id);
                    val g = buffers.getByID(id);
                    if (a != null && g != null) {
                        if (pb != null) {
                            pb.from.add(a.name());
                            pb.to.add(g.name());
                        }
                        src.put(i, a);
                        dst.put(i, g);
                    }
                }
            }
            if (pb != null) {
                info.postBlit = pb;
            }
            steps.add(new BlitStep(Int2ObjectMaps.unmodifiable(src), Int2ObjectMaps.unmodifiable(dst)));
        }
        return new CompositePipeline(ObjectLists.unmodifiable(steps), aux);
    }

    @Override
    public void run() {
        for (val step : steps) {
            step.run();
        }
    }

    public void resize(int width, int height) {
        aux.resize(width, height);
    }

    public void deinit() {
        aux.deinit();
    }

    private static ColorBuffers genAux(String prefix,
                                       Map<CompositeTextureData, BufferConfig> gBufferConfigs,
                                       ObjectList<CompositeShader> shaders,
                                       int width,
                                       int height) {
        val auxNeeded = new BitSet();

        val gbufferUniforms = new BitSet();
        //Determine auxiliary buffer requirements
        for (val shader : shaders) {
            gbufferUniforms.clear();
            for (val uniform : shader.getUniforms()
                                     .values()) {
                val id = BufferNameUtil.gbufferIndexFromName(uniform.name());
                if (id != null) {
                    gbufferUniforms.set(id.gpuIndex());
                }
            }
            val iter = shader.renderTargets()
                             .intIterator();
            while (iter.hasNext()) {
                val index = iter.nextInt();
                val id = DrawBuffers.textureFromColorTexIndex(index);
                if (id != null) {
                    if (gbufferUniforms.get(id.gpuIndex())) {
                        auxNeeded.set(index);
                    }
                }
            }
        }
        // Allocate auxiliary buffer
        val configs = new EnumMap<CompositeTextureData, BufferConfig>(CompositeTextureData.class);

        for (val entry : gBufferConfigs.entrySet()) {
            val id = entry.getKey();
            val index = DrawBuffers.colorTexIndexFromTexture(id);
            if (auxNeeded.get(index)) {
                configs.put(id, entry.getValue());
            }
        }

        return new ColorBuffers(configs, prefix, width, height);
    }

    @RequiredArgsConstructor
    private static class CompositeStep implements Runnable {
        private final Map<CompositeTextureData, Texture2D> inputs;
        private final CompositeShader shader;

        @Override
        public void run() {
            ShaderEngine.useCompositeShader(shader);
            ShaderEngine.bindCompositeTextures(inputs);

            GLDebugGroups.RENDER_COMPOSITE_DRAW.push();
            ShadersCompositeMesh.drawWithColor();
            GLDebugGroups.RENDER_COMPOSITE_DRAW.pop();
        }
    }

    @RequiredArgsConstructor
    private static class MipmapStep implements Runnable {
        private final ObjectList<Texture2D> mipInputs;

        @Override
        public void run() {
            GLDebugGroups.push(GLDebugGroups.GEN_COMPOSITE_MIPS);
            ShaderEngine.genMipmaps(mipInputs);
            GLDebugGroups.pop(GLDebugGroups.GEN_COMPOSITE_MIPS);
        }
    }

    @RequiredArgsConstructor
    private static class BlitStep implements Runnable {
        private final Int2ObjectMap<Texture2D> src;
        private final Int2ObjectMap<Texture2D> dst;

        @Override
        public void run() {
            ShaderEngine.blitColors(src, dst);
        }
    }
}
