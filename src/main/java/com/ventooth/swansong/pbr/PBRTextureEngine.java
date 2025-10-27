/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.pbr;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.mixin.interfaces.PBRTextureHolder;
import com.ventooth.swansong.mixin.interfaces.ShadersTextureAtlasSprite;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderSamplers;
import com.ventooth.swansong.shader.ShaderState;
import com.ventooth.swansong.sufrace.PBRTexture2D;
import com.ventooth.swansong.sufrace.Texture2D;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

// TODO: Missing hooks for: Layered Textures (Horses) & Clocks/Compass in Item Frame
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PBRTextureEngine {
    private static final Logger log = Share.getLogger();

    /**
     * Base color only for the base atlas itself.
     */
    private static final int BASE_ATLAS_DEFAULT_COL = 0x00000000;

    private static final int BASE_DEFAULT_COL = 0xFFFFFFFF;
    private static final int NORM_DEFAULT_COL = 0xFF7F7FFF;
    private static final int SPEC_DEFAULT_COL = 0x00000000;

    private static boolean isInitialized = false;
    private static boolean isDefaultTexUnit = false;

    private static Texture2D fallbackBaseTex;
    private static Texture2D fallbackNormTex;
    private static Texture2D fallbackSpecTex;

    private static int @Nullable [] intArray = null;
    private static @Nullable IntBuffer intBuffer = null;

    public static void init() {
        if (isInitialized) {
            log.warn("Already initialized?");
            return;
        }

        try {
            isDefaultTexUnit = false;

            assert fallbackBaseTex == null;
            assert fallbackNormTex == null;
            assert fallbackSpecTex == null;

            GL13.glActiveTexture(baseTexUnit());
            fallbackBaseTex = Texture2D.ofColoredPixel("pbr_fallback_base", BASE_DEFAULT_COL);
            fallbackNormTex = Texture2D.ofColoredPixel("pbr_fallback_norm", NORM_DEFAULT_COL);
            fallbackSpecTex = Texture2D.ofColoredPixel("pbr_fallback_spec", SPEC_DEFAULT_COL);

            intArray = null;
            intBuffer = null;
        } catch (RuntimeException | Error e) {
            log.fatal("Failed to initialize", e);
            throw e;
        } finally {
            GL13.glActiveTexture(baseTexUnit());
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }

        isInitialized = true;
        log.debug("Initialized");
    }

    public static void deinit() {
        if (!isInitialized) {
            log.warn("Already deinitialized?");
            return;
        }

        try {
            isDefaultTexUnit = false;

            assert fallbackBaseTex != null;
            assert fallbackNormTex != null;
            assert fallbackSpecTex != null;

            GL13.glActiveTexture(baseTexUnit());
            fallbackBaseTex.deinit();
            fallbackBaseTex = null;
            fallbackNormTex.deinit();
            fallbackNormTex = null;
            fallbackSpecTex.deinit();
            fallbackSpecTex = null;

            intArray = null;
            intBuffer = null;
        } catch (RuntimeException | Error e) {
            log.fatal("Failed to deinitialized", e);
            throw e;
        } finally {
            GL13.glActiveTexture(baseTexUnit());
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }

        isInitialized = false;
        log.debug("Deinitialized");
    }

    public static void isDefaultTexUnit(boolean toggle) {
        isDefaultTexUnit = toggle;
    }

    public static void bindPbrTex(@Nullable PBRTextureHolder pbrHolder) {
        assert isInitialized : "Not Initialized";
        assert ShaderEngine.graph.isManaged() : "Not in managed mode";

        // Probably dealing with the lightmap
        if (!isDefaultTexUnit) {
            return;
        }

        final PBRTexture2D norm;
        final PBRTexture2D spec;

        // Null or invalid texture will just use fallbacks
        if (pbrHolder == null || !pbrHolder.swan$isValid()) {
            norm = null;
            spec = null;
        } else {
            // Will will also support *partial* fallbacks
            val pbrTex = getPbrTexOrInit(pbrHolder);
            norm = pbrTex.norm();
            spec = pbrTex.spec();

            if (pbrHolder.swan$isAtlas()) {
                ShaderState.updateAtlasSize(pbrHolder.swan$width(), pbrHolder.swan$height());
            } else {
                ShaderState.updateAtlasSize(0, 0);
            }
        }

        GL13.glActiveTexture(normTexUnit());
        if (norm != null) {
            norm.bind();
        } else {
            fallbackNormTex.bind();
        }
        GL13.glActiveTexture(specTexUnit());
        if (spec != null) {
            spec.bind();
        } else {
            fallbackSpecTex.bind();
        }

        GL13.glActiveTexture(baseTexUnit());
    }

    public static void logInitialLoad(PBRTextureHolder pbrHolder) {
        log.debug("Initial load: {}", pbrHolder.swan$baseToString());
    }

    public static void initAtlas(PBRTextureHolder pbrHolder, int mipLevels, float anisotropy) {
        final PBRTexture2D norm;
        if (ShadersConfig.NormalMapping.value) {
            norm = createAtlasTex(pbrHolder, mipLevels, anisotropy, 'n');
        } else {
            norm = null;
        }
        final PBRTexture2D spec;
        if (ShadersConfig.SpecularMapping.value) {
            spec = createAtlasTex(pbrHolder, mipLevels, anisotropy, 's');
        } else {
            spec = null;
        }
        // Avoid state pollution
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        val baseLoc = pbrHolder.swan$base();
        val pbrTex = new PBRTexture2D.Bundle(baseLoc, norm, spec);
        pbrHolder.swan$pbrTex(pbrTex);
        log.debug("Initialized PBR for atlas: {}", baseLoc);
    }

    private static @Nullable PBRTexture2D createAtlasTex(PBRTextureHolder pbrHolder,
                                                         int mipLevels,
                                                         float anisotropy,
                                                         char postfix) {
        val baseLoc = pbrHolder.swan$base();
        final ResourceLocation pbrLoc;
        try {
            pbrLoc = getPbrLoc(baseLoc, postfix);
        } catch (Exception e) {
            log.warn("Failed to decode atlas loc??: ", e);
            return null;
        }

        val width = pbrHolder.swan$width();
        val height = pbrHolder.swan$height();
        val glName = GL11.glGenTextures();
        TextureUtil.allocateTextureImpl(glName, mipLevels, width, height, anisotropy);

        return PBRTexture2D.ofWrapped(pbrLoc, width, height, glName);
    }

    //FalseTweaks mixin lands in here. Double check before modifying!
    public static void uploadAtlasSpritesAll(PBRTextureHolder pbrHolder,
                                             int baseGlName,
                                             ObjectList<TextureAtlasSprite> baseSprites,
                                             int mipmapLevels) {
        val pbrTex = pbrHolder.swan$pbrTex();
        val norm = pbrTex.norm();
        val spec = pbrTex.spec();

        final int normGlName;
        final int specGlName;

        if (ShadersConfig.NormalMapping.value) {
            normGlName = norm != null ? norm.glName() : 0;
        } else {
            normGlName = 0;
        }

        if (ShadersConfig.SpecularMapping.value) {
            specGlName = spec != null ? spec.glName() : 0;
        } else {
            specGlName = 0;
        }

        val normSprites = new ObjectArrayList<TextureAtlasSprite>();
        val specSprites = new ObjectArrayList<TextureAtlasSprite>();
        for (val sprite : baseSprites) {
            val shaderSprite = (ShadersTextureAtlasSprite) sprite;
            val normSprite = shaderSprite.swan$spriteNorm();
            if (normSprite != null) {
                normSprites.add(normSprite);
            }
            val specSprite = shaderSprite.swan$spriteSpec();
            if (specSprite != null) {
                specSprites.add(specSprite);
            }
        }

        val width = pbrHolder.swan$width();
        val height = pbrHolder.swan$height();

        // Always upload even if no sprites are present, because the default color still gets set!
        if (normGlName != 0) {
            log.debug("Uploading: {} sprites to: {}", normSprites.size(), norm.loc());
            uploadAtlasSpriteLayer(normGlName, normSprites, width, height, mipmapLevels, NORM_DEFAULT_COL);
        }
        if (specGlName != 0) {
            log.debug("Uploading: {} sprites to: {}", specSprites.size(), spec.loc());
            uploadAtlasSpriteLayer(specGlName, specSprites, width, height, mipmapLevels, SPEC_DEFAULT_COL);
        }

        log.debug("Uploading: {} sprites to: {}", baseSprites.size(), pbrHolder.swan$base());
        uploadAtlasSpriteLayer(baseGlName, baseSprites, width, height, mipmapLevels, BASE_ATLAS_DEFAULT_COL);
        // Last bound texture will the be base atlas, so the correct state will be preserved
    }

    //FalseTweaks mixin lands in here. Double check before modifying!
    public static void updateAnimations(TextureMap map, List<TextureAtlasSprite> listAnimatedSprites, boolean updateNorm, boolean updateSpec) {
        val holder = (PBRTextureHolder) map;
        if (!holder.swan$isValid()) {
            return;
        }

        updateNorm &= ShadersConfig.NormalMapping.value;
        updateSpec &= ShadersConfig.SpecularMapping.value;
        if (!(updateNorm || updateSpec)) {
            return;
        }

        val pbrTex = holder.swan$pbrTex();
        if (pbrTex == null) {
            return;
        }

        val norm = updateNorm ? pbrTex.norm() : null;
        val spec = updateSpec ? pbrTex.spec() : null;
        if (norm == null && spec == null) {
            return;
        }

        if (norm != null) {
            norm.bind();

            for (val sprite : listAnimatedSprites) {
                val spriteS = (ShadersTextureAtlasSprite) sprite;
                if (spriteS.swan$spriteNorm() != null) {
                    // TODO: Sync frame counter for compass and clock
                    //                    spriteS.swan$spriteNormal().frameCounter = sprite.frameCounter;

                    spriteS.swan$spriteNorm()
                           .updateAnimation();
                }
            }
        }

        if (spec != null) {
            spec.bind();

            for (val sprite : listAnimatedSprites) {
                val spriteS = (ShadersTextureAtlasSprite) sprite;
                if (spriteS.swan$spriteSpec() != null) {
                    // TODO: Sync frame counter for compass and clock
                    //                    spriteS.swan$spriteSpecular().frameCounter = sprite.frameCounter;

                    spriteS.swan$spriteSpec()
                           .updateAnimation();
                }
            }
        }

        TextureUtil.bindTexture(map.getGlTextureId());
    }

    private static void uploadAtlasSpriteLayer(int glName,
                                               ObjectList<TextureAtlasSprite> sprites,
                                               int width,
                                               int height,
                                               int mipLevels,
                                               int defaultCol) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glName);

        mipLevels += 1;

        int[][] buf = new int[mipLevels][];
        for (int mip = 0; mip < mipLevels; mip++) {
            int w = width >> mip;
            int h = height >> mip;
            int[] currentMip = new int[w * h];
            buf[mip] = currentMip;
            Arrays.fill(currentMip, defaultCol);
        }
        for (val sprite : sprites) {
            int originX = sprite.getOriginX();
            int originY = sprite.getOriginY();
            int iconWidth = sprite.getIconWidth();
            int iconHeight = sprite.getIconHeight();
            int[][] frame = sprite.getFrameTextureData(0);
            int levels = Math.min(frame.length, mipLevels);
            for (int mip = 0; mip < levels; mip++) {
                int[] mipFrame = frame[mip];
                int x = originX >> mip;
                int y = originY >> mip;
                int w = iconWidth >> mip;
                int h = iconHeight >> mip;
                int imgW = width >> mip;
                val mipTex = buf[mip];
                for (int y1 = 0; y1 < h; y1++) {
                    int source = y1 * w;
                    int dest = (y + y1) * imgW + x;
                    System.arraycopy(mipFrame, source, mipTex, dest, w);
                }
            }
        }
        TextureUtil.uploadTextureMipmap(buf, width, height, 0, 0, false, false);
    }

    public static void deinitPbrTex(PBRTexture2D.Bundle pbrTex, String reason) {
        val norm = pbrTex.norm();
        final ResourceLocation normLoc;
        if (norm == null) {
            normLoc = null;
        } else {
            norm.deinit();
            normLoc = norm.loc();
        }

        val spec = pbrTex.spec();
        final ResourceLocation specLoc;
        if (spec == null) {
            specLoc = null;
        } else {
            spec.deinit();
            specLoc = spec.loc();
        }

        log.debug("Deinit PBR {}: {}, (norm={}, spec={})", reason, pbrTex.base(), normLoc, specLoc);
    }

    private static PBRTexture2D.Bundle getPbrTexOrInit(PBRTextureHolder pbrHolder) {
        var pbrTex = pbrHolder.swan$pbrTex();
        if (pbrTex != null) {
            return pbrTex;
        }

        val norm = ShadersConfig.NormalMapping.value ? loadPbrTex(pbrHolder, 'n') : null;
        val spec = ShadersConfig.SpecularMapping.value ? loadPbrTex(pbrHolder, 's') : null;
        // Avoid state pollution
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        pbrTex = new PBRTexture2D.Bundle(pbrHolder.swan$base(), norm, spec);
        pbrHolder.swan$pbrTex(pbrTex);
        log.debug("Initialized PBR Tex for: ({})->[norm={},spec={}]", pbrTex.base(), norm != null, spec != null);

        return pbrTex;
    }

    private static @Nullable PBRTexture2D loadPbrTex(PBRTextureHolder pbrHolder, char postfix) {
        val baseLoc = pbrHolder.swan$base();
        final ResourceLocation pbrLoc;
        try {
            pbrLoc = getPbrLoc(baseLoc, postfix);
        } catch (Exception e) {
            log.warn("Failed to load PBR texture: ", e);
            return null;
        }

        final InputStream is;
        try {
            is = Minecraft.getMinecraft()
                          .getResourceManager()
                          .getResource(pbrLoc)
                          .getInputStream();
        } catch (Exception e) {
            log.debug("PBR Texture not found: {}", pbrLoc);
            return null;
        }

        final BufferedImage img;
        try {
            img = ImageIO.read(is);
        } catch (Exception e) {
            log.warn("Failed to decode image: " + pbrLoc, e);
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }

        val width = pbrHolder.swan$width();
        val height = pbrHolder.swan$height();
        val pbrWidth = img.getWidth();
        val pbrHeight = img.getHeight();

        if (width != pbrWidth || height != pbrHeight) {
            log.warn("Size mismatch between ({})->[{}x{}] and ({})->[{}x{}]",
                     baseLoc,
                     width,
                     height,
                     pbrLoc,
                     pbrWidth,
                     pbrHeight);
            return null;
        }

        try {
            val size = width * height;
            val pixels = getIntArray(size);
            img.getRGB(0, 0, width, height, pixels, 0, width);

            val buf = getIntBuffer(size);
            buf.put(pixels, 0, size);
            buf.flip();

            val meta = pbrHolder.swan$meta();
            val clamp = meta.clamp();
            val blur = meta.blur();

            val tex = PBRTexture2D.ofIntBuffer(pbrLoc, width, height, clamp, blur, buf);
            log.debug("Loaded PBR Texture: {}", pbrLoc);
            return tex;
        } catch (Exception e) {
            intArray = null;
            intBuffer = null;

            log.error("Unexpected error trying to load: " + pbrLoc, e);
            return null;
        }
    }

    private static ResourceLocation getPbrLoc(ResourceLocation baseLoc, char postfix) throws IllegalArgumentException {
        val path = baseLoc.getResourcePath();
        if (!path.endsWith(".png")) {
            throw new IllegalArgumentException("Texture location does not end with '.png': " + baseLoc);
        }
        val pbrPath = path.substring(0, path.length() - ".png".length()) + "_" + postfix + ".png";
        return new ResourceLocation(baseLoc.getResourceDomain(), pbrPath);
    }

    private static int baseTexUnit() {
        return GL13.GL_TEXTURE0 + ShaderSamplers.GBuffer.texture();
    }

    private static int normTexUnit() {
        return GL13.GL_TEXTURE0 + ShaderSamplers.GBuffer.normals();
    }

    private static int specTexUnit() {
        return GL13.GL_TEXTURE0 + ShaderSamplers.GBuffer.specular();
    }

    private static int[] getIntArray(int size) {
        if (intArray == null || intArray.length < size) {
            intArray = new int[size];
            log.debug("Expanded int array to: {}", size);
        }
        return intArray;
    }

    private static IntBuffer getIntBuffer(int size) {
        if (intBuffer == null || intBuffer.capacity() < size) {
            intBuffer = BufferUtils.createIntBuffer(size);
            log.debug("Expanded int buffer to: {}", size);
        }
        intBuffer.clear();
        return intBuffer;
    }

    public static IResource getResource(ResourceLocation loc) throws IOException {
        return Minecraft.getMinecraft()
                        .getResourceManager()
                        .getResource(loc);
    }
}
