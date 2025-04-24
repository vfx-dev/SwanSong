/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.mc.SpriteTranslucencyFix;

import com.ventooth.swansong.mixin.interfaces.ShadersTextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.List;

// TODO: Beddium has a "better" version of this inside BetterMipmapsModule?
@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin {
    @Shadow
    protected List<int[][]> framesTextureData;

    @Final
    @Shadow
    private String iconName;

    @Inject(method = "setFramesTextureData",
            at = @At("RETURN"),
            require = 1)
    private void fixTransparency(List<int[][]> newFramesTextureData, CallbackInfo ci) {
        if (swan$isBaseSprite(this)) {
            for (int i = 0; i < this.framesTextureData.size(); ++i) {
                int[][] datas = this.framesTextureData.get(i);
                if (datas != null && !this.iconName.startsWith("leaves_")) {
                    for (int di = 0; di < datas.length; ++di) {
                        int[] data = datas[di];
                        if (data != null) {
                            s$fixTransparentColor(data);
                        }
                    }
                }
            }
        }
    }

    @Unique
    private static boolean swan$isBaseSprite(Object thiz) {
        if (thiz instanceof ShadersTextureAtlasSprite sprite) {
            return sprite.swan$isBaseSprite();
        }
        return true;
    }

    @Unique
    private void s$fixTransparentColor(int[] data) {
        long redSum = 0L;
        long greenSum = 0L;
        long blueSum = 0L;
        long count = 0L;

        for (int i = 0; i < data.length; ++i) {
            int col = data[i];
            int alpha = col >> 24 & 255;
            if (alpha >= 16) {
                int red = col >> 16 & 255;
                int green = col >> 8 & 255;
                int blue = col & 255;
                redSum += red;
                greenSum += green;
                blueSum += blue;
                ++count;
            }
        }

        if (count > 0L) {
            int redAvg = (int) (redSum / count);
            int greenAvg = (int) (greenSum / count);
            int blueAvg = (int) (blueSum / count);
            int colAvg = redAvg << 16 | greenAvg << 8 | blueAvg;

            for (int i = 0; i < data.length; ++i) {
                int col = data[i];
                int alpha = col >> 24 & 255;
                if (alpha <= 16) {
                    data[i] = colAvg;
                }
            }

        }
    }
}
