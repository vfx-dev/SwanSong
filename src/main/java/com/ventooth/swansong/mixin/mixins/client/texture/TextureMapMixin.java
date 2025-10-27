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
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.ventooth.swansong.mixin.interfaces.PBRAtlas;
import com.ventooth.swansong.mixin.interfaces.PBRTextureHolder;
import com.ventooth.swansong.mixin.interfaces.ShadersTextureAtlasSprite;
import com.ventooth.swansong.pbr.PBRTextureEngine;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
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
import net.minecraft.client.resources.IResourceManager;
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

    @Inject(method = "loadTextureAtlas",
            at = @At("HEAD"),
            require = 1)
    private void preLoad(IResourceManager resourceManager, CallbackInfo ci) {
        PBRTextureEngine.currentlyLoadingAtlas = swan$mapLoc;
    }

    @Inject(method = "loadTextureAtlas",
            at = @At("RETURN"),
            require = 1)
    private void postLoad(IResourceManager resourceManager, CallbackInfo ci) {
        PBRTextureEngine.currentlyLoadingAtlas = null;
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

    @Inject(method = "updateAnimations",
            at = @At("HEAD"),
            require = 1)
    private void preUpdateAnim(CallbackInfo ci, @Share("updateNorm") LocalBooleanRef updateNorm, @Share("updateSpec") LocalBooleanRef updateSpec) {
        updateNorm.set(false);
        updateSpec.set(false);
    }

    @Inject(method = "updateAnimations",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;updateAnimation()V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void onUpdateAnim(CallbackInfo ci, @Local TextureAtlasSprite sprite, @Share("updateNorm")LocalBooleanRef updateNorm, @Share("updateSpec")LocalBooleanRef updateSpec) {
        val spriteS = (ShadersTextureAtlasSprite) sprite;
        if (spriteS.swan$spriteNorm() != null) {
            updateNorm.set(true);
        }

        if (spriteS.swan$spriteSpec() != null) {
            updateSpec.set(true);
        }
    }

    @Inject(method = "updateAnimations",
            at = @At("RETURN"),
            require = 1)
    private void postUpdateAnim(CallbackInfo ci, @Share("updateNorm")LocalBooleanRef updateNorm, @Share("updateSpec")LocalBooleanRef updateSpec) {
        PBRTextureEngine.updateAnimations((TextureMap) (Object)this, listAnimatedSprites, updateNorm.get(), updateSpec.get());
    }
}
