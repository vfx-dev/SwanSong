/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.info;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.shader.loader.ShaderLoaderOutParams;
import com.ventooth.swansong.shader.mappings.OrderedProperties;
import com.ventooth.swansong.shader.preprocessor.Option;
import com.ventooth.swansong.shader.preprocessor.macro.MacroExpressionInterpreter;
import com.ventooth.swansong.uniforms.Type;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

//TODO convert to record
public final class ShaderProperties {
    private final Screen screen;
    private final Object2ObjectMap<String, String> profiles;
    private final ObjectList<String> programEnableExpressions;
    private final ObjectList<ShaderVar> shaderVars;
    private final Object2ObjectMap<String, String> alphaTest;
    private final Object2ObjectMap<String, String> texture;
    private final Object2ObjectMap<String, String> everything;

    public ShaderProperties(Screen screen,
                            Object2ObjectMap<String, String> profiles,
                            ObjectList<String> programEnableExpressions,
                            ObjectList<ShaderVar> shaderVars,
                            Object2ObjectMap<String, String> alphaTest,
                            Object2ObjectMap<String, String> texture,
                            Object2ObjectMap<String, String> everything) {
        this.screen = screen;
        this.profiles = profiles;
        this.programEnableExpressions = programEnableExpressions;
        this.shaderVars = shaderVars;
        this.alphaTest = alphaTest;
        this.texture = texture;
        this.everything = everything;
    }

