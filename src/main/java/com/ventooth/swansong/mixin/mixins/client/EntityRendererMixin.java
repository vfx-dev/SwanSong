/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client;

import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.shader.ShaderEngine;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.shader.Shader;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Redirect(method = "renderWorld",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/culling/Frustrum;setPosition(DDD)V"),
              require = 1)
    private void grabFrustrum(Frustrum instance, double x, double y, double z) {
        if (ShaderEngine.isInitialized()) {
            ShaderEngine.mcFrustrum = instance;
        }
        instance.setPosition(x, y, z);
    }

    // TODO: [STATE_MANAGEMENT] Move to hooks
    @Redirect(method = "renderWorld",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glViewport(IIII)V",
                       remap = false),
              require = 1)
    private void resizeViewport(int x, int y, int width, int height) {
        if (ShaderEngine.isInitialized()) {
            val q = ShadersConfig.RenderQuality.get();
            GL11.glViewport(x, y, (int) (width * q), (int) (height * q));
        } else {
            GL11.glViewport(x, y, width, height);
        }
    }
}
