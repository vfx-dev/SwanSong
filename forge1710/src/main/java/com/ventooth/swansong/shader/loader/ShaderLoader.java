/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.loader;

import com.falsepattern.lib.util.MathUtil;
import com.ventooth.swansong.EnvInfo;
import com.ventooth.swansong.Share;
import com.ventooth.swansong.config.DebugConfig;
import com.ventooth.swansong.config.ModuleConfig;
import com.ventooth.swansong.gl.GLProgram;
import com.ventooth.swansong.gl.GLShader;
import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.resources.ShaderpackResourceManagerAdapter;
import com.ventooth.swansong.resources.pack.ShaderPack;
import com.ventooth.swansong.shader.MCRenderStage;
import com.ventooth.swansong.shader.Report;
import com.ventooth.swansong.shader.ShaderException;
import com.ventooth.swansong.shader.ShaderTypes;
import com.ventooth.swansong.shader.config.ConfigEntry;
import com.ventooth.swansong.shader.info.ShaderProperties;
import com.ventooth.swansong.shader.loader.config.ConfigChoice;
import com.ventooth.swansong.shader.loader.config.ConfigProfile;
import com.ventooth.swansong.shader.loader.config.ConfigRootScreen;
import com.ventooth.swansong.shader.loader.config.ConfigScreen;
import com.ventooth.swansong.shader.preprocessor.MacroBuilder;
import com.ventooth.swansong.shader.preprocessor.Option;
import com.ventooth.swansong.shader.preprocessor.ShaderPreprocessor;
import com.ventooth.swansong.shader.preprocessor.ShaderStage2Meta;
import com.ventooth.swansong.shader.uniform.CompiledUniforms;
import com.ventooth.swansong.todo.tess.DanglingWiresTess;
import com.ventooth.swansong.uniforms.UniformFunctionRegistry;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.val;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL20;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Locale;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class ShaderLoader {
    //region input - populate before load()
    public ObjectList<ResourceLocation> inExpectedShaders;
    //mc_Entity, etc.
    public ObjectList<DanglingWiresTess.AttribMapping> inAttribs;
    //miscellaneous parameters
    public ShaderLoaderInParams inParams;
    //shader txt config file contents
    public byte @Nullable [] inShaderConfig;
    // environment info
    public EnvInfo inEnvInfo;
    // "minecraft" uniforms (NOT the builtins!)
    public UniformFunctionRegistry inMcUniforms;
    //endregion

    //region output - populated by load()
    private ShaderPool outShaderPool;

    public IShaderPool borrowOutShaderPool() {
        val res = outShaderPool;
        outShaderPool = null;
        return res;
    }

    public @Nullable CompiledUniforms outCompiledUniforms;
    public ConfigEntry.RootScreen outConfigScreen;
    public ShaderLoaderOutParams outParams;
    public Locale outLocale;
    //endregion

    //region internal state

    //fixed
    private final ShaderPack pack;
    private final ShaderPreprocessor preprocessor;
    private final @Nullable WorldProvider dimension;

    //common
    private ShaderLoaderOutParams.Builder paramsBuilder;
    private boolean loaded = false;

    //discovery
    private Object2ObjectMap<String, Option.Value> configFile;
    private Stage1ExtraMacros stage1ExtraMacros;
    private DeduplicatingOptionList definesStage1;
    private DeduplicatingOptionList constsStage1;
    private ObjectList<ProgramStage1> stage1;

    //configure
    private Object2ObjectMap<String, Option.Value> shaderPropertiesMacros;
    private ShaderProperties shaderProperties;
    private DeduplicatingOptionList definesStage2;
    private DeduplicatingOptionList constsStage2;
    private ObjectList<ProgramStage2> stage2;
    private ObjectSet<String> disabled;

    //endregion

    private class DeduplicatingOptionList {
        private final ObjectList<Option> options = new ObjectArrayList<>();
        private final Object2IntMap<String> indices = new Object2IntOpenHashMap<>();

        {
            indices.defaultReturnValue(-1);
        }

        public void add(Option option) {
            val idx = indices.getInt(option.name);
            if (idx < 0) {
                indices.put(option.name, options.size());
                options.add(option);
            } else {
                options.set(idx, option);
            }
        }

        public void addAll(List<Option> options) {
            for (val option : options) {
                add(option);
            }
        }
    }

    public ShaderLoader(ShaderPack pack, @Nullable WorldProvider dimension) {
        this.pack = pack;
        this.preprocessor = new ShaderPreprocessor(pack);
        this.dimension = dimension;
    }

    //Discards all dynamic state to conserve memory
    public void reset() {
        inExpectedShaders = null;
        inAttribs = null;
        inShaderConfig = null;
        inEnvInfo = null;
        inParams = null;

        val comp = outShaderPool;
        if (comp != null) {
            comp.close();
        }
        outShaderPool = null;
        outCompiledUniforms = null;
        outConfigScreen = null;
        outParams = null;
        outLocale = null;
        loaded = false;
        clearTemp();
    }

    private void clearTemp() {
        paramsBuilder = null;
        configFile = null;
        stage1ExtraMacros = null;
        definesStage1 = null;
        constsStage1 = null;
        stage1 = null;
        shaderPropertiesMacros = null;
        shaderProperties = null;
        definesStage2 = null;
        constsStage2 = null;
        stage2 = null;
        disabled = null;
    }

    public void lazyLoad(@Nullable Report report) {
        if (loaded) {
            return;
        }
        load(report);
    }

    public void load(@Nullable Report report) {
        loaded = true;
        Option.purgeCaches();
        //cleanup
        val comp = outShaderPool;
        if (comp != null) {
            comp.close();
        }
        outShaderPool = new ShaderPool();
        shaderPropertiesMacros = null;
        shaderProperties = null;

        paramsBuilder = new ShaderLoaderOutParams.Builder();
        configFile = new Object2ObjectOpenHashMap<>();
        definesStage1 = new DeduplicatingOptionList();
        constsStage1 = new DeduplicatingOptionList();
        definesStage2 = new DeduplicatingOptionList();
        constsStage2 = new DeduplicatingOptionList();
        stage1 = new ObjectArrayList<>(inExpectedShaders.size());
        stage2 = new ObjectArrayList<>(inExpectedShaders.size());
        disabled = new ObjectOpenHashSet<>();

        //discover
        loadConfigFile();
        parseEnvInfo();
        for (val expectedShader : inExpectedShaders) {
            val sh1 = runProgramStage1(expectedShader, report);
            if (sh1 != null) {
                stage1.add(sh1);
            }
        }
        inExpectedShaders = null;

        outLocale = new Locale();
        val lang = Minecraft.getMinecraft()
                            .getLanguageManager()
                            .getCurrentLanguage();
        val langs = new ArrayList<String>();
        langs.add("en_US");
        if (!"en_US".equals(lang.getLanguageCode())) {
            langs.add(lang.getLanguageCode());
        }
        outLocale.loadLocaleDataFiles(new ShaderpackResourceManagerAdapter(pack), langs);

        //configure
        parseShadersProperties();
        extractParamsFromProperties();
        disableShadersFromProperties();
        if (!disabled.isEmpty()) {
            outShaderPool.setDisabled(disabled);
        }
        createConfigScreen();
        for (val sh1 : stage1) {
            stage2.add(runProgramStage2(sh1));
        }

        //compile
        for (val sh2 : stage2) {
            val c = compileShader(sh2, report);
            if (c != null) {
                outShaderPool.insertShader(c.loc,
                                           new CompiledProgram(c.path,
                                                               c.program,
                                                               c.mipmapEnabled,
                                                               c.renderTargets,
                                                               c.actualLoc));
            }
        }
        inAttribs = null;
        extractParamsFromStage2();
        compileUniforms();

        outParams = paramsBuilder.build();

        //finished, discard temporary memory
        clearTemp();
        Option.purgeCaches();
    }

    //region discover
    private void parseEnvInfo() {
        if (inEnvInfo == null) {
            return;
        }
        val mcVersion = parseSemVer(inEnvInfo.mcVersion, 1_00_00, 1_00, 1);
        val glVersion = parseSemVer(inEnvInfo.glVersionStr.split(" ", 2)[0], 100, 10, 1);
        val glslVersion = parseSemVer(inEnvInfo.glslVersionStr.split(" ", 2)[0], 100, 1, 0);
        val swansongVersion = parseSemVer(inEnvInfo.swansongVersion, 1_00_00, 1_00, 1);
        val vendorMacro = switch (inEnvInfo.glVendor) {
            case ATI -> "MC_GL_VENDOR_ATI";
            case INTEL -> "MC_GL_VENDOR_INTEL";
            case NVIDIA -> "MC_GL_VENDOR_NVIDIA";
            case AMD -> "MC_GL_VENDOR_AMD";
            case XORG -> "MC_GL_VENDOR_XORG";
            case OTHER -> "MC_GL_VENDOR_OTHER";
        };
        val rendererMacro = switch (inEnvInfo.glRenderer) {
            case RADEON -> "MC_GL_RENDERER_RADEON";
            case GALLIUM -> "MC_GL_RENDERER_GALLIUM";
            case INTEL -> "MC_GL_RENDERER_INTEL";
            case GEFORCE -> "MC_GL_RENDERER_GEFORCE";
            case QUADRO -> "MC_GL_RENDERER_QUADRO";
            case MESA -> "MC_GL_RENDERER_MESA";
            case OTHER -> "MC_GL_RENDERER_OTHER";
        };
        val osMacro = switch (inEnvInfo.osPlatform) {
            case WINDOWS -> "MC_OS_WINDOWS";
            case OSX -> "MC_OS_MAC";
            case LINUX -> "MC_OS_LINUX";
            case OTHER -> "MC_OS_OTHER";
        };
        val extensions = new ArrayList<String>();
        for (val ext : inEnvInfo.glExtSet) {
            extensions.add("MC_" + ext);
        }
        extensions.sort(Comparator.naturalOrder());
        stage1ExtraMacros = new Stage1ExtraMacros(mcVersion,
                                                  glVersion,
                                                  glslVersion,
                                                  swansongVersion,
                                                  vendorMacro,
                                                  rendererMacro,
                                                  osMacro,
                                                  extensions,
                                                  new Option.Value.Dbl(inParams.handDepth),
                                                  new Option.Value.Dbl(inParams.renderQuality),
                                                  new Option.Value.Dbl(inParams.shadowQuality));
    }

    private static Option.Value parseSemVer(String version, int mulMajor, int mulMinor, int mulPatch) {
        val parts = version.split("\\.", 4);
        val major = parts.length >= 1 ? parseIntSafe(parts[0]) : 0;
        val minor = parts.length >= 2 ? parseIntSafe(parts[1]) : 0;
        val release = parts.length >= 3 ? parseIntSafe(parts[2]) : 0;
        return new Option.Value.Int(major * mulMajor + minor * mulMinor + release * mulPatch);
    }

    private static int parseIntSafe(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void loadConfigFile() {
        if (inShaderConfig == null) {
            return;
        }
        val reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(inShaderConfig)));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                int commentIndex = line.indexOf('#');
                val withoutComment = commentIndex < 0 ? line : line.substring(0, commentIndex);
                if (withoutComment.isEmpty()) {
                    continue;
                }
                val trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                val parts = trimmed.split("=", 2);
                if (parts.length != 2) {
                    Share.log.warn("Invalid shader config line: {}", line);
                    continue;
                }
                configFile.put(parts[0].trim(), Option.Value.detect(parts[1].trim()));
            }
        } catch (IOException e) {
            Share.log.error("Failed to read shader config", e);
        } finally {
            inShaderConfig = null;
        }
    }

    private ProgramStage1 runProgramStage1(ResourceLocation inLoc, @Nullable Report report) {
        val inPath = shaderPath(inLoc);
        var path = inPath;
        var loc = inLoc;
        while (true) {
            val vert = runStage1(path + ".vsh");
            val frag = runStage1(path + ".fsh");
            if (vert == null && frag == null) {
                //This is correct if the shader doesn't exist at all
                loc = ShaderTypes.getFallback(loc);
                if (loc != null) {
                    path = shaderPath(loc);
                    continue;
                } else {
                    return null;
                }
            }
            if (vert == null) {
                Share.log.error("Missing vertex shader for path: {}", path);
                if (report != null) {
                    report.erroredShaders.add(path);
                }
                return null;
            }
            if (frag == null) {
                Share.log.error("Missing fragment shader for path: {}", path);
                if (report != null) {
                    report.erroredShaders.add(path);
                }
                return null;
            }
            if (report != null && !Objects.equals(inPath, path)) {
                report.shadersFallback.put(inPath, path);
            }
            return new ProgramStage1(inLoc, loc, inPath, vert, frag);
        }
    }

    private String shaderPath(ResourceLocation loc) {
        val domain = loc.getResourceDomain();
        var path = loc.getResourcePath();
        if (!"minecraft".equals(domain)) {
            path = domain + "/" + path;
        }
        val spec = pack.getWorldSpecialization(dimension);
        if (spec != null) {
            path = spec + "/" + path;
        }
        return path;
    }

    private ShaderPreprocessor.PreprocessorStage1Suspend runStage1(String path) {
        return preprocessor.runStage1(path, true, stage1 -> {
            addBuiltinMacros(stage1.extraMacros);

            //apply config and store to define map
            for (val opt : stage1.defines) {
                val name = opt.name;
                val config = configFile.get(name);
                if (config != null) {
                    opt.setCurrentValue(config);
                }
                definesStage1.add(opt);
            }

            for (val opt : stage1.consts) {
                constsStage1.add(opt);
            }
        });
    }

    private void addBuiltinMacros(MacroBuilder builder) {
        //https://shaders.properties/current/reference/macros/overview/
        builder.add(stage1ExtraMacros.glVendorMacro);
        builder.add(stage1ExtraMacros.glRendererMacro);
        builder.add("MC_GL_VERSION", stage1ExtraMacros.glVersion);
        builder.add("MC_GLSL_VERSION", stage1ExtraMacros.glslVersion);
        builder.add("MC_HAND_DEPTH", stage1ExtraMacros.handDepth);
        //TODO MC_TEXTURE_FORMAT_LAB_PBR, MC_TEXTURE_FORMAT_LAB_PBR_1_3
        builder.add("MC_RENDER_QUALITY", stage1ExtraMacros.renderQuality);
        builder.add("MC_SHADOW_QUALITY", stage1ExtraMacros.shadowQuality);
        builder.add("MC_VERSION", stage1ExtraMacros.mcVersion);
        builder.add(stage1ExtraMacros.osMacro);
        for (val renderStage : MCRenderStage.values()) {
            builder.add(renderStage.macro(), renderStage.value());
        }
        for (val ext : stage1ExtraMacros.extensions) {
            builder.add(ext);
        }
        builder.add("IS_SWANSONG");
        builder.add("SWANSONG_VERSION", stage1ExtraMacros.swansongVersion);
    }

    //TODO convert to record
    private static final class ProgramStage1 {
        private final ResourceLocation loc;
        private final ResourceLocation actualLoc;
        private final String path;
        private final ShaderPreprocessor.PreprocessorStage1Suspend vert;
        private final ShaderPreprocessor.PreprocessorStage1Suspend frag;

        private ProgramStage1(ResourceLocation loc,
                              ResourceLocation actualLoc,
                              String path,
                              ShaderPreprocessor.PreprocessorStage1Suspend vert,
                              ShaderPreprocessor.PreprocessorStage1Suspend frag) {
            this.loc = loc;
            this.actualLoc = actualLoc;
            this.path = path;
            this.vert = vert;
            this.frag = frag;
        }

        public ResourceLocation loc() {
            return loc;
        }

        public ResourceLocation actualLoc() {
            return actualLoc;
        }

        public String path() {
            return path;
        }

        public ShaderPreprocessor.PreprocessorStage1Suspend vert() {
            return vert;
        }

        public ShaderPreprocessor.PreprocessorStage1Suspend frag() {
            return frag;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ProgramStage1) obj;
            return Objects.equals(this.loc, that.loc) &&
                   Objects.equals(this.path, that.path) &&
                   Objects.equals(this.vert, that.vert) &&
                   Objects.equals(this.frag, that.frag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(loc, path, vert, frag);
        }

        @Override
        public String toString() {
            return "ProgramStage1[" +
                   "loc=" +
                   loc +
                   ", " +
                   "path=" +
                   path +
                   ", " +
                   "vert=" +
                   vert +
                   ", " +
                   "frag=" +
                   frag +
                   ']';
        }
    }

    //TODO convert to record
    private static final class Stage1ExtraMacros {
        private final Option.Value mcVersion;
        private final Option.Value glVersion;
        private final Option.Value glslVersion;
        private final Option.Value swansongVersion;
        private final String glVendorMacro;
        private final String glRendererMacro;
        private final String osMacro;
        private final List<String> extensions;
        private final Option.Value handDepth;
        private final Option.Value renderQuality;
        private final Option.Value shadowQuality;

        private Stage1ExtraMacros(Option.Value mcVersion,
                                  Option.Value glVersion,
                                  Option.Value glslVersion,
                                  Option.Value swansongVersion,
                                  String glVendorMacro,
                                  String glRendererMacro,
                                  String osMacro,
                                  List<String> extensions,
                                  Option.Value handDepth,
                                  Option.Value renderQuality,
                                  Option.Value shadowQuality) {
            this.mcVersion = mcVersion;
            this.glVersion = glVersion;
            this.glslVersion = glslVersion;
            this.swansongVersion = swansongVersion;
            this.glVendorMacro = glVendorMacro;
            this.glRendererMacro = glRendererMacro;
            this.osMacro = osMacro;
            this.extensions = extensions;
            this.handDepth = handDepth;
            this.renderQuality = renderQuality;
            this.shadowQuality = shadowQuality;
        }

        public Option.Value mcVersion() {
            return mcVersion;
        }

        public Option.Value glVersion() {
            return glVersion;
        }

        public Option.Value glslVersion() {
            return glslVersion;
        }

        public Option.Value swansongVersion() {
            return swansongVersion;
        }

        public String glVendorMacro() {
            return glVendorMacro;
        }

        public String glRendererMacro() {
            return glRendererMacro;
        }

        public String osMacro() {
            return osMacro;
        }

        public List<String> extensions() {
            return extensions;
        }

        public Option.Value handDepth() {
            return handDepth;
        }

        public Option.Value renderQuality() {
            return renderQuality;
        }

        public Option.Value shadowQuality() {
            return shadowQuality;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Stage1ExtraMacros) obj;
            return Objects.equals(this.mcVersion, that.mcVersion) &&
                   Objects.equals(this.glVersion, that.glVersion) &&
                   Objects.equals(this.glslVersion, that.glslVersion) &&
                   Objects.equals(this.swansongVersion, that.swansongVersion) &&
                   Objects.equals(this.glVendorMacro, that.glVendorMacro) &&
                   Objects.equals(this.glRendererMacro, that.glRendererMacro) &&
                   Objects.equals(this.osMacro, that.osMacro) &&
                   Objects.equals(this.extensions, that.extensions) &&
                   Objects.equals(this.handDepth, that.handDepth) &&
                   Objects.equals(this.renderQuality, that.renderQuality) &&
                   Objects.equals(this.shadowQuality, that.shadowQuality);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mcVersion,
                                glVersion,
                                glslVersion,
                                swansongVersion,
                                glVendorMacro,
                                glRendererMacro,
                                osMacro,
                                extensions,
                                handDepth,
                                renderQuality,
                                shadowQuality);
        }

        @Override
        public String toString() {
            return "Stage1ExtraMacros[" +
                   "mcVersion=" +
                   mcVersion +
                   ", " +
                   "glVersion=" +
                   glVersion +
                   ", " +
                   "glslVersion=" +
                   glslVersion +
                   ", " +
                   "swansongVersion=" +
                   swansongVersion +
                   ", " +
                   "glVendorMacro=" +
                   glVendorMacro +
                   ", " +
                   "glRendererMacro=" +
                   glRendererMacro +
                   ", " +
                   "osMacro=" +
                   osMacro +
                   ", " +
                   "extensions=" +
                   extensions +
                   ", " +
                   "handDepth=" +
                   handDepth +
                   ", " +
                   "renderQuality=" +
                   renderQuality +
                   ", " +
                   "shadowQuality=" +
                   shadowQuality +
                   ']';
        }
    }
    //endregion

    //region configure

    private void parseShadersProperties() {
        val builder = new MacroBuilder();
        addBuiltinMacros(builder);
        definesStage1.options.forEach((option) -> {
            builder.add(option.name, option.getCurrentValue());
        });
        shaderPropertiesMacros = builder.get();
        val propertiesBytes = preprocessor.getBytes("shaders.properties", false, stage1 -> {
            stage1.extraMacros.addAll(shaderPropertiesMacros);
        }, stage2 -> {});
        if (propertiesBytes != null) {
            shaderProperties = ShaderProperties.parse(propertiesBytes);
        }
    }

    private void extractParamsFromProperties() {
        val props = shaderProperties;
        if (props == null) {
            return;
        }
        props.parseQuality("clouds", paramsBuilder::clouds);
        props.parseBool("moon", paramsBuilder::moon);
        props.parseBool("sun", paramsBuilder::sun);
        props.parseBool("underwaterOverlay", paramsBuilder::underwaterOverlay);
        props.parseBool("vignette", paramsBuilder::vignette);
        props.parseBool("oldHandLight", paramsBuilder::oldHandLight);
        props.parseBool("oldLighting", paramsBuilder::oldLighting);

        props.parseBool("shadowTerrain", paramsBuilder::shadowTerrain);
        props.parseBool("shadowTranslucent", paramsBuilder::shadowTranslucent);
        props.parseBool("shadowEntities", paramsBuilder::shadowEntities);
        props.parseBool("shadowBlockEntities", paramsBuilder::shadowBlockEntities);

        props.parseBool("backface.solid", paramsBuilder::backFaceSolid);
        props.parseBool("backface.cutout", paramsBuilder::backFaceCutout);
        props.parseBool("backface.cutoutMipped", paramsBuilder::backFaceCutoutMipped);
        props.parseBool("backface.translucent", paramsBuilder::backFaceTranslucent);

        props.parseBool("frustum.culling", paramsBuilder::frustumCulling);
        props.parseBool("shadow.culling", paramsBuilder::shadowCulling);
        props.parseBool("rain.depth", paramsBuilder::rainDepth);
        props.parseBool("beacon.beam.depth", paramsBuilder::beaconBeamDepth);

        props.parseBool("rpleCompatible", paramsBuilder::rpleCompatible);

        props.texture()
             .forEach((name, path) -> {
                 if ("noise".equals(name)) {
                     paramsBuilder.noiseTexture(path);
                     return;
                 }
                 val parts = name.split("\\.", 2);
                 if (parts.length != 2) {
                     Share.log.warn("Invalid texture in properties: \"{}\"", name);
                     return;
                 }
                 paramsBuilder.textures.computeIfAbsent(parts[0], ign -> new Object2ObjectLinkedOpenHashMap<>())
                                       .put(parts[1], path);
             });
    }

    private void disableShadersFromProperties() {
        val props = shaderProperties;
        if (props == null) {
            return;
        }
        props.programEnableExpressions()
             .forEach(program -> {
                 val opt = "program." + program + ".enabled";
                 props.parseBoolExpr(shaderPropertiesMacros, opt, flag -> {
                     if (!flag) {
                         disabled.add(program);
                     }
                 });
             });
    }

    private void createConfigScreen() {
        val allOptions = new ObjectArrayList<Option>();

        val screenInfo = shaderProperties != null ? shaderProperties.screen() : null;
        val mainPage = screenInfo != null ? screenInfo.mainPage() : null;
        val sliders = screenInfo != null ? screenInfo.sliders() : null;
        val profilesList = shaderProperties != null ? shaderProperties.profiles() : null;
        val profile =
                profilesList != null && !profilesList.isEmpty() ? new ConfigProfile(outLocale, allOptions, profilesList)
                                                                : null;
        if (mainPage == null) {
            readStage1Options(allOptions::add);
            val mainScreenContent = new ObjectArrayList<ConfigEntry>();
            if (profile != null) {
                mainScreenContent.add(profile);
            }
            val root = new ConfigRootScreen(outLocale, mainScreenContent, allOptions);
            outConfigScreen = root;
            for (int i = 0; i < allOptions.size(); i++) {
                var opt = allOptions.get(i);
                mainScreenContent.add(optionToEntry(sliders, opt, i, root));
            }

        } else {
            val optionsIndices = new Object2IntOpenHashMap<String>();
            val optionsMap = new Object2ObjectOpenHashMap<String, Option>();
            readStage1Options(opt -> {
                optionsIndices.put(opt.name, allOptions.size());
                allOptions.add(opt);
                optionsMap.put(opt.name, opt);
            });
            val rootScreen = new ConfigRootScreen(outLocale, new ObjectArrayList<>(), allOptions);
            outConfigScreen = rootScreen;
            val subScreens = new Object2ObjectOpenHashMap<String, ConfigScreen>();
            //Init sub-screens
            Object2ObjectMaps.fastForEach(screenInfo.subPages(), entry -> {
                val key = entry.getKey();
                subScreens.put(key, new ConfigScreen(outLocale, key, new ObjectArrayList<>()));
            });
            //Construct layout
            parseScreen(rootScreen.content,
                        subScreens,
                        optionsMap,
                        profile,
                        sliders,
                        mainPage,
                        optionsIndices,
                        rootScreen);
            screenInfo.subPages()
                      .forEach((name, value) -> {
                          val subScreen = subScreens.get(name);
                          parseScreen(subScreen.content,
                                      subScreens,
                                      optionsMap,
                                      profile,
                                      sliders,
                                      value,
                                      optionsIndices,
                                      rootScreen);
                      });
        }
    }

    private ConfigEntry optionToEntry(ObjectSet<String> sliders, Option opt, int index, ConfigRootScreen root) {
        if (sliders != null && sliders.contains(opt.name)) {
            return new ConfigChoice.Draggable(outLocale, opt, index, root);
        } else if (opt.isToggle()) {
            return new ConfigChoice.Toggleable(outLocale, opt, index, root);
        } else {
            return new ConfigChoice.Switchable(outLocale, opt, index, root);
        }
    }

    private void parseScreen(ObjectList<ConfigEntry> output,
                             Object2ObjectMap<String, ConfigScreen> subScreens,
                             Object2ObjectMap<String, Option> options,
                             ConfigProfile profile,
                             ObjectSet<String> sliders,
                             ObjectList<String> config,
                             Object2IntMap<String> indices,
                             ConfigRootScreen root) {
        for (val element : config) {
            if ("<empty>".equals(element)) {
                output.add(null);
            } else if ("<profile>".equals(element)) {
                output.add(profile);
            } else if (element.startsWith("[") && element.endsWith("]")) {
                val subScreen = subScreens.get(element.substring(1, element.length() - 1));
                output.add(subScreen);
            } else {
                val option = options.get(element);
                if (option == null) {
                    output.add(null);
                } else {
                    output.add(optionToEntry(sliders, option, indices.getInt(option.name), root));
                }
            }
        }
    }

    private void readStage1Options(Consumer<Option> configurer) {
        definesStage1.options.forEach(opt -> {
            if (opt.isConfigurable()) {
                configurer.accept(opt.copy(false));
            }
        });
        constsStage1.options.forEach(opt -> {
            if (opt.isConfigurable()) {
                val theCopy = opt.copy(false);
                val config = configFile.get(theCopy.name);
                if (config != null) {
                    theCopy.setCurrentValue(config);
                }
                configurer.accept(theCopy);
            }
        });
    }

    private ProgramStage2 runProgramStage2(ProgramStage1 stage1) {
        val pRenderTargets = new IntList[1];
        val mipmapEnabled = new ObjectLinkedOpenHashSet<String>();
        val vert = stage1.vert.runStage2(stage2 -> {
            fetchStage2Data(stage2, mipmapEnabled);
        });
        val frag = stage1.frag.runStage2(stage2 -> {
            fetchStage2Data(stage2, mipmapEnabled);
            pRenderTargets[0] = stage2.renderTargets;
        });
        return new ProgramStage2(stage1.loc,
                                 stage1.actualLoc,
                                 stage1.path,
                                 vert,
                                 frag,
                                 new ObjectArrayList<>(mipmapEnabled),
                                 pRenderTargets[0]);
    }

    private void fetchStage2Data(ShaderStage2Meta stage2, ObjectSet<String> mipmapEnabled) {
        definesStage2.addAll(stage2.defines);
        for (val opt : stage2.consts) {
            val name = opt.name;
            val config = configFile.get(name);
            if (config != null) {
                opt.setCurrentValue(config);
            }
            constsStage2.add(opt);
            tryFetchMipMapEnabledOption(opt, mipmapEnabled);
        }
    }

    private void tryFetchMipMapEnabledOption(Option option, ObjectSet<String> output) {
        if (!option.isToggle()) {
            return;
        }
        val name = option.name;
        if (!name.endsWith("MipmapEnabled")) {
            return;
        }
        val bufName = name.substring(0, name.length() - "MipmapEnabled".length());
        if (option.isEnabled()) {
            output.add(bufName);
        }
    }

    private void extractParamsFromStage2() {
        val p = paramsBuilder;
        for (val opt : constsStage2.options) {
            val v = opt.getCurrentValue();
            switch (opt.name) {
                case "ambientOcclusionLevel" -> v.safeDouble(0, 1, p::ambientOcclusionLevel);
                case "sunPathRotation" -> v.safeDouble(p::sunPathRotation);
                case "eyeBrightnessHalflife" -> v.safeDouble(p::eyeBrightnessHalfLife);
                case "centerDepthHalflife" -> v.safeDouble(p::centerDepthHalfLife);
                case "drynessHalflife" -> v.safeDouble(p::drynessHalfLife);
                case "wetnessHalflife" -> v.safeDouble(p::wetnessHalfLife);

                case "generateShadowMipmap" -> v.boolFused(p::shadowDepth0Mipmap, p::shadowDepth1Mipmap);
                case "shadowtexMipmap", "shadowtex0Mipmap" -> v.boolFused(p::shadowDepth0Mipmap);
                case "shadowtex0Nearest", "shadowtexNearest", "shadow0MinMagNearest" ->
                        v.boolFused(p::shadowDepth0Nearest);
                case "shadowtex1Mipmap" -> v.boolFused(p::shadowDepth1Mipmap);
                case "shadowtex1Nearest", "shadow1MinMagNearest" -> v.boolFused(p::shadowDepth1Nearest);

                case "generateShadowColorMipmap" -> v.boolFused(p::shadowColor0Mipmap, p::shadowColor1Mipmap);
                case "shadowcolor0Mipmap", "shadowColor0Mipmap" -> v.boolFused(p::shadowColor0Mipmap);
                case "shadowcolor0Nearest", "shadowColor0Nearest", "shadowColor0MinMagNearest" ->
                        v.boolFused(p::shadowColor0Nearest);
                case "shadowcolor1Mipmap", "shadowColor1Mipmap" -> v.boolFused(p::shadowColor1Mipmap);
                case "shadowcolor1Nearest", "shadowColor1Nearest", "shadowColor1MinMagNearest" ->
                        v.boolFused(p::shadowColor1Nearest);

                case "noiseTextureResolution" -> v.safeInt(p::noiseTextureResolution);

                case "shadowDistance" -> v.safeDouble(p::shadowDistance);
                case "shadowDistanceRenderMul" -> v.safeDouble(p::shadowDistanceRenderMul);
                case "shadowHardwareFiltering" -> v.boolFused(p::shadowHardwareFiltering0, p::shadowHardwareFiltering1);
                case "shadowHardwareFiltering0" -> v.boolFused(p::shadowHardwareFiltering0);
                case "shadowHardwareFiltering1" -> v.boolFused(p::shadowHardwareFiltering1);
                case "shadowIntervalSize" -> v.safeDouble(p::shadowIntervalSize);
                case "shadowMapFov" -> v.safeDouble(p::shadowMapFov);
                case "shadowMapResolution" -> v.safeInt(p::shadowMapResolution);
                default -> {
                    if (tryFetchBufferFormat(opt)) {
                        continue;
                    }
                    if (tryFetchBufferClear(opt)) {
                        continue;
                    }
                    if (tryFetchBufferClearColor(opt)) {
                        continue;
                    }
                }
            }
        }
    }

    private boolean tryFetchBufferFormat(Option option) {
        val name = option.name;
        if (!name.endsWith("Format")) {
            return false;
        }
        val bufName = name.substring(0, name.length() - "Format".length());
        paramsBuilder.bufferFormat.put(bufName,
                                       option.getCurrentValue()
                                             .toString());
        return true;
    }

    private boolean tryFetchBufferClear(Option option) {
        if (!option.isToggle()) {
            return false;
        }
        val name = option.name;
        if (!name.endsWith("Clear")) {
            return false;
        }
        val bufName = name.substring(0, name.length() - "Clear".length());
        if (!option.isEnabled()) {
            paramsBuilder.bufferClearDisabled.add(bufName);
        }
        return true;
    }


    private boolean tryFetchBufferClearColor(Option option) {
        val name = option.name;
        if (!name.endsWith("ClearColor")) {
            return false;
        }
        val bufName = name.substring(0, name.length() - "Clear".length());
        val txt = option.getCurrentValue()
                        .toString()
                        .trim();
        if (!txt.startsWith("vec4")) {
            return true;
        }
        int parenOpen = txt.indexOf('(');
        int parenClose = txt.indexOf(')');
        if (parenOpen < 0 || parenClose < parenOpen) {
            return true;
        }
        val values = txt.substring(parenOpen + 1, parenClose);
        val parts = values.split("\\s*,\\s*");
        if (parts.length != 4) {
            return true;
        }
        try {
            val vector = new Vector4d(Double.parseDouble(parts[0]),
                                      Double.parseDouble(parts[1]),
                                      Double.parseDouble(parts[2]),
                                      Double.parseDouble(parts[3]));
            paramsBuilder.bufferClearColor.put(bufName, vector);
        } catch (NumberFormatException e) {
            Share.log.error("Could not parse buffer clear color: {}", values);
            Share.log.error("Stacktrace: ", e);
        }
        return true;
    }

    private void safeDouble(Option.Value val, double min, double max, DoubleConsumer output) {
        val dVal = val.doubleValue();
        if (dVal == null) {
            return;
        }
        val clamped = MathUtil.clamp(dVal, min, max);
        output.accept(clamped);
    }

    private void safeDouble(Option.Value val, DoubleConsumer output) {
        val dVal = val.doubleValue();
        if (dVal == null) {
            return;
        }
        output.accept(dVal);
    }

    //TODO convert to record
    private static final class ProgramStage2 {
        private final ResourceLocation loc;
        private final ResourceLocation actualLoc;
        private final String path;
        private final ShaderPreprocessor.PreprocessorStage2Suspend vert;
        private final ShaderPreprocessor.PreprocessorStage2Suspend frag;
        private final ObjectList<String> mipmapEnabled;
        private final IntList renderTargets;

        private ProgramStage2(ResourceLocation loc,
                              ResourceLocation actualLoc,
                              String path,
                              ShaderPreprocessor.PreprocessorStage2Suspend vert,
                              ShaderPreprocessor.PreprocessorStage2Suspend frag,
                              ObjectList<String> mipmapEnabled,
                              IntList renderTargets) {
            this.loc = loc;
            this.actualLoc = actualLoc;
            this.path = path;
            this.vert = vert;
            this.frag = frag;
            this.mipmapEnabled = mipmapEnabled;
            this.renderTargets = renderTargets;
        }

        public ResourceLocation loc() {
            return loc;
        }

        public ResourceLocation actualLoc() {
            return actualLoc;
        }

        public String path() {
            return path;
        }

        public ShaderPreprocessor.PreprocessorStage2Suspend vert() {
            return vert;
        }

        public ShaderPreprocessor.PreprocessorStage2Suspend frag() {
            return frag;
        }

        public ObjectList<String> mipmapEnabled() {
            return mipmapEnabled;
        }

        public IntList renderTargets() {
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
            var that = (ProgramStage2) obj;
            return Objects.equals(this.loc, that.loc) &&
                   Objects.equals(this.path, that.path) &&
                   Objects.equals(this.vert, that.vert) &&
                   Objects.equals(this.frag, that.frag) &&
                   Objects.equals(this.mipmapEnabled, that.mipmapEnabled) &&
                   Objects.equals(this.renderTargets, that.renderTargets);
        }

        @Override
        public int hashCode() {
            return Objects.hash(loc, path, vert, frag, mipmapEnabled, renderTargets);
        }

        @Override
        public String toString() {
            return "ProgramStage2[" +
                   "loc=" +
                   loc +
                   ", " +
                   "path=" +
                   path +
                   ", " +
                   "vert=" +
                   vert +
                   ", " +
                   "frag=" +
                   frag +
                   ", " +
                   "mipmapEnabled=" +
                   mipmapEnabled +
                   ", " +
                   "renderTargets=" +
                   renderTargets +
                   ']';
        }
    }

    //endregion

    //region compile

    private void compileUniforms() {
        if (shaderProperties == null) {
            return;
        }
        outCompiledUniforms = CompiledUniforms.createCompiledUniforms(inMcUniforms, shaderProperties.shaderVars());
    }

    private ProgramCompiled compileShader(ProgramStage2 stage2, @Nullable Report report) {
        GLShader vert = null;
        GLShader frag = null;

        try {
            vert = createShader(GL20.GL_VERTEX_SHADER, stage2.path + ".vsh", stage2.vert.getNativeBuffer(true));
            frag = createShader(GL20.GL_FRAGMENT_SHADER, stage2.path + ".fsh", stage2.frag.getNativeBuffer(true));
            val prog = createProgram(stage2.path, vert, frag);
            return new ProgramCompiled(stage2.loc,
                                       stage2.actualLoc,
                                       stage2.path,
                                       prog,
                                       stage2.mipmapEnabled,
                                       stage2.renderTargets);
        } catch (Exception e) {
            Share.log.error("Error while compiling shader {}", stage2.path);
            Share.log.error("Stacktrace:", e);
            if (report != null) {
                report.erroredShaders.add(stage2.path);
            }
            return null;
        } finally {
            if (vert != null) {
                vert.glDeleteShader();
            }
            if (frag != null) {
                frag.glDeleteShader();
            }
        }
    }

    /**
     * src MUST be null terminated!
     */
    @NotNull
    public GLShader createShader(@MagicConstant(intValues = {GL20.GL_VERTEX_SHADER, GL20.GL_FRAGMENT_SHADER}) int type,
                                 String name,
                                 ByteBuffer src) throws ShaderException {
        ShaderPackManager.dumpShader(name, src);
        val shader = new GLShader();
        shader.glCreateShader(type);
        shader.glShaderSource(src);
        shader.glCompileShader();

        if (!shader.glGetShaderCompileStatus()) {
            var infoLog = shader.glGetShaderInfoLog();
            if (infoLog.isEmpty()) {
                infoLog = "Empty shader info log";
            }
            shader.glDeleteShader();

            throw new ShaderException("Failed to compile shader: " + name + '\n' + (infoLog) + '\n');
        }

        return shader;
    }

    @NotNull
    public GLProgram createProgram(@NotNull String name, @NotNull GLShader vertShader, @NotNull GLShader fragShader)
            throws ShaderException {
        val program = new GLProgram();
        program.glCreateProgram();
        program.glAttachShader(vertShader);
        program.glAttachShader(fragShader);

        for (val attrib : inAttribs) {
            program.glBindAttribLocation(attrib.index, attrib.name);
        }

        program.glLinkProgram();
        if (!program.glGetProgramLinkStatus()) {
            var infoLog = program.glGetProgramInfoLog();
            if (infoLog.isEmpty()) {
                infoLog = "Empty program info log";
            }
            program.glDeleteProgram();

            throw new ShaderException("Failed to link program: " + name + '\n' + (infoLog) + '\n');
        }

        return program;
    }

    //TODO convert to record
    private static final class ProgramCompiled {
        private final ResourceLocation loc;
        private final ResourceLocation actualLoc;
        private final String path;
        private final GLProgram program;
        private final ObjectList<String> mipmapEnabled;
        private final IntList renderTargets;

        private ProgramCompiled(ResourceLocation loc,
                                ResourceLocation actualLoc,
                                String path,
                                GLProgram program,
                                ObjectList<String> mipmapEnabled,
                                IntList renderTargets) {
            this.loc = loc;
            this.actualLoc = actualLoc;
            this.path = path;
            this.program = program;
            this.mipmapEnabled = mipmapEnabled;
            this.renderTargets = renderTargets;
        }

        public ResourceLocation loc() {
            return loc;
        }

        public ResourceLocation actualLoc() {
            return actualLoc;
        }

        public String path() {
            return path;
        }

        public GLProgram program() {
            return program;
        }

        public ObjectList<String> mipmapEnabled() {
            return mipmapEnabled;
        }

        public IntList renderTargets() {
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
            var that = (ProgramCompiled) obj;
            return Objects.equals(this.loc, that.loc) &&
                   Objects.equals(this.path, that.path) &&
                   Objects.equals(this.program, that.program) &&
                   Objects.equals(this.mipmapEnabled, that.mipmapEnabled) &&
                   Objects.equals(this.renderTargets, that.renderTargets);
        }

        @Override
        public int hashCode() {
            return Objects.hash(loc, path, program, mipmapEnabled, renderTargets);
        }

        @Override
        public String toString() {
            return "ProgramCompiled[" +
                   "loc=" +
                   loc +
                   ", " +
                   "path=" +
                   path +
                   ", " +
                   "program=" +
                   program +
                   ", " +
                   "mipmapEnabled=" +
                   mipmapEnabled +
                   ", " +
                   "renderTargets=" +
                   renderTargets +
                   ']';
        }
    }

    //endregion
}
