/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.screenshot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ventooth.swansong.image.ThreadedScreenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.IChatComponent;

import java.io.File;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @WrapOperation(method = "func_152348_aa",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/util/ScreenShotHelper;saveScreenshot(Ljava/io/File;IILnet/minecraft/client/shader/Framebuffer;)Lnet/minecraft/util/IChatComponent;"),
                   require = 1)
    private IChatComponent captureScreenshot(File gameDirectory,
                                             int requestedWidthInPixels,
                                             int requestedHeightInPixels,
                                             Framebuffer frameBuffer,
                                             Operation<IChatComponent> original) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            return ThreadedScreenshot.captureScreenshot(gameDirectory, frameBuffer);
        } else {
            return original.call(gameDirectory, requestedHeightInPixels, requestedHeightInPixels, frameBuffer);
        }
    }
}