    public static @NotNull ShaderProperties parse(byte[] code) {
        val properties = new OrderedProperties();
        try {
            properties.load(new ByteArrayInputStream(code));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        ObjectSet<String> sliders = ObjectSets.emptySet();
        ObjectList<String> screenMainPage = null;
        val screenSubPages = new Object2ObjectLinkedOpenHashMap<String, @NotNull ObjectList<String>>();
        val profiles = new Object2ObjectLinkedOpenHashMap<String, @NotNull String>();
        val expressions = new ObjectArrayList<@NotNull String>();
        val shaderVars = new ObjectArrayList<@NotNull ShaderVar>();
        val alphaTest = new Object2ObjectLinkedOpenHashMap<String, @NotNull String>();
        val texture = new Object2ObjectLinkedOpenHashMap<String, @NotNull String>();
        val everything = new Object2ObjectLinkedOpenHashMap<String, @NotNull String>();
        for (val prop : properties) {
            val name = prop.getKey();
            val value = prop.getValue();
            everything.put(name, value);
            if (name.startsWith("screen.")) {
                screenSubPages.put(name.substring("screen.".length()), parseScreenPage(value));
                continue;
            }
            if ("screen".equals(name)) {
                screenMainPage = parseScreenPage(value);
                continue;
            }
            if ("sliders".equals(name)) {
                sliders = ObjectSets.unmodifiable(new ObjectOpenHashSet<>(value.split("\\s+")));
            }
            if (name.startsWith("profile.")) {
                profiles.put(name.substring("profile.".length()), value);
                continue;
            }
            if (name.startsWith("program.") && name.endsWith(".enabled")) {
                val prog = name.substring("program.".length(), name.length() - ".enabled".length());
                expressions.add(prog);
                continue;
            }
            if (name.startsWith("alphaTest.")) {
                val prog = name.substring("alphaTest.".length());
                alphaTest.put(prog, value);
                continue;
            }
            if (name.startsWith("uniform.")) {
                val v = parseShaderVar(ShaderVar.Variant.Uniform, name.substring("uniform.".length()), value);
                if (v != null) {
                    shaderVars.add(v);
                }
                continue;
            }
            if (name.startsWith("variable.")) {
                val v = parseShaderVar(ShaderVar.Variant.Variable, name.substring("variable.".length()), value);
                if (v != null) {
                    shaderVars.add(v);
                }
                continue;
            }
            if (name.startsWith("texture.")) {
                texture.put(name.substring("texture.".length()), value);
                continue;
            }
        }
        return new ShaderProperties(new Screen(sliders, screenMainPage, screenSubPages),
                                    profiles,
                                    expressions,
                                    shaderVars,
                                    alphaTest,
                                    texture,
                                    everything);
    }

    private static @Nullable ShaderVar parseShaderVar(ShaderVar.Variant variant, String name, String value) {
        val parts = name.split("\\.", 2);
        if (parts.length != 2) {
            Share.log.error("Failed to parse <type>.<name> for shader variable {} in shaders.properties!", name);
            return null;
        }
        return new ShaderVar(variant, Type.of(parts[0]), parts[1], value);
    }

    private static ObjectList<String> parseScreenPage(String entry) {
        return ObjectLists.unmodifiable(ObjectArrayList.of(entry.split("\\s+")));
    }


    public void parseBool(@NotNull String name, BooleanConsumer out) {
        val input = getValue(name);
        if (input == null) {
            return;
        }
        switch (input.toLowerCase(Locale.ROOT)) {
            case "false" -> out.accept(false);
            case "true" -> out.accept(true);
            default -> {
                Share.log.error("Error while parsing bool \"{}\" for \"{}\"", input, name);
            }
        }
    }

    public void parseInt(@NotNull String name, IntConsumer out) {
        val input = getValue(name);
        if (input == null) {
            return;
        }
        try {
            out.accept(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            Share.log.error("Error while parsing int \"{}\" for \"{}\"", input, name);
            Share.log.error("Stacktrace:", e);
        }
    }

    public void parseDouble(@NotNull String name, DoubleConsumer out) {
        val input = getValue(name);
        if (input == null) {
            return;
        }
        try {
            out.accept(Double.parseDouble(input));
        } catch (NumberFormatException e) {
            Share.log.error("Error while parsing double \"{}\" for \"{}\"", input, name);
            Share.log.error("Stacktrace:", e);
        }
    }

    public void parseQuality(@NotNull String name, Consumer<ShaderLoaderOutParams.Quality> out) {
        val input = getValue(name);
        if (input == null) {
            return;
        }

        switch (input.toLowerCase(Locale.ROOT)) {
            case "fancy" -> out.accept(ShaderLoaderOutParams.Quality.Fancy);
            case "fast" -> out.accept(ShaderLoaderOutParams.Quality.Fast);
            case "off" -> out.accept(ShaderLoaderOutParams.Quality.Off);
            default -> {
                Share.log.error("Error while parsing quality \"{}\" for \"{}\"", input, name);
            }
        }
        ;
    }

    public void getValue(@NotNull String name, Consumer<String> out) {
        val input = getValue(name);
        if (input == null) {
            return;
        }
        out.accept(input);
    }

    public void parseBoolExpr(Object2ObjectMap<String, Option.Value> defines,
                              @NotNull String name,
                              BooleanConsumer out) {
        val input = getValue(name);
        if (input == null) {
            return;
        }
        try {
            val res = MacroExpressionInterpreter.interpret(input, defines);
            out.accept(res.asBool());
        } catch (Exception e) {
            Share.log.error("Error while parsing bool expression \"{}\" for \"{}\"", input, name);
            Share.log.error("Stacktrace:", e);
        }
    }

    private @Nullable String getValue(@NotNull String name) {
        return everything.get(name);
    }

    public Screen screen() {
        return screen;
    }

    public Object2ObjectMap<String, String> profiles() {
        return profiles;
    }

    public ObjectList<String> programEnableExpressions() {
        return programEnableExpressions;
    }

    public ObjectList<ShaderVar> shaderVars() {
        return shaderVars;
    }

    public Object2ObjectMap<String, String> alphaTest() {
        return alphaTest;
    }

    public Object2ObjectMap<String, String> texture() {
        return texture;
    }

    public Object2ObjectMap<String, String> everything() {
        return everything;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (ShaderProperties) obj;
        return Objects.equals(this.screen, that.screen) &&
               Objects.equals(this.profiles, that.profiles) &&
               Objects.equals(this.programEnableExpressions, that.programEnableExpressions) &&
               Objects.equals(this.shaderVars, that.shaderVars) &&
               Objects.equals(this.alphaTest, that.alphaTest) &&
               Objects.equals(this.texture, that.texture) &&
               Objects.equals(this.everything, that.everything);
    }

    @Override
    public int hashCode() {
        return Objects.hash(screen, profiles, programEnableExpressions, shaderVars, alphaTest, texture, everything);
    }

    @Override
    public String toString() {
        return "ShaderProperties[" +
               "screen=" +
               screen +
               ", " +
               "profiles=" +
               profiles +
               ", " +
               "programEnableExpressions=" +
               programEnableExpressions +
               ", " +
               "shaderVars=" +
               shaderVars +
               ", " +
               "alphaTest=" +
               alphaTest +
               ", " +
               "texture=" +
               texture +
               ", " +
               "everything=" +
               everything +
               ']';
    }

}
