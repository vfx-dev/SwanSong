/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.platform;

import com.falsepattern.lib.dependencies.DependencyLoader;
import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.falsepattern.lib.util.FileUtil;
import com.ventooth.swansong.CoreSettings;
import com.ventooth.swansong.Tags;
import com.ventooth.swansong.Share;
import com.ventooth.swansong.api.SwanSongAttributes.Instanced;
import com.ventooth.swansong.config.Configs;
import com.ventooth.swansong.config.DebugConfig;
import com.ventooth.swansong.config.ModuleConfig;
import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.gl.ShaderHax;
import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.shader.Report;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import com.ventooth.swansong.shader.mappings.BlockIDRemapper;
import com.ventooth.swansong.todo.tess.DanglingWiresTess;
import com.ventooth.swansong.todo.tess.DanglingWiresTess.AttribMapping;
import com.ventooth.swansong.uniforms.compiler.UniformCodegen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.Sys;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.TimeZone;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlatformHooks {
    private static final String CHAT_PREFIX =
            "[" + EnumChatFormatting.BLUE + "Swan" + EnumChatFormatting.AQUA + "Song" + EnumChatFormatting.RESET + "] ";

    public static void install() {
        Report.chatReporter = PlatformHooks::reportToChat;
        ShaderPackManager.settingsChangedHook = PlatformHooks::onShaderSettingsChanged;
        ShaderPackManager.referencePackEnabled = () -> ShadersConfig.enableReferenceShaderPack;
        BlockIDRemapper.blockIdLookup = PlatformHooks::findBlockId;
        ShaderPackManager.localizerFactory = MinecraftPackLocalizer::new;
        ShaderPackManager.referencePack = DefaultShaderPack.INSTANCE;
        ShaderEngine.shadowPass = new McShadowPassRenderer();
        syncCoreSettings();
        ensureJnaAvailable();
        ShaderEngine.host = new McHostRenderer();
        ShaderState.host = new McHostWorld();
        ShaderState.worldSampler = McWorldSampler::sample;
        ShaderState.heldItemIdSource = McWorldSampler::heldItemId;
        ShaderState.heldBlockLightSource = McWorldSampler::heldBlockLightValue;
        DanglingWiresTess.hostAttribs = instancedAttribs();
        UniformCodegen.dumpDirSupplier = PlatformHooks::uniformDumpDir;
    }

    private static ObjectList<AttribMapping> instancedAttribs() {
        val attribs = new ObjectArrayList<AttribMapping>();
        attribs.add(new AttribMapping(Instanced.staticPosition, "inst_Position"));
        attribs.add(new AttribMapping(Instanced.staticTexture, "inst_Texture"));
        attribs.add(new AttribMapping(Instanced.staticColor, "inst_Color"));
        attribs.add(new AttribMapping(Instanced.staticEntityData, "inst_EntityData"));
        attribs.add(new AttribMapping(Instanced.staticNormal, "inst_Normal"));
        attribs.add(new AttribMapping(Instanced.staticTangent, "inst_Tangent"));
        attribs.add(new AttribMapping(Instanced.staticMidTexture, "inst_MidTexture"));
        attribs.add(new AttribMapping(Instanced.staticEdgeTexture, "inst_EdgeTexture"));

        attribs.add(new AttribMapping(Instanced.dynamicModelMat, "inst_ModelMat"));
        attribs.add(new AttribMapping(Instanced.dynamicBrightnessR, "inst_BrightnessR"));
        attribs.add(new AttribMapping(Instanced.dynamicBrightnessG, "inst_BrightnessG"));
        attribs.add(new AttribMapping(Instanced.dynamicBrightnessB, "inst_BrightnessB"));
        return ObjectLists.unmodifiable(attribs);
    }

    private static @Nullable Integer findBlockId(String modId, String blockName) {
        val block = GameRegistry.findBlock(modId, blockName);
        return block == null ? null : Block.getIdFromBlock(block);
    }

    private static @Nullable Path uniformDumpDir() {
        if (!DebugConfig.DumpCompiledUniforms) {
            return null;
        }
        val dir = FileUtil.getMinecraftHomePath()
                          .resolve("swansong_uniform_compiler");
        try {
            if (Files.exists(dir)) {
                FileUtils.deleteDirectory(dir.toFile());
            }
            Files.createDirectories(dir);
        } catch (IOException e) {
            Share.log.error("Failed to prepare the uniform dump directory: {}", dir, e);
            return null;
        }
        return dir;
    }

    private static void ensureJnaAvailable() {
        if (ShaderHax.isLwjgl3()) {
            return;
        }
        DependencyLoader.addMavenRepo("https://repo1.maven.org/maven2/");
        DependencyLoader.loadLibraries(Library.builder()
                                              .loadingModId(Tags.MOD_ID)
                                              .groupId("net.java.dev.jna")
                                              .artifactId("jna")
                                              .minVersion(new SemanticVersion(5, 17, 0))
                                              .preferredVersion(new SemanticVersion(5, 17, 0))
                                              .build());
    }

    public static void syncCoreSettings() {
        CoreSettings.glDebugMarkers = DebugConfig.GLDebugMarkers;
        CoreSettings.glDebugGroups = ModuleConfig.Debug && DebugConfig.UseGLDebugGroups;
        CoreSettings.glObjectLabels = ModuleConfig.Debug && DebugConfig.UseGLObjectLabels;
        CoreSettings.handDepth = ShadersConfig.HandDepth.get();
        CoreSettings.renderQuality = ShadersConfig.RenderQuality.get();
        CoreSettings.shadowQuality = ShadersConfig.ShadowQuality.get();
        CoreSettings.allowDepthOfField = ShadersConfig.LetMeUseDepthOfFieldPlease;
    }

    private static void onShaderSettingsChanged() {
        syncCoreSettings();
        ShadersConfig.CurrentShaderPack = ShaderPackManager.getCurrentShaderPackName();
        ShaderEngine.scheduleShaderPackReload();
        Configs.syncConfigFile();
    }

    public static void openShaderPacksDir() {
        val dir = ShaderPackManager.getShaderPacksDir();
        try {
            // Works on Windows/Linux
            // TODO: Doesn't work on my machine without LWJGL3ify? Wayland+KDE, should open Dolphin :(
            Desktop.getDesktop()
                   .open(dir.toFile());
        } catch (Exception e) {
            var failed = false;
            try {
                // Works on MacOS
                failed = !Sys.openURL("file://" + dir.toFile()
                                                     .getAbsolutePath());
            } catch (Exception e2) {
                e.addSuppressed(e2);
            }

            if (failed) {
                Share.log.error("Failed to open shaderpacks directory", e);
            }
        }
    }

    private static void reportToChat(Report report) {
        if (!report.erroredShaders.isEmpty()) {
            val plr = Minecraft.getMinecraft().thePlayer;
            if (plr != null) {
                val zone = TimeZone.getDefault();
                val now = ZonedDateTime.now(zone.toZoneId())
                                       .toLocalTime()
                                       .toString();
                chat("-----------------");
                chat(EnumChatFormatting.YELLOW + now);
                chat(EnumChatFormatting.RED + "Failed to load shaders:");
                for (val sh : report.erroredShaders) {
                    chat("  " + EnumChatFormatting.RED + sh);
                }
                chat(EnumChatFormatting.YELLOW + "Check the log for more details");
                chat("-----------------");
            }
        }

        if (!report.rpleCompatible && Loader.isModLoaded("rple")) {
            for (val line : Report.RPLE_WARNING) {
                Share.log.warn(line);
            }
            if (Minecraft.getMinecraft().thePlayer != null) {
                for (val line : Report.RPLE_WARNING) {
                    chat(EnumChatFormatting.DARK_RED + line);
                }
            }
        }
    }

    private static void chat(String message) {
        val plr = Minecraft.getMinecraft().thePlayer;
        if (plr != null) {
            plr.addChatMessage(new ChatComponentText(CHAT_PREFIX + message));
        }
    }
}
