/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */
package com.ventooth.swansong.ornithe.mixin;

import com.ventooth.swansong.ornithe.OrnitheRender;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.block.entity.EndPortalRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndPortalRenderer.class)
public abstract class EndPortalRendererMixin {
    @Shadow
    @Final
    private static Identifier END_PORTAL_LOCATION;

    @Inject(method = "render(Lnet/minecraft/block/entity/EndPortalBlockEntity;DDDFI)V",
            at = @At("HEAD"),
            cancellable = true)
    private void swansong$packPortal(EndPortalBlockEntity entity,
                                     double x,
                                     double y,
                                     double z,
                                     float tickDelta,
                                     int stage,
                                     CallbackInfo ci) {
        if (!OrnitheRender.isManaged()) {
            return;
        }
        ci.cancel();
        if (OrnitheRender.isShadowPass()) {
            return;
        }
        GlStateManager.disableLighting();
        Minecraft.getInstance()
                 .getTextureManager()
                 .bind(END_PORTAL_LOCATION);
        var tesselator = Tesselator.getInstance();
        var buffer = tesselator.getBuffer();
        buffer.begin(7, DefaultVertexFormat.BLOCK);
        float time = (Minecraft.getTime() % 700000L) / 700000F;
        buffer.vertex(x, y + 0.75, z)
              .color(0.075F, 0.15F, 0.2F, 1F)
              .texture(time, time)
              .texture(240, 240)
              .nextVertex();
        buffer.vertex(x, y + 0.75, z + 1)
              .color(0.075F, 0.15F, 0.2F, 1F)
              .texture(time, time + 0.2F)
              .texture(240, 240)
              .nextVertex();
        buffer.vertex(x + 1, y + 0.75, z + 1)
              .color(0.075F, 0.15F, 0.2F, 1F)
              .texture(time + 0.2F, time + 0.2F)
              .texture(240, 240)
              .nextVertex();
        buffer.vertex(x + 1, y + 0.75, z)
              .color(0.075F, 0.15F, 0.2F, 1F)
              .texture(time + 0.2F, time)
              .texture(240, 240)
              .nextVertex();
        tesselator.end();
        GlStateManager.enableLighting();
    }
}
