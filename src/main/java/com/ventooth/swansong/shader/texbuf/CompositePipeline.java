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
import com.ventooth.swansong.mixin.interfaces.ShaderGameSettings;
import com.ventooth.swansong.shader.BufferNameUtil;
import com.ventooth.swansong.shader.CompositeTextureData;
import com.ventooth.swansong.shader.DrawBuffers;
import com.ventooth.swansong.shader.Report;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShadersCompositeMesh;
import com.ventooth.swansong.shader.shaderobjects.CompositeShader;
import com.ventooth.swansong.sufrace.Framebuffer;
import com.ventooth.swansong.sufrace.FramebufferAttachment;
import com.ventooth.swansong.sufrace.Texture2D;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;

import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CompositePipeline implements Runnable {
    private final ObjectList<Runnable> steps;
    private final @Nullable ColorBuffers aux;

    public static CompositePipeline buildPipelineFinal(@NotNull DrawBuffers buffers,
                                                       @NotNull ObjectList<CompositeShader> shaders,
                                                       @Nullable Report report) {
        val info = report == null ? null
                                  : report.pipelines.computeIfAbsent("final", n -> new Report.CompositePipelineInfo());
        val steps = new ObjectArrayList<Runnable>();

        val inputMask = new BitSet();
        for (val shader : shaders) {
            inputMask.clear();
            val inputTex = getInputTexFinal(buffers, shader, inputMask);

            final var mipInputs = getMipInputs(shader, inputTex);

            if (!mipInputs.isEmpty()) {
                steps.add(new MipmapStep(mipInputs));
            }

            if (info != null) {
                recordShaderIOInfo(shader, info, mipInputs, inputTex, null);
            }

            steps.add(new FinalStep(Collections.unmodifiableMap(inputTex), shader));
        }
        return new CompositePipeline(ObjectLists.unmodifiable(steps), null);
    }

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
        for (int i = 0, shadersSize = shaders.size(); i < shadersSize; i++) {
            val shader = shaders.get(i);
            inputMask.clear();
            val inputTex = getInputTex(buffers, shader, inputMask, auxTex, aux);
            val outputTex = getOutputTex(buffers, shader, inputMask, auxTex, aux);

            if (shader.framebuffer != null) {
                shader.framebuffer.deinit();
                shader.framebuffer = null;
            }

            final var mipInputs = getMipInputs(shader, inputTex);

            if (!mipInputs.isEmpty()) {
                steps.add(new MipmapStep(mipInputs));
            }

            if (info != null) {
                recordShaderIOInfo(shader, info, mipInputs, inputTex, outputTex);
            }

            shader.framebuffer = Framebuffer.create(name + i, Collections.unmodifiableMap(outputTex));
            steps.add(new CompositeStep(Collections.unmodifiableMap(inputTex), shader));
        }
        genAuxPostBlit(buffers, auxTex, info, aux, steps);
        return new CompositePipeline(ObjectLists.unmodifiable(steps), aux);
    }

    private static void recordShaderIOInfo(CompositeShader shader,
                                           Report.CompositePipelineInfo info,
                                           ObjectArrayList<Texture2D> mipInputs,
                                           EnumMap<CompositeTextureData, Texture2D> inputTex,
                                           @Nullable EnumMap<FramebufferAttachment, Texture2D> outputTex) {
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
        if (outputTex != null) {
            for (val entry: outputTex.entrySet()) {
                stage.outputs.put(entry.getKey(),
                                  entry.getValue()
                                       .name());
            }
        }
    }

    private static void genAuxPostBlit(@NotNull DrawBuffers buffers,
                                       BitSet auxTex,
                                       Report.CompositePipelineInfo info,
                                       ColorBuffers aux,
                                       ObjectArrayList<Runnable> steps) {
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
    }

    private static @NotNull ObjectArrayList<Texture2D> getMipInputs(CompositeShader shader,
                                                                    EnumMap<CompositeTextureData, Texture2D> inputTex) {
        val mipInputs = new ObjectArrayList<Texture2D>();
        for (val mip : shader.mipmapEnabled()) {
            val mipInput = inputTex.get(mip);
            if (mipInput == null) {
                Share.log.debug("Failed to setup composite mipmap: [{}->colortex{}]", shader.loc(), mip);
            } else {
                mipInputs.add(mipInput);
            }
        }
        return mipInputs;
    }

    private static @NotNull EnumMap<FramebufferAttachment, Texture2D> getOutputTex(@NotNull DrawBuffers buffers,
                                                                              CompositeShader shader,
                                                                              BitSet inputMask,
                                                                              BitSet auxTex,
                                                                              ColorBuffers aux) {
        val outputs = new ObjectArrayList<CompositeTextureData>();
        val iter = shader.renderTargets()
                         .intIterator();
        while (iter.hasNext()) {
            val index = iter.nextInt();
            val id = DrawBuffers.textureFromColorTexIndex(index);
            outputs.add(id);
        }
        val outputTex = new EnumMap<FramebufferAttachment, Texture2D>(FramebufferAttachment.class);

        int i = 0;
        for (val output : outputs) {
            val auxIndex = DrawBuffers.colorTexIndexFromTexture(output);
            assert auxIndex >= 0;
            val outIntoAux = inputMask.get(output.gpuIndex()) && !auxTex.get(auxIndex);
            auxTex.set(auxIndex, outIntoAux);
            val tex = outIntoAux ? aux.get(output) : buffers.getByID(output);
            val att = FramebufferAttachment.fromColorIndex(i);
            i++;
            //TODO proper error handling
            if (att != null) {
                outputTex.put(att, tex);
            }
        }
        return outputTex;
    }

    private static @NotNull EnumMap<CompositeTextureData, Texture2D> getInputTex(@NotNull DrawBuffers buffers,
                                                                                 CompositeShader shader,
                                                                                 BitSet inputMask,
                                                                                 BitSet auxTex,
                                                                                 ColorBuffers aux) {
        val inputs = scanInputs(shader, inputMask);
        val inputTex = new EnumMap<CompositeTextureData, Texture2D>(CompositeTextureData.class);

        for (val input : inputs) {
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
        return inputTex;
    }

    private static @NotNull EnumMap<CompositeTextureData, Texture2D> getInputTexFinal(@NotNull DrawBuffers buffers,
                                                                                      CompositeShader shader,
                                                                                      BitSet inputMask) {
        val inputs = scanInputs(shader, inputMask);
        val inputTex = new EnumMap<CompositeTextureData, Texture2D>(CompositeTextureData.class);

        for (val input : inputs) {
            Texture2D tex = buffers.getByID(input);

            // TODO: This needs to be fixed properly, happens if a shader samples a gbuffer it does not write to.
            if (tex != null) {
                inputTex.put(input, tex);
            }
        }
        return inputTex;
    }

    private static @NotNull ObjectArrayList<CompositeTextureData> scanInputs(CompositeShader shader, BitSet inputMask) {
        val inputs = new ObjectArrayList<CompositeTextureData>();
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
        return inputs;
    }

    @Override
    public void run() {
        for (val step : steps) {
            step.run();
        }
    }

    public void resize(int width, int height) {
        if (aux != null) {
            aux.resize(width, height);
        }
    }

    public void deinit() {
        if (aux != null) {
            aux.deinit();
        }
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

    @RequiredArgsConstructor
    private static class FinalStep implements Runnable {
        private final Map<CompositeTextureData, Texture2D> inputs;
        private final CompositeShader shader;

        @Override
        public void run() {
            GLDebugGroups.RENDER_COMPOSITE_FINAL.push();
            ShaderEngine.useCompositeShader(shader);
            ShaderEngine.bindCompositeTextures(inputs);
            val anaglyph = ((ShaderGameSettings) Minecraft.getMinecraft().gameSettings).swan$anaglyph();
            GLDebugGroups.RENDER_COMPOSITE_FINAL_DRAW.push();
            if (anaglyph != 0) {
                ShadersCompositeMesh.drawWithAnaglyphField(EntityRenderer.anaglyphField);
            } else {
                ShadersCompositeMesh.drawWithColor();
            }
            GLDebugGroups.RENDER_COMPOSITE_FINAL_DRAW.pop();
            GLDebugGroups.RENDER_COMPOSITE_FINAL.pop();
        }
    }
}
