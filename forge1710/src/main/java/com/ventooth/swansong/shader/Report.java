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

import com.ventooth.swansong.Share;
import com.ventooth.swansong.sufrace.FramebufferAttachment;
import com.ventooth.swansong.sufrace.Texture2D;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.RequiredArgsConstructor;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import cpw.mods.fml.common.Loader;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class Report {
    private static final String CHAT_PREFIX = "[" + EnumChatFormatting.BLUE + "Swan" + EnumChatFormatting.AQUA + "Song" + EnumChatFormatting.RESET + "] ";

    public String name;

    public long startTime;
    public long endTime;

    public boolean rpleCompatible;

    public final Map<String, ShaderInfo> foundShaders = new LinkedHashMap<>();
    public final Map<String, String> shadersFallback = new HashMap<>();
    public final Set<String> erroredShaders = new HashSet<>();
    public final Set<String> shadersFallbackInternal = new HashSet<>();
    public final Map<String, DrawBufferInfo> drawBuffers = new LinkedHashMap<>();
    public final Map<String, TextureInfo> customTextures = new LinkedHashMap<>();
    public final Map<String, CompositePipelineInfo> pipelines = new LinkedHashMap<>();

    @SuppressWarnings("SizeReplaceableByIsEmpty")
    public void print() {
        val log = Share.log;
        log.info("-----------------Shader Information-----------------");
        log.info("Active ShaderPack: {}", name);
        log.info("Loaded Shaders:");
        int longestName = 0;
        for (val shader : foundShaders.entrySet()) {
            longestName = Math.max(longestName,
                                   shader.getKey()
                                         .length());
        }
        for (val shader : foundShaders.entrySet()) {
            val name = shader.getKey();
            val info = shader.getValue();
            val tgt = new StringBuilder();
            for (val target : info.renderTargets) {
                if (tgt.length() != 0) {
                    tgt.append(", ");
                }
                tgt.append(target);
            }
            log.info("  {}{}  [{}]", name, padding(longestName - name.length()), tgt);
            if (shadersFallbackInternal.contains(name)) {
                if (erroredShaders.contains(name)) {
                    log.info("    Failed to load, using internal shader.");
                } else {
                    log.info("    Not present, using internal shader.");
                }
            }
            val fb = shadersFallback.get(name);
            if (fb != null) {
                log.info("    Using Fallback: {}", fb);
            }
        }
        boolean headerErrored = false;
        for (val errored: erroredShaders) {
            if (!foundShaders.containsKey(errored)) {
                if (!headerErrored) {
                    headerErrored = true;
                    log.error("Errored shaders:");
                }
                log.error("  {}", errored);
            }
        }
        log.info("Draw Buffers:");
        longestName = 0;
        for (val entry : drawBuffers.entrySet()) {
            longestName = Math.max(longestName,
                                   entry.getKey()
                                        .length());
        }
        val drawBuffers = new ArrayList<>(this.drawBuffers.entrySet());
        drawBuffers.sort(Map.Entry.comparingByKey());
        for (val entry : drawBuffers) {
            val name = entry.getKey();
            val info = entry.getValue();
            val padding = longestName - name.length();
            log.info("  {}{}  {}", name, padding(padding), info.ignored ? "(unused)" : info.format);
        }
        log.info("Custom Textures:");
        longestName = 0;
        int longestFormat = 0;
        for (val entry : customTextures.entrySet()) {
            longestName = Math.max(longestName,
                                   entry.getKey()
                                        .length());
            longestFormat = Math.max(longestFormat, entry.getValue().format.length());
        }
        for (val entry : customTextures.entrySet()) {
            val name = entry.getKey();
            val info = entry.getValue();
            log.info("  {}{}  {}{}  {}x{}",
                     name,
                     padding(longestName - name.length()),
                     info.format,
                     padding(longestFormat - info.format.length()),
                     info.width,
                     info.height);
        }
        if (pipelines.isEmpty()) {
            log.info("No Post-Processing Pipelines present");
        } else {
            for (val entry : pipelines.entrySet()) {
                val name = entry.getKey();
                val info = entry.getValue();
                log.info("Post Post-Processing Pipeline: {}", name);
                val sb = new StringBuilder();
                for (val stage : info.stages) {
                    log.info("  {}", stage.name);

                    // Mipmaps
                    sb.setLength(0);
                    for (val mipmap : stage.mipmaps) {
                        if (sb.length() != 0) {
                            sb.append(", ");
                        }
                        sb.append(mipmap);
                    }
                    if (sb.length() != 0) {
                        log.info("    Mipmap:  [{}]", sb);
                    }

                    // Inputs
                    sb.setLength(0);
                    for (val input : stage.inputs.entrySet()) {
                        if (sb.length() != 0) {
                            sb.append(", ");
                        }
                        sb.append(input.getValue())
                          .append(" -> ")
                          .append(input.getKey().gpuIndex());
                    }
                    if (sb.length() != 0) {
                        log.info("    Inputs:  [{}]", sb);
                    }

                    // Outputs
                    sb.setLength(0);
                    for (val output : stage.outputs.entrySet()) {
                        if (sb.length() != 0) {
                            sb.append(", ");
                        }
                        sb.append(output.getKey().name())
                          .append(" -> ")
                          .append(output.getValue());
                    }
                    if (sb.length() != 0) {
                        log.info("    Outputs: [{}]", sb);
                    }
                }
                if (info.postBlit != null) {
                    sb.setLength(0);
                    val from = info.postBlit.from;
                    val to = info.postBlit.to;
                    for (int i = 0; i < from.size(); i++) {
                        if (sb.length() != 0) {
                            sb.append(", ");
                        }
                        sb.append(from.get(i))
                          .append(" -> ")
                          .append(to.get(i));
                    }
                    log.info("  Post Blit: [{}]", sb);
                }
            }
        }

        blk: if (!erroredShaders.isEmpty()) {
            val plr = Minecraft.getMinecraft().thePlayer;
            if (plr == null) {
                break blk;
            }
            val zone = TimeZone.getDefault();
            val now = ZonedDateTime.now(zone.toZoneId()).toLocalTime().toString();
            plr.addChatMessage(new ChatComponentText(CHAT_PREFIX + "-----------------"));
            plr.addChatMessage(new ChatComponentText(CHAT_PREFIX + EnumChatFormatting.YELLOW + now));
            plr.addChatMessage(new ChatComponentText(CHAT_PREFIX + EnumChatFormatting.RED + "Failed to load shaders:"));
            for (val sh: erroredShaders) {
                plr.addChatMessage(new ChatComponentText(CHAT_PREFIX + "  " + EnumChatFormatting.RED + sh));
            }
            plr.addChatMessage(new ChatComponentText(CHAT_PREFIX + EnumChatFormatting.YELLOW + "Check the log for more details"));
            plr.addChatMessage(new ChatComponentText(CHAT_PREFIX + "-----------------"));
        }
        if (!rpleCompatible && Loader.isModLoaded("rple")) {
            val txt = """
                    You are using a shaderpack which is not marked
                    as compatible with RPLE. Do not report
                    issues to SwanSong/RPLE.
                    If the shaderpack works fine,
                    add rpleCompatible=true to shaders.properties
                    """.split("\n");
            for (val line: txt) {
                log.warn(line);
            }
            val plr = Minecraft.getMinecraft().thePlayer;
            if (plr != null) {
                for (val line: txt) {
                    plr.addChatMessage(new ChatComponentText(CHAT_PREFIX + EnumChatFormatting.DARK_RED + line));
                }
            }
        }
    }

    private static String padding(int size) {
        if (size <= 0) {
            return "";
        }
        val res = new StringBuilder();
        //noinspection StringRepeatCanBeUsed
        for (int i = 0; i < size; i++) {
            res.append(' ');
        }
        return res.toString();
    }

    @RequiredArgsConstructor
    public static class DrawBufferInfo {
        public final boolean ignored;
        public final String format;
    }

    public static class CompositePipelineInfo {
        public final List<TextureInfo> auxTextures = new ArrayList<>();
        public final List<CompositeStageInfo> stages = new ArrayList<>();
        public CompositePostBlit postBlit = null;
    }

    public static class CompositePostBlit {
        public final List<String> from = new ArrayList<>();
        public final List<String> to = new ArrayList<>();
    }

    @RequiredArgsConstructor
    public static class CompositeStageInfo {
        public final String name;
        public final ObjectList<String> mipmaps = new ObjectArrayList<>(16);
        public final Map<CompositeTextureData, String> inputs = new EnumMap<>(CompositeTextureData.class);
        public final Map<FramebufferAttachment, String> outputs = new EnumMap<>(FramebufferAttachment.class);
    }

    public static class ShaderInfo {
        public final Set<String> renderTargets = new LinkedHashSet<>();
    }

    @RequiredArgsConstructor
    public static class TextureInfo {
        public final int width;
        public final int height;
        public final String format;

        public TextureInfo(Texture2D tex) {
            this(tex.width(), tex.height(), BufferNameUtil.gbufferFormatNameFromEnum(tex.internalFormat()));
        }
    }
}
