/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.resources;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.config.Configs;
import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.resources.pack.DefaultShaderPack;
import com.ventooth.swansong.resources.pack.ResolvedShaderPack;
import com.ventooth.swansong.resources.pack.ResolvedShaderPack.WorldSpecializationPredicate;
import com.ventooth.swansong.resources.pack.ShaderPack;
import com.ventooth.swansong.shader.ShaderEngine;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.client.Minecraft;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipFile;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderPackManager {
    private static final Logger log = Share.getLogger();

    public static final String DISABLED_SHADER_PACK_NAME = "(disabled)";

    public static String currentShaderPackName = DISABLED_SHADER_PACK_NAME;

    private static Path shaderpacksDir;
    private static Path shaderpacksDebugDir;

    private static List<String> detectedShaderpacks = ShadersConfig.enableReferenceShaderPack
                                                      ? Arrays.asList(DISABLED_SHADER_PACK_NAME, DefaultShaderPack.NAME)
                                                      : Collections.singletonList(DISABLED_SHADER_PACK_NAME);

    public static void init() {
        val minecraftDir = Minecraft.getMinecraft().mcDataDir.toPath();
        shaderpacksDir = minecraftDir.resolve("shaderpacks");
        shaderpacksDebugDir = shaderpacksDir.resolve("debug");

        ensureDirExists("Shader Pack", shaderpacksDir);

        refreshShaderPackNames();
        setShaderPackByName(ShadersConfig.CurrentShaderPack);
    }

    public static File resolveFile(String name) {
        return resolvePath(name).toFile();
    }

    public static Path resolvePath(String name) {
        return shaderpacksDir.resolve(name);
    }

    public static void cleanDebugDir() throws IOException {
        ensureDirExists("Shader Pack Debug output", shaderpacksDebugDir);
        FileUtils.cleanDirectory(shaderpacksDebugDir.toFile());
    }

    public static void dumpShader(String name, String src) throws IOException {
        ensureDirExists("Shader Pack Debug output", shaderpacksDebugDir);
        FileUtils.write(shaderpacksDebugDir.resolve(name)
                                           .toFile(), src);
    }

    //Does not advance the offset of src
    public static void dumpShader(String name, ByteBuffer src) {
        try {
            val file = shaderpacksDebugDir.resolve(name)
                                          .toFile();
            ensureDirExists("Shader Pack Debug output",
                            file.toPath()
                                .getParent());
            @Cleanup val fileOut = new FileOutputStream(shaderpacksDebugDir.resolve(name)
                                                                           .toFile());
            val theSlice = src.slice();
            theSlice.limit(theSlice.limit() - 1);
            val fc = fileOut.getChannel();
            while (theSlice.remaining() > 0) {
                fc.write(theSlice);
            }
            fc.close();
        } catch (IOException e) {
            Share.log.error("Failed to dump shader: ", e);
        }
    }

    private static void ensureDirExists(String name, Path dir) {
        if (Files.isDirectory(dir)) {
            return;
        }

        try {
            Files.createDirectories(dir);
            Share.log.debug("Created dir ({}): {}", name, dir);
        } catch (IOException e) {
            Share.log.fatal("Failed to create dir ({}): {}", name, dir);
            throw new UncheckedIOException(e);
        }
    }

    public static void saveShaderSettings() {
        ShaderEngine.scheduleShaderPackReload();
        Configs.syncConfigFile();
    }

    public static void openShaderPacksDir() {
        try {
            Desktop.getDesktop()
                   .open(shaderpacksDir.toFile());
        } catch (IOException e) {
            Share.log.error("Failed to open shaderpacks directory", e);
        }
    }

    public static String getCurrentShaderPackName() {
        return currentShaderPackName;
    }

    public static void setShaderPackByName(String shaderPackName) {
        if (detectedShaderpacks.contains(shaderPackName)) {
            currentShaderPackName = shaderPackName;
        } else {
            currentShaderPackName = DISABLED_SHADER_PACK_NAME;
        }
        ShadersConfig.CurrentShaderPack = currentShaderPackName;
        saveShaderSettings();
    }

    public static void refreshShaderPackNames() {
        val newShaders = new ArrayList<String>();
        try {
            @Cleanup val shadersDir = Files.list(shaderpacksDir);
            shadersDir.forEach(subPath -> {
                if (Objects.equals(subPath, shaderpacksDebugDir)) {
                    return;
                }
                try {
                    val realPath = subPath.toRealPath();
                    if (Files.isRegularFile(realPath)) {
                        try(val zip = new ZipFile(realPath.toFile())) {
                            val entries = zip.entries();
                            while (entries.hasMoreElements()) {
                                val entry = entries.nextElement();
                                val name = entry.getName();
                                if ("shaders".equals(name) || name.startsWith("shaders/")) {
                                    newShaders.add(subPath.getFileName()
                                                          .toString());
                                    break;
                                }
                            }
                        } catch (IOException ignored) {}
                    } else if (Files.isDirectory(realPath)) {
                        val nestDir = realPath.resolve("shaders").toRealPath();
                        if (Files.isDirectory(nestDir)) {
                            newShaders.add(subPath.getFileName()
                                                  .toString());
                        }
                    }
                } catch (IOException e) {
                    Share.log.error("Error while processing shaderpack {}", subPath.getFileName().toString());
                    Share.log.error("Stacktrace:", e);
                }
            });
            newShaders.sort(Comparator.naturalOrder());

            if (ShadersConfig.enableReferenceShaderPack) {
                newShaders.add(0, DefaultShaderPack.NAME);
            }
            newShaders.add(0, DISABLED_SHADER_PACK_NAME);
            detectedShaderpacks = newShaders;
        } catch (IOException e) {
            Share.log.error("Error while detecting shaderpacks. Using fallback.", e);
            detectedShaderpacks = Arrays.asList(DISABLED_SHADER_PACK_NAME, DefaultShaderPack.NAME);
        }
    }

    @UnmodifiableView
    public static List<String> getShaderPackNames() {
        return Collections.unmodifiableList(detectedShaderpacks); //TODO: Should include the internal shader pack on top, but ONLY if it is active.
    }

    public static @Nullable ShaderPack createShaderPack() {
        if (DISABLED_SHADER_PACK_NAME.equals(currentShaderPackName)) {
            return null;
        }
        if (DefaultShaderPack.NAME.equals(currentShaderPackName)) {
            if (ShadersConfig.enableReferenceShaderPack) {
                return DefaultShaderPack.INSTANCE;
            } else {
                return null;
            }
        }
        val builder = new ResolvedShaderPack.Builder(currentShaderPackName);
        boolean successful = false;
        try {
            val theFile = shaderpacksDir.resolve(currentShaderPackName).toRealPath();
            if (Files.isDirectory(theFile)) {
                successful = createDirShaderPack(theFile, builder);
            } else if (Files.isRegularFile(theFile)) {
                successful = createFileShaderPack(theFile, builder);
            }
        } catch (IOException e) {
            log.error("Exception in loading shader pack:", e);
        }
        if (successful) {
            return builder.build();
        }
        log.error("Failed to load shader pack named \"" + currentShaderPackName + "\"");
        setShaderPackByName(DISABLED_SHADER_PACK_NAME);
        return null;
    }

    public static void saveShaderPackConfig(List<String> dataz) {
        try {
            Files.write(shaderpacksDir.resolve(currentShaderPackName + ".txt"), dataz);
        } catch (IOException e) {
            log.error("Failed to save shader pack config:", e);
        }
    }

    public static byte @Nullable [] readShaderPackConfig() {
        val configPath = shaderpacksDir.resolve(currentShaderPackName + ".txt");
        if (!Files.isRegularFile(configPath)) {
            return null;
        }

        try {
            return Files.readAllBytes(configPath);
        } catch (IOException e) {
            log.error("Failed to load shader pack config:", e);
            return null;
        }
    }

    private static boolean createDirShaderPack(Path theFile, ResolvedShaderPack.Builder builder) throws IOException {
        Files.walkFileTree(theFile, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attrs) throws IOException {
                if (!Files.isRegularFile(file)) {
                    return super.visitFile(file, attrs);
                }
                @Cleanup val input = Files.newInputStream(file);
                var rPath = theFile.relativize(file)
                                   .toString();
                if (File.separatorChar != '/') {
                    rPath = rPath.replace(File.separatorChar, '/');
                }
                builder.add(rPath, input);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult preVisitDirectory(@NotNull Path dir, BasicFileAttributes attrs) throws IOException {
                val rPath = theFile.relativize(dir);
                if (rPath.getNameCount() == 2 &&
                    rPath.getName(0)
                         .toString()
                         .equals("shaders") &&
                    rPath.getName(1)
                         .toString()
                         .startsWith("world")) {
                    val worldSuffix = rPath.getName(1)
                                           .toString()
                                           .substring("world".length());
                    try {
                        int worldIndex = Integer.parseInt(worldSuffix);
                        if (worldIndex == 0) {
                            builder.hasWorld0();
                        }
                        builder.addSpecialization(new WorldSpecializationPredicate.ByID(worldIndex,
                                                                                        "world" + worldSuffix));
                    } catch (NumberFormatException ignored) {
                    }
                }
                return super.preVisitDirectory(dir, attrs);
            }
        });
        return true;
    }

    private static boolean createFileShaderPack(Path theFile, ResolvedShaderPack.Builder builder) throws IOException {
        val fileName = theFile.getFileName()
                              .toString();
        if (!fileName.endsWith(".zip")) {
            return false;
        }
        @Cleanup val zipFile = new ZipFile(theFile.toFile());
        val entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement();
            val name = entry.getName();
            if (entry.isDirectory()) {
                val parts = name.split("/");
                if (parts.length == 2 && parts[0].equals("shaders") && parts[1].startsWith("world")) {
                    val worldSuffix = parts[1].substring("world".length());
                    try {
                        int worldIndex = Integer.parseInt(worldSuffix);
                        if (worldIndex == 0) {
                            builder.hasWorld0();
                        }
                        builder.addSpecialization(new WorldSpecializationPredicate.ByID(worldIndex,
                                                                                        "world" + worldSuffix));
                    } catch (NumberFormatException ignored) {
                    }
                }
                continue;
            }
            builder.add(name, zipFile.getInputStream(entry));
        }
        return true;
    }
}
