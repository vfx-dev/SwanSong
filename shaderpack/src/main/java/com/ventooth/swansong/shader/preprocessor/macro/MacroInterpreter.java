/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.preprocessor.macro;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.mathparser.ParserException;
import com.ventooth.swansong.shader.preprocessor.Option;
import com.ventooth.swansong.shader.preprocessor.TaggedLine;
import com.ventooth.swansong.shader.preprocessor.util.RenderTargetParser;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MacroInterpreter {
    //inputs
    private final Int2ObjectMap<Option> inOptions;
    private final List<TaggedLine> inCode;
    private final List<String> sourceIndices;
    private final boolean glsl;

    //state
    private final Map<String, Option.Value> defines = new HashMap<>();
    private final BitSet disabled = new BitSet();
    private final BitSet elifChainBranchTaken = new BitSet();
    private int depth = 0;

    //output
    private String outVersion = null;
    private final List<String> outExtensions;
    private final Int2ObjectMap<Option> outOptions = new Int2ObjectOpenHashMap<>();
    private final List<TaggedLine> outCode = new ArrayList<>();
    private @Nullable IntList outRenderTargets = null;

    public static Result interpret(Int2ObjectMap<Option> inOptions,
                                   List<TaggedLine> inCode,
                                   List<String> sourceIndices,
                                   Map<String, Option.Value> externalDefines,
                                   boolean glsl) {
        val interpreter = new MacroInterpreter(inOptions, inCode, sourceIndices, glsl);
        interpreter.defines.putAll(externalDefines);
        interpreter.execute();
        return new Result(glsl ? new Result.GLSL(interpreter.outVersion,
                                                 Collections.unmodifiableList(interpreter.outExtensions),
                                                 interpreter.outRenderTargets) : null,
                          Collections.unmodifiableList(interpreter.outCode),
                          Int2ObjectMaps.unmodifiable(interpreter.outOptions));
    }

    private MacroInterpreter(Int2ObjectMap<Option> inOptions,
                             List<TaggedLine> inCode,
                             List<String> sourceIndices,
                             boolean glsl) {
        this.inOptions = inOptions;
        this.inCode = inCode;
        this.sourceIndices = sourceIndices;
        this.glsl = glsl;
        this.outExtensions = glsl ? new ArrayList<>() : null;
    }

    private void execute() {
        val size = inCode.size();
        for (int i = 0; i < size; i++) {
            val taggedLine = inCode.get(i);
            boolean doDisable;
            val tag = taggedLine.tag();
            if (tag == TaggedLine.Tag.Macro) {
                val macro = taggedLine.text()
                                      .trim();
                if (processIfdef(macro)) {
                    doDisable = true;
                } else if (processIfndef(macro)) {
                    doDisable = true;
                } else if (processIf(macro, taggedLine)) {
                    doDisable = true;
                } else if (processElif(macro, taggedLine)) {
                    doDisable = true;
                } else if (processElse(macro, taggedLine)) {
                    doDisable = true;
                } else if (processEndif(macro, taggedLine)) {
                    doDisable = true;
                } else if (processUndef(macro)) {
                    doDisable = false;
                } else if (processVersion(macro, taggedLine)) {
                    doDisable = true;
                } else if (processExtension(macro, taggedLine)) {
                    doDisable = true;
                } else if (processOption(i)) {
                    doDisable = false;
                } else {
                    doDisable = false;
                }
            } else if (tag == TaggedLine.Tag.Standard && processOption(i)) {
                doDisable = false;
            } else {
                doDisable = false;
            }
            if (doDisable || !disabled.isEmpty()) {
                outCode.add(taggedLine.withText("// " + taggedLine.text()));
                continue;
            }
            if (tag == TaggedLine.Tag.MultilineComment) {
                val rt = RenderTargetParser.parseRenderTargetList(taggedLine.text());
                if (rt != null) {
                    outRenderTargets = rt;
                }
            }
            outCode.add(taggedLine);
        }
    }

    private static String[] split(String macro) {
        return macro.split("\\s+", 2);
    }

    private boolean processIfdef(String macro) {
        if (!macro.startsWith("#ifdef ")) {
            return false;
        }
        depth++;
        if (disabled.previousSetBit(depth - 1) != -1) {
            return true;
        }
        val parts = split(macro);
        val res = defines.containsKey(parts[1]);
        elifChainBranchTaken.set(depth, res);
        disabled.set(depth, !res);
        return true;
    }

    private boolean processIfndef(String macro) {
        if (!macro.startsWith("#ifndef ")) {
            return false;
        }
        depth++;
        if (disabled.previousSetBit(depth - 1) != -1) {
            return true;
        }
        val parts = split(macro);
        val res = !defines.containsKey(parts[1]);
        elifChainBranchTaken.set(depth, res);
        disabled.set(depth, !res);
        return true;
    }

    private boolean processIf(String macro, TaggedLine taggedLine) {
        if (!macro.startsWith("#if ")) {
            return false;
        }
        depth++;
        if (disabled.previousSetBit(depth - 1) != -1) {
            return true;
        }
        try {
            val parts = split(macro);
            val res = MacroExpressionInterpreter.interpret(parts[1], defines)
                                                .asBool();
            elifChainBranchTaken.set(depth, res);
            disabled.set(depth, !res);
        } catch (ParserException e) {
            elifChainBranchTaken.clear(depth);
            disabled.set(depth);
            Share.log.error("Failed to process #if macro in file {} line {}",
                            sourceIndices.get(taggedLine.file()),
                            taggedLine.line());
            Share.log.error("Stacktrace:", e);
        }
        return true;
    }

    private boolean processElif(String macro, TaggedLine taggedLine) {
        if (!macro.startsWith("#elif ")) {
            return false;
        }
        if (depth < 0) {
            throw new IllegalStateException("Dangling #elif macro in file " +
                                            sourceIndices.get(taggedLine.file()) +
                                            " line " +
                                            taggedLine.line());
        }
        if (disabled.previousSetBit(depth - 1) != -1) {
            return true;
        }
        if (elifChainBranchTaken.get(depth)) {
            disabled.set(depth);
        } else {
            try {
                val parts = split(macro);
                val res = MacroExpressionInterpreter.interpret(parts[1], defines)
                                                    .asBool();
                elifChainBranchTaken.set(depth, res);
                disabled.set(depth, !res);
            } catch (ParserException e) {
                disabled.set(depth);
                Share.log.error("Failed to process #elif macro in file {} line {}",
                                sourceIndices.get(taggedLine.file()),
                                taggedLine.line());
                Share.log.error("Stacktrace:", e);
            }
        }
        return true;
    }

    private boolean processElse(String macro, TaggedLine taggedLine) {
        if (!macro.startsWith("#else")) {
            return false;
        }
        if (disabled.previousSetBit(depth - 1) != -1) {
            return true;
        }
        if (depth < 0) {
            throw new IllegalStateException("Dangling #else macro in file " +
                                            sourceIndices.get(taggedLine.file()) +
                                            " line " +
                                            taggedLine.line());
        }
        if (elifChainBranchTaken.get(depth)) {
            disabled.set(depth);
        } else {
            disabled.flip(depth);
            elifChainBranchTaken.set(depth);
        }
        return true;
    }

    private boolean processEndif(String macro, TaggedLine taggedLine) {
        if (!macro.startsWith("#endif")) {
            return false;
        }
        if (depth < 0) {
            throw new IllegalStateException("Dangling #else macro in file " +
                                            sourceIndices.get(taggedLine.file()) +
                                            " line " +
                                            taggedLine.line());
        }
        disabled.clear(depth);
        elifChainBranchTaken.clear(depth);
        depth--;
        return true;
    }

    private boolean processUndef(String macro) {
        if (!macro.startsWith("#undef ")) {
            return false;
        }
        if (disabled.isEmpty()) {
            val parts = split(macro);
            defines.remove(parts[1]);
        }
        return true;
    }

    private boolean processVersion(String macro, TaggedLine taggedLine) {
        if (!glsl || !macro.startsWith("#version ")) {
            return false;
        }
        if (disabled.isEmpty()) {
            if (outVersion == null) {
                outVersion = macro;
            } else {
                Share.log.trace("Multiple version macros defined at file {} line {}",
                                sourceIndices.get(taggedLine.file()),
                                taggedLine.line());
                outVersion = macro;
            }
        }
        return true;
    }

    private boolean processExtension(String macro, TaggedLine taggedLine) {
        if (!glsl || !macro.startsWith("#extension ")) {
            return false;
        }
        if (disabled.isEmpty()) {
            outExtensions.add(macro);
        } else {
            Share.log.trace("Disabled extension at file {} line {}",
                            sourceIndices.get(taggedLine.file()),
                            taggedLine.line());
        }
        return true;
    }

    private boolean processOption(int i) {
        if (!inOptions.containsKey(i)) {
            return false;
        }
        val opt = inOptions.get(i);
        if (disabled.isEmpty()) {
            outOptions.put(outCode.size(), opt.copy(true));
            if (!(opt instanceof Option.Define dir)) {
                throw new AssertionError();
            }
            if (!dir.isToggle() || dir.isEnabled()) {
                defines.put(dir.name, dir.getCurrentValue());
            }
        }
        return true;
    }

    //TODO convert to record
    public static final class Result {
        private final @Nullable GLSL glsl;
        private final @NotNull
        @Unmodifiable List<TaggedLine> code;
        private final @NotNull
        @Unmodifiable Int2ObjectMap<Option> options;

        public Result(@Nullable GLSL glsl,
                      @NotNull @Unmodifiable List<TaggedLine> code,
                      @NotNull @Unmodifiable Int2ObjectMap<Option> options) {
            this.glsl = glsl;
            this.code = code;
            this.options = options;
        }

        public @Nullable GLSL glsl() {
            return glsl;
        }

        public @NotNull @Unmodifiable List<TaggedLine> code() {
            return code;
        }

        public @NotNull @Unmodifiable Int2ObjectMap<Option> options() {
            return options;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Result) obj;
            return Objects.equals(this.glsl, that.glsl) &&
                   Objects.equals(this.code, that.code) &&
                   Objects.equals(this.options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(glsl, code, options);
        }

        @Override
        public String toString() {
            return "Result[" + "glsl=" + glsl + ", " + "code=" + code + ", " + "options=" + options + ']';
        }

        //TODO convert to record
        public static final class GLSL {
            private final @Nullable String version;
            private final @NotNull
            @Unmodifiable List<String> extensions;
            private final @Nullable
            @Unmodifiable IntList renderTargets;

            public GLSL(@Nullable String version,
                        @NotNull @Unmodifiable List<String> extensions,
                        @Nullable @Unmodifiable IntList renderTargets) {
                this.version = version;
                this.extensions = extensions;
                this.renderTargets = renderTargets;
            }

            public @Nullable String version() {
                return version;
            }

            public @NotNull @Unmodifiable List<String> extensions() {
                return extensions;
            }

            public @Nullable @Unmodifiable IntList renderTargets() {
                return renderTargets;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (GLSL) obj;
                return Objects.equals(this.version, that.version) &&
                       Objects.equals(this.extensions, that.extensions) &&
                       Objects.equals(this.renderTargets, that.renderTargets);
            }

            @Override
            public int hashCode() {
                return Objects.hash(version, extensions, renderTargets);
            }

            @Override
            public String toString() {
                return "GLSL[" +
                       "version=" +
                       version +
                       ", " +
                       "extensions=" +
                       extensions +
                       ", " +
                       "renderTargets=" +
                       renderTargets +
                       ']';
            }
        }
    }
}
