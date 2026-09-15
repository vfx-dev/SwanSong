/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.preprocessor;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.shader.preprocessor.macro.MacroInterpreter;
import com.ventooth.swansong.shader.preprocessor.util.CodePrinter;
import com.ventooth.swansong.shader.preprocessor.util.RecursiveIncluder;
import com.ventooth.swansong.shader.preprocessor.util.StringUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ShaderPreprocessor {
    private final FSProvider fs;
    private RecyclableOutputContext context;

    //region api

    public @Nullable String getString(String path,
                                      boolean glsl,
                                      Consumer<ShaderStage1Meta> stage1Configurer,
                                      Consumer<ShaderStage2Meta> stage2Configurer) {
        val stage2 = runStage2(path, glsl, stage1Configurer, stage2Configurer);
        if (stage2 == null) {
            return null;
        } else {
            return stage2.getString();
        }
    }

    public byte @Nullable [] getBytes(String path,
                                      boolean glsl,
                                      Consumer<ShaderStage1Meta> stage1Configurer,
                                      Consumer<ShaderStage2Meta> stage2Configurer) {
        val stage2 = runStage2(path, glsl, stage1Configurer, stage2Configurer);
        if (stage2 == null) {
            return null;
        } else {
            return stage2.getBytes();
        }
    }

    /**
     * The previously returned buffer gets invalidated when this function is called again.
     * <p>
     * This function is NOT thread-safe.
     */
    @SneakyThrows
    public @Nullable ByteBuffer getNativeBuffer(String path,
                                                boolean glsl,
                                                Consumer<ShaderStage1Meta> stage1Configurer,
                                                Consumer<ShaderStage2Meta> stage2Configurer,
                                                boolean nullTerminator) {
        val stage2 = runStage2(path, glsl, stage1Configurer, stage2Configurer);
        if (stage2 == null) {
            return null;
        } else {
            return stage2.getNativeBuffer(nullTerminator);
        }
    }

    public PreprocessorStage1Suspend runStage1(String path, boolean glsl, Consumer<ShaderStage1Meta> stage1Configurer) {
        val includer = new RecursiveIncluder(fs);
        if (!includer.read(path)) {
            return null;
        }

        val rawCode = markMultilineComments(includer.lines());
        val sourceIndices = includer.sourceIndices();

        val stage1DefOptions = Option.Define.find(rawCode, false);
        val stage1DefOptionsNamed = new HashMap<String, Option>();
        val stage1DefOptionsList = deduplicateOptions(stage1DefOptions, stage1DefOptionsNamed);
        val stage1ConstOptions = glsl ? Option.Const.find(rawCode, true) : null;
        val stage1ConstOptionsNamed = glsl ? new HashMap<String, Option>() : null;
        val stage1ConstOptionsList = glsl ? deduplicateOptions(stage1ConstOptions, stage1ConstOptionsNamed) : null;
        val stage1 = new ShaderStage1Meta(stage1DefOptionsList,
                                          Collections.unmodifiableMap(stage1DefOptionsNamed),
                                          glsl ? stage1ConstOptionsList : null,
                                          glsl ? Collections.unmodifiableMap(stage1ConstOptionsNamed) : null,
                                          sourceIndices);
        stage1Configurer.accept(stage1);
        return new PreprocessorStage1Suspend(stage1, stage1DefOptions, rawCode, glsl);
    }

    private static byte @NotNull [] sourceToByteArray(PreprocessedCode sources) {
        val out = new ByteArrayOutputStream();
        try (val pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), false)) {
            CodePrinter.printCode(sources.prelude(), sources.code(), sources.opts(), pw);
        }
        return out.toByteArray();
    }

    private static @NotNull String sourceToString(PreprocessedCode sources) {
        val sprint = new StringWriter();
        try (val pw = new PrintWriter(sprint, false)) {
            CodePrinter.printCode(sources.prelude(), sources.code(), sources.opts(), pw);
        }
        return sprint.toString();
    }

    private @NotNull ByteBuffer sourceToNativeBuffer(PreprocessedCode sources, boolean nullTerminator) {
        var context = this.context;
        if (context == null) {
            context = this.context = new RecyclableOutputContext();
        }
        context.reset();
        val out = context.outputStream;
        try (val pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), false)) {
            CodePrinter.printCode(sources.prelude(), sources.code(), sources.opts(), pw);
        }
        if (nullTerminator) {
            out.write(0);
        }
        return context.toNativeBuffer();
    }

    //endregion

    private @Nullable PreprocessorStage2Suspend runStage2(String path,
                                                          boolean glsl,
                                                          Consumer<ShaderStage1Meta> stage1Configurer,
                                                          Consumer<ShaderStage2Meta> stage2Configurer) {
        val stage1 = runStage1(path, glsl, stage1Configurer);
        if (stage1 == null) {
            return null;
        }
        return stage1.runStage2(stage2Configurer);
    }

    @RequiredArgsConstructor
    public final class PreprocessorStage1Suspend {
        private final ShaderStage1Meta meta;
        private final Int2ObjectMap<Option> defOptions;
        private final List<TaggedLine> rawCode;
        private final boolean glsl;

        public PreprocessorStage2Suspend runStage2(Consumer<ShaderStage2Meta> stage2Configurer) {
            val stage1Macros = meta.extraMacros.get();

            val interpreterResult = MacroInterpreter.interpret(defOptions,
                                                               rawCode,
                                                               meta.fileNameIndices,
                                                               stage1Macros,
                                                               glsl);

            val glslData = glsl ? Objects.requireNonNull(interpreterResult.glsl()) : null;
            val preprocessedCode = interpreterResult.code();
            val stage2DefOptions = interpreterResult.options();

            val prelude = new ArrayList<String>(1 +
                                                (glsl ? glslData.extensions()
                                                                .size() : 0) +
                                                stage1Macros.size());
            if (glsl) {
                val ver = glslData.version();
                if (ver == null) {
                    Share.log.error("#version macro missing for GLSL!");
                } else {
                    prelude.add(ver);
                }
                prelude.addAll(glslData.extensions());
            }
            for (val macro : stage1Macros.entrySet()) {
                val macroValue = macro.getValue();
                if (macroValue.type() == Option.ValueType.Toggle) {
                    if (macroValue == Option.Value.Bool.True) {
                        prelude.add("#define " + macro.getKey());
                    }
                } else {
                    prelude.add("#define " + macro.getKey() + " " + macro.getValue());
                }
            }

            val stage2DefOptionsNamed = new HashMap<String, Option>();
            val stage2DefOptionsList = deduplicateOptions(defOptions, stage2DefOptionsNamed);
            val stage2ConstOptions = glsl ? Option.Const.find(preprocessedCode, false) : null;
            val stage2ConstOptionsNamed = glsl ? new HashMap<String, Option>() : null;
            val stage2ConstOptionsList = glsl ? deduplicateOptions(stage2ConstOptions, stage2ConstOptionsNamed) : null;
            val stage2 = new ShaderStage2Meta(glsl ? glslData.renderTargets() : null,
                                              stage2DefOptionsList,
                                              stage2DefOptionsNamed,
                                              stage2ConstOptionsList,
                                              stage2ConstOptionsNamed);
            stage2Configurer.accept(stage2);
            return new PreprocessorStage2Suspend(stage2DefOptions, stage2ConstOptions, prelude, preprocessedCode);
        }
    }

    @RequiredArgsConstructor
    public final class PreprocessorStage2Suspend {
        private final Int2ObjectMap<Option> defOptions;
        private final Int2ObjectMap<Option> constOptions;
        private final List<String> prelude;
        private final List<TaggedLine> preprocessedCode;


        public @NotNull String getString() {
            return sourceToString(getSources());
        }

        public byte @NotNull [] getBytes() {
            return sourceToByteArray(getSources());
        }

        /**
         * The previously returned buffer gets invalidated when this function is called again.
         * <p>
         * This function is NOT thread-safe.
         */
        @SneakyThrows
        public @NotNull ByteBuffer getNativeBuffer(boolean nullTerminator) {
            return sourceToNativeBuffer(getSources(), nullTerminator);
        }

        private PreprocessedCode getSources() {
            val mergedOpts = new Int2ObjectOpenHashMap<Option>();

            mergedOpts.putAll(defOptions);
            if (constOptions != null) {
                mergedOpts.putAll(constOptions);
            }

            return new PreprocessedCode(prelude, preprocessedCode, mergedOpts);
        }
    }

    private static @Unmodifiable List<Option> deduplicateOptions(Int2ObjectMap<Option> opts,
                                                                 Map<String, Option> named) {
        val optList = new ArrayList<Option>();
        for (val entry : opts.int2ObjectEntrySet()) {
            val opt = entry.getValue();
            val n = opt.uniqueName();
            if (named.containsKey(n)) {
                val opt2 = named.get(n);
                if (opt.legalValues.equals(opt2.legalValues) &&
                    !Option.valueMatches(opt.getCurrentValue(), opt2.getCurrentValue())) {
                    Share.log.warn("Mismatched option values: {}", opt.name);
                }
                if (opt.isConfigurable() && opt2.isConfigurable()) {
                    entry.setValue(opt2);
                } else if (!opt.isConfigurable() && !opt2.isConfigurable()) {
                    optList.add(opt);
                    named.put(n, opt);
                }
            } else {
                named.put(n, opt);
                optList.add(opt);
            }
        }
        return Collections.unmodifiableList(optList);
    }

    private static @Unmodifiable List<TaggedLine> markMultilineComments(List<TaggedLine> lines) {
        boolean isInComment = false;
        boolean newComment = false;
        lines = new ArrayList<>(lines);
        for (int i = 0; i < lines.size(); i++) {
            val line = lines.get(i);
            val txt = line.text();
            if (isInComment) {
                int commentEnd = txt.indexOf("*/");
                if (commentEnd < 0) {
                    lines.set(i, line.withTag(TaggedLine.Tag.MultilineComment));
                    newComment = false;
                    continue;
                }
                if (newComment) {
                    newComment = false;
                    if (commentEnd == 1 && txt.charAt(0) == '/') {
                        commentEnd = txt.indexOf("*/", commentEnd + 2);
                        if (commentEnd < 0) {
                            lines.set(i, line.withTag(TaggedLine.Tag.MultilineComment));
                            continue;
                        }
                    }
                }
                val len = txt.length();
                isInComment = false;
                if (commentEnd == len - 2 || StringUtils.firstNonWhitespace(txt, commentEnd + 2, len) == -1) {
                    lines.set(i,
                              line.withTextAndTag(txt.substring(0, commentEnd + 2), TaggedLine.Tag.MultilineComment));
                    continue;
                }
                lines.set(i,
                          line.withTextAndTag(txt.substring(0, commentEnd + 2),
                                              false,
                                              TaggedLine.Tag.MultilineComment));
                lines.add(i + 1, line.withText(txt.substring(commentEnd + 2)));
            } else {
                int commentStart = txt.indexOf("/*");
                if (commentStart < 0) {
                    continue;
                }
                if (commentStart == 0) {
                    isInComment = true;
                    newComment = true;
                    i--;
                    continue;
                }
                if (txt.charAt(commentStart - 1) == '/') {
                    continue;
                }
                isInComment = true;
                newComment = true;
                lines.set(i, line.withText(txt.substring(0, commentStart), false));
                lines.add(i + 1, line.withText(txt.substring(commentStart)));
            }
        }
        return Collections.unmodifiableList(lines);
    }

    private static class RecyclableOutputContext {
        private final ExposedByteArrayOutputStream outputStream = new ExposedByteArrayOutputStream();
        private ByteBuffer buf;

        private void reset() {
            outputStream.reset();
        }

        private ByteBuffer toNativeBuffer() {
            val size = outputStream.size();
            if (buf == null || size > buf.capacity()) {
                buf = ByteBuffer.allocateDirect(size);
            }
            buf.clear();
            buf.put(outputStream.buffer(), 0, size);
            buf.flip();
            return buf;
        }
    }

    private static class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
        public ExposedByteArrayOutputStream() {
            super(4096);
        }

        private byte[] buffer() {
            return buf;
        }
    }

    //TODO convert to record
    private static final class PreprocessedCode {
        private final List<String> prelude;
        private final List<TaggedLine> code;
        private final Int2ObjectMap<Option> opts;

        private PreprocessedCode(List<String> prelude, List<TaggedLine> code, Int2ObjectMap<Option> opts) {
            this.prelude = prelude;
            this.code = code;
            this.opts = opts;
        }

        public List<String> prelude() {
            return prelude;
        }

        public List<TaggedLine> code() {
            return code;
        }

        public Int2ObjectMap<Option> opts() {
            return opts;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (PreprocessedCode) obj;
            return Objects.equals(this.prelude, that.prelude) &&
                   Objects.equals(this.code, that.code) &&
                   Objects.equals(this.opts, that.opts);
        }

        @Override
        public int hashCode() {
            return Objects.hash(prelude, code, opts);
        }

        @Override
        public String toString() {
            return "PreprocessedCode[" + "prelude=" + prelude + ", " + "code=" + code + ", " + "opts=" + opts + ']';
        }

    }
}
