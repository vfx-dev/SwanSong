/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.mc.FixReadImageException;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.ventooth.swansong.Share;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

@Mixin(TextureUtil.class)
public abstract class TextureUtilMixin {
    @WrapMethod(method = "readImageData",
                require = 1)
    private static int[] fix_WrapWithIOEOnly(IResourceManager resourceManager,
                                             ResourceLocation imageLocation,
                                             Operation<int[]> original) throws IOException {
        try {
            return original.call(resourceManager, imageLocation);
        } catch (Exception e) {
            //noinspection ConstantValue
            if (e instanceof IOException) {
                throw e;
            } else {
                Share.log.debug("Wrapped as IOE: ", e);
                throw new IOException(e);
            }
        }
    }
}