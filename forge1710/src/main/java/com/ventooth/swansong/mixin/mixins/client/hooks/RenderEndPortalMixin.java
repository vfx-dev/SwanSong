/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.hooks;

import com.ventooth.swansong.api.ShaderStateInfo;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import com.ventooth.swansong.shader.StateGraph;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.RenderEndPortal;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.ResourceLocation;

@Mixin(RenderEndPortal.class)
public abstract class RenderEndPortalMixin {
    @Final
    @Shadow
    private static ResourceLocation field_147526_d;

    /**
     * @author Ven, FalsePattern
     * @reason Shader support
     */
    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityEndPortal;DDDF)V",
            at = @At("HEAD"),
            cancellable = true)
    public void renderTileEntityShaderCompat(TileEntityEndPortal tileEntityEndPortal,
                                             double posX,
                                             double posY,
                                             double posZ,
                                             float subTick,
                                             CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            if (ShaderStateInfo.shadowPassActive()) {
                // Don't render anything
                ci.cancel();
                return;
            }

            if (ShaderEngine.hasPortalShader()) {
                ShaderState.updatePortalEyeState(false, false, false, true);
                // Shader support end portals via a gbuffer shader
                ShaderEngine.graph.push(StateGraph.Stack.Portal);
                return;
            }
            // Shadersmod style compat
            ci.cancel();
            swan$renderPortalShadersMod(posX, posY, posZ);
        }
    }

    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityEndPortal;DDDF)V",
            at = @At("RETURN"))
    private void endPortal(TileEntityEndPortal entity, double x, double y, double z, float tickDelta, CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            if (ShaderEngine.hasPortalShader()) {
                ShaderEngine.graph.pop(StateGraph.Stack.Portal);
            }
        }
    }

    @Unique
    private void swan$renderPortalShadersMod(double posX, double posY, double posZ) {
        GL11.glDisable(GL11.GL_LIGHTING);

        // Binds the `textures/entity/end_portal.png` texture
        Minecraft.getMinecraft()
                 .getTextureManager()
                 .bindTexture(field_147526_d);

        val tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_QUADS);

        // Same tint as OptiFine
        tess.setColorOpaque_F(0.075F, 0.15F, 0.2F);

        // Full bright
        tess.setBrightness(0xF000F0);

        // Same time offset as vanilla
        val time = (float) (Minecraft.getSystemTime() % 700000L) / 700000F;

        tess.addVertexWithUV(posX + 0.0D, posY + 0.75D, posZ + 0.0D, time + 0.0F, time + 0.0F);
        tess.addVertexWithUV(posX + 0.0D, posY + 0.75D, posZ + 1.0D, time + 0.0F, time + 0.2F);
        tess.addVertexWithUV(posX + 1.0D, posY + 0.75D, posZ + 1.0D, time + 0.2F, time + 0.2F);
        tess.addVertexWithUV(posX + 1.0D, posY + 0.75D, posZ + 0.0D, time + 0.2F, time + 0.0F);

        tess.draw();

        GL11.glEnable(GL11.GL_LIGHTING);
    }
}
