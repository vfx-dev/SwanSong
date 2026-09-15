/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.image;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.mixin.interfaces.PBRTextureHolder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TexDumper {
    public static final Logger log = Share.getLogger();

    private static Path texDumpDir;

    public static void dumpAllMc() {
        log.info("Dumping all textures");

        if (texDumpDir == null) {
            texDumpDir = Minecraft.getMinecraft().mcDataDir.toPath()
                                                           .resolve("swan_tex");
        }

        try {
            Files.createDirectories(texDumpDir);
            FileUtils.cleanDirectory(texDumpDir.toFile());
        } catch (IOException e) {
            log.error("Failed to prepare texture dump dir: ", e);
            return;
        }

        val textures = getMcTextures();
        log.info("Found: {} textures", textures.size());
        textures.forEach(TexDumper::saveTexture);
    }

    private static void saveTexture(ResourceLocation loc, ITextureObject tex) {
        final String rootTexName;
        {
            var texName = loc.getResourcePath();
            if (texName.endsWith(".png")) {
                texName = texName.substring(0, texName.length() - 4);
            }
            rootTexName = texName;
        }

        val domainPath = texDumpDir.resolve(loc.getResourceDomain());

        saveTexture(i -> domainPath.resolve(rootTexName + "_" + i + ".png"), tex.getGlTextureId());

        if (!(tex instanceof PBRTextureHolder texIDHolder)) {
            return;
        }
        if (!texIDHolder.swan$isValid()) {
            return;
        }

        //noinspection deprecation
        val multiTex = texIDHolder.swan$pbrTex();
        if (multiTex == null) {
            return;
        }

        if (ShadersConfig.NormalMapping.value) {
            val norm = multiTex.norm();
            if (norm != null) {
                saveTexture(i -> domainPath.resolve(rootTexName + "_n_" + i + ".png"), norm.glName());
            }
        }
        if (ShadersConfig.SpecularMapping.value) {
            val spec = multiTex.spec();
            if (spec != null) {
                saveTexture(i -> domainPath.resolve(rootTexName + "_s_" + i + ".png"), spec.glName());
            }
        }
    }

    private static void saveTexture(Function<Integer, Path> pathFn, int tex) {
        val rawImgs = ImageUtils.downloadGLTextureLevelsAsBGRA(tex);
        if (rawImgs.isEmpty()) {
            log.error("Failed to download texture (some mod is busted): path={}", pathFn.apply(0));
            return;
        }

        for (var i = 0; i < rawImgs.size(); i++) {
            val rawImg = rawImgs.get(i);
            val path = pathFn.apply(i);

            try {
                val bufImg = rawImgs.get(i)
                                    .asBufImg(true, false);
                Files.createDirectories(path.getParent());
                ImageIO.write(bufImg, "png", path.toFile());
                log.info("Saved texture: path={} size=[{}x{}]", path, rawImg.width(), rawImg.height());
            } catch (IOException e) {
                log.error("Failed to save texture (file problem): path={}", path);
                log.error("Trace: ", e);
            } catch (Exception e) {
                log.error("Failed to save texture (unknown problem): path={}", path);
                log.error("Trace: ", e);
            }
        }
    }

    private static Map<ResourceLocation, ITextureObject> getMcTextures() {
        val mc = Minecraft.getMinecraft();
        val texMap = mc.getTextureManager().mapTextureObjects;
        //noinspection unchecked
        return (Map<ResourceLocation, ITextureObject>) texMap;
    }
}
