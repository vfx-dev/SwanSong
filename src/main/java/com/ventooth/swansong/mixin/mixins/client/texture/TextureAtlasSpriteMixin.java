/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.texture;

import com.google.common.collect.Lists;
import com.ventooth.swansong.Share;
import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.mixin.extensions.TextureAtlasSpriteExt;
import com.ventooth.swansong.mixin.interfaces.ShadersTextureAtlasSprite;
import com.ventooth.swansong.pbr.PBRTextureEngine;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin implements IIcon, ShadersTextureAtlasSprite {
    @Shadow
    private AnimationMetadataSection animationMetadata;
    @Shadow
    protected List<int[][]> framesTextureData;
    @Shadow
    protected int width;
    @Shadow
    protected int height;
    @Shadow
    @Final
    private String iconName;

    @Shadow
    private static int[][] getFrameTextureData(int[][] data, int rows, int columns, int p_147962_3_) {
        return null;
    }

    @Shadow
    protected abstract void allocateFrameTextureData(int index);

    @Unique
    private TextureAtlasSprite s$spriteNormal = null;
    @Unique
    private TextureAtlasSprite s$spriteSpecular = null;
    @Unique
    private boolean s$isShadersSprite = false;

    @Override
    public boolean swan$isBaseSprite() {
        return !s$isShadersSprite;
    }

    @Override
    public TextureAtlasSprite swan$spriteNorm() {
        return s$spriteNormal;
    }

    @Override
    public TextureAtlasSprite swan$spriteSpec() {
        return s$spriteSpecular;
    }

    @Inject(method = "initSprite",
            at = @At("RETURN"),
            require = 1)
    private void initSprite(int inX, int inY, int originInX, int originInY, boolean rotated, CallbackInfo ci) {
        if (s$spriteNormal != null) {
            s$spriteNormal.initSprite(inX, inY, originInX, originInY, rotated);
        }
        if (s$spriteSpecular != null) {
            s$spriteSpecular.initSprite(inX, inY, originInX, originInY, rotated);
        }
    }

    @Inject(method = "updateAnimation",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void updateAnimation(CallbackInfo ci) {
        if (this.animationMetadata == null) {
            ci.cancel(); // TODO: [TEXTURE] Do we need this?
        }
    }

    @Inject(method = "loadSprite",
            at = @At("RETURN"),
            require = 1)
    private void loadShadersSprite(BufferedImage[] p_147964_1_,
                                   AnimationMetadataSection p_147964_2_,
                                   boolean p_147964_3_,
                                   CallbackInfo ci) {
        if (swan$isBaseSprite()) {
            val tex = Minecraft.getMinecraft().getTextureManager().getTexture(PBRTextureEngine.currentlyLoadingAtlas);
            if (!(tex instanceof TextureMap map)) {
                return;
            }
            val s$mipmapLevels = Minecraft.getMinecraft()
                                          .getTextureMapBlocks().mipmapLevels;
            AnimationMetadataSection animBase = null;
            try {
                val locBase = s$getIconResource(map, this.iconName);
                val resBase = swan$getResource(locBase);
                animBase = (AnimationMetadataSection) resBase.getMetadata("animation");
            } catch (Exception ignored) {
            }
            if (ShadersConfig.NormalMapping.value) {
                String nameNormal = this.iconName + "_n";
                ResourceLocation locNormal = s$getIconResource(map, this.iconName + "_n");
                if (swan$hasResource(locNormal)) {
                    try {
                        val sprite = TextureAtlasSpriteExt.newInstance(nameNormal);
                        @SuppressWarnings("DataFlowIssue") val spriteM = (TextureAtlasSpriteMixin) (Object) sprite;
                        spriteM.s$isShadersSprite = true;
                        sprite.copyFrom((TextureAtlasSprite) (Object) this);
                        spriteM.s$loadShaderSpriteFrames(animBase, locNormal, s$mipmapLevels + 1);
                        sprite.generateMipmaps(s$mipmapLevels);
                        this.s$spriteNormal = sprite;
                    } catch (IOException e) {
                        com.ventooth.swansong.Share.log.warn("Error loading normal texture: {}", nameNormal);
                        com.ventooth.swansong.Share.log.warn("{}: {}",
                                                             e.getClass()
                                                              .getName(),
                                                             e.getMessage());
                    }
                }
            }

            if (ShadersConfig.SpecularMapping.value) {
                String nameSpecular = this.iconName + "_s";
                ResourceLocation locSpecular = s$getIconResource(map, this.iconName + "_s");
                if (swan$hasResource(locSpecular)) {
                    try {
                        val sprite = TextureAtlasSpriteExt.newInstance(nameSpecular);
                        @SuppressWarnings("DataFlowIssue") val spriteM = (TextureAtlasSpriteMixin) (Object) sprite;
                        spriteM.s$isShadersSprite = true;
                        sprite.copyFrom((TextureAtlasSprite) (Object) this);
                        spriteM.s$loadShaderSpriteFrames(animBase, locSpecular, s$mipmapLevels + 1);
                        sprite.generateMipmaps(s$mipmapLevels);
                        s$spriteSpecular = sprite;
                    } catch (IOException e) {
                        com.ventooth.swansong.Share.log.warn("Error loading specular texture: {}", nameSpecular);
                        com.ventooth.swansong.Share.log.warn("{}: {}",
                                                             e.getClass()
                                                              .getName(),
                                                             e.getMessage());
                    }
                }
            }
        }
    }

    @Unique
    private static ResourceLocation s$getIconResource(TextureMap map, String iconName) {
        val res = new ResourceLocation(iconName);
        return map.completeResourceLocation(res, 0);
    }

    @Unique
    private void s$loadShaderSpriteFrames(@Nullable AnimationMetadataSection baseAnim, ResourceLocation loc, int mipmaplevels) throws IOException {
        IResource resource = swan$getResource(loc);
        BufferedImage bufferedimage = swan$readBufferedImage(resource.getInputStream());
        if (this.width != bufferedimage.getWidth()) {
            bufferedimage = swan$scaleImage(bufferedimage, this.width);
        }

        AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection) resource.getMetadata("animation");
        int[][] aint = new int[mipmaplevels][];
        aint[0] = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
        bufferedimage.getRGB(0,
                             0,
                             bufferedimage.getWidth(),
                             bufferedimage.getHeight(),
                             aint[0],
                             0,
                             bufferedimage.getWidth());
        if (animationmetadatasection == null) {
            if (baseAnim == null) {
                this.framesTextureData.add(aint);
            } else {
                try {
                    s$readAnimations(bufferedimage, baseAnim, aint);
                } catch (Exception ignored) {
                    this.framesTextureData.clear();
                    this.framesTextureData.add(aint);
                }
            }
        } else {
            s$readAnimations(bufferedimage, animationmetadatasection, aint);
        }
    }

    @Unique
    private void s$readAnimations(BufferedImage bufferedimage,
                                  AnimationMetadataSection animationmetadatasection,
                                  int[][] aint) {
        int i = bufferedimage.getHeight() / this.width;
        if (animationmetadatasection.getFrameCount() > 0) {
            for (int j : animationmetadatasection.getFrameIndexSet()) {
                if (j >= i) {
                    throw new RuntimeException("invalid frameindex " + j);
                }

                this.allocateFrameTextureData(j);
                this.framesTextureData.set(j, getFrameTextureData(aint, this.width, this.width, j));
            }

            this.animationMetadata = animationmetadatasection;
        } else {
            if (i == 1) {
                this.framesTextureData.add(aint);
                return;
            }

            List<AnimationFrame> list = Lists.newArrayList();

            for (int k = 0; k < i; ++k) {
                this.framesTextureData.add(getFrameTextureData(aint, this.width, this.width, k));
                list.add(new AnimationFrame(k, -1));
            }

            this.animationMetadata = new AnimationMetadataSection(list,
                                                                  this.width,
                                                                  this.height,
                                                                  animationmetadatasection.getFrameTime());
        }
    }

    @Unique
    private static BufferedImage swan$readBufferedImage(InputStream is) throws IOException {
        BufferedImage var1;
        try {
            var1 = ImageIO.read(is);
        } finally {
            IOUtils.closeQuietly(is);
        }

        return var1;
    }

    @Unique
    private static BufferedImage swan$scaleImage(BufferedImage bi, int w2) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        int h2 = h * w2 / w;
        BufferedImage bi2 = new BufferedImage(w2, h2, 2);
        Graphics2D g2 = bi2.createGraphics();
        Object method = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        if (w2 < w || w2 % w != 0) {
            method = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, method);
        g2.drawImage(bi, 0, 0, w2, h2, null);
        return bi2;
    }

    @Unique
    private static boolean swan$hasResource(ResourceLocation loc) {
        try {
            return swan$getResource(loc) != null;
        } catch (IOException e) {
            return false;
        }
    }

    @Unique
    private static IResource swan$getResource(ResourceLocation loc) throws IOException {
        return Minecraft.getMinecraft()
                        .getResourceManager()
                        .getResource(loc);
    }
}
