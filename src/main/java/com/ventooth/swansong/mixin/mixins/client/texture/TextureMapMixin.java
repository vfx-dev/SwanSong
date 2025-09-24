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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.mixin.interfaces.PBRAtlas;
import com.ventooth.swansong.mixin.interfaces.PBRTextureHolder;
import com.ventooth.swansong.mixin.interfaces.ShadersTextureAtlasSprite;
import com.ventooth.swansong.pbr.PBRTextureEngine;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(TextureMap.class)
public abstract class TextureMapMixin extends AbstractTexture implements PBRTextureHolder, PBRAtlas {
    @Shadow
    @Final
    public static ResourceLocation locationBlocksTexture;
    @Shadow
    @Final
    public static ResourceLocation locationItemsTexture;

    @Shadow
    @Final
    private int textureType;
    @Shadow
    public int mipmapLevels;
    @Shadow
    @Final
    private String basePath;
    @Shadow
    @Final
    private List<TextureAtlasSprite> listAnimatedSprites;
    @Shadow
    @Final
    private Map<String, TextureAtlasSprite> mapUploadedSprites;

    @Unique
    private ResourceLocation swan$mapLoc;

    @Override
    public void swan$mapLoc(ResourceLocation location) {
        this.swan$mapLoc = location;
    }

    @Override
    public boolean swan$supportsPbr() {
        return true;
    }

    @Override
    public boolean swan$isAtlas() {
        return true;
    }

    @SuppressWarnings({"DefaultAnnotationParam", "UnnecessaryUnsafe"})
    @Inject(method = "<init>(ILjava/lang/String;Z)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureMap;registerIcons()V",
                     unsafe = true),
            require = 1)
    private void init(CallbackInfo ci) {
        switch (this.textureType) {
            case 0 -> swan$mapLoc = TextureMapMixin.locationBlocksTexture;
            case 1 -> swan$mapLoc = TextureMapMixin.locationItemsTexture;
            default -> swan$mapLoc = null;
        }
    }

    @WrapOperation(method = "loadTextureAtlas",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/util/MathHelper;calculateLogBaseTwo(I)I"),
                   require = 1)
    private int safeMipMaps(int value, Operation<Integer> original) {
        return Math.max(original.call(value), 0);
    }

    @WrapOperation(method = "loadTextureAtlas",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/texture/TextureUtil;allocateTextureImpl(IIIIF)V"),
                   require = 1)
    private void allocateShaderTexture(int texID,
                                       int mipLevels,
                                       int width,
                                       int height,
                                       float anisotropy,
                                       Operation<Void> original,
                                       @Local Stitcher stitcher) {
        original.call(texID, mipmapLevels, width, height, anisotropy);
        swan$baseInit(swan$mapLoc, width, height, false, true);
        PBRTextureEngine.initAtlas(this, mipLevels, anisotropy);
    }

    @Redirect(method = "loadTextureAtlas",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/Iterator;hasNext()Z",
                       ordinal = 0),
              slice = @Slice(from = @At(value = "CONSTANT",
                                        args = "stringValue=Uploading GL texture")),
              require = 1)
    private boolean customUploadTextures(Iterator<TextureAtlasSprite> slotsIter,
                                         @Local Stitcher stitcher,
                                         @Local HashMap missing) {
        try {
            val slots = new ObjectArrayList<>(slotsIter);
            PBRTextureEngine.uploadAtlasSpritesAll(this, this.getGlTextureId(), slots, mipmapLevels);

            for (val slot : slots) {
                val s = slot.getIconName();
                missing.remove(s);
                mapUploadedSprites.put(s, slot);
                if (slot.hasAnimationMetadata()) {
                    listAnimatedSprites.add(slot);
                } else {
                    slot.clearFramesTextureData();
                }
            }
            return false;
        } catch (Throwable throwable) {
            CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
            CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Texture being stitched together");
            crashreportcategory1.addCrashSection("Atlas path", this.basePath);
            throw new ReportedException(crashreport1);
        }
    }


    /**
     * TODO turn into proper mixins
     *
     * @author FalsePattern
     * @reason Original method is tiny
     */
    @Overwrite
    public void updateAnimations() {
        var updateNorm = false;
        var updateSpec = false;
        TextureUtil.bindTexture(this.getGlTextureId());

        for (val sprite : this.listAnimatedSprites) {
            val spriteS = (ShadersTextureAtlasSprite) sprite;
            sprite.updateAnimation();
            if (spriteS.swan$spriteNorm() != null) {
                updateNorm = true;
            }

            if (spriteS.swan$spriteSpec() != null) {
                updateSpec = true;
            }
        }

        if (!swan$isValid()) {
            return;
        }

        updateNorm &= ShadersConfig.NormalMapping.value;
        updateSpec &= ShadersConfig.SpecularMapping.value;
        if (!(updateNorm || updateSpec)) {
            return;
        }

        val pbrTex = swan$pbrTex();
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

            for (val sprite : this.listAnimatedSprites) {
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

            for (val sprite : this.listAnimatedSprites) {
                val spriteS = (ShadersTextureAtlasSprite) sprite;
                if (spriteS.swan$spriteSpec() != null) {
                    // TODO: Sync frame counter for compass and clock
                    //                    spriteS.swan$spriteSpecular().frameCounter = sprite.frameCounter;

                    spriteS.swan$spriteSpec()
                           .updateAnimation();
                }
            }
        }

        TextureUtil.bindTexture(this.getGlTextureId());
    }
}
