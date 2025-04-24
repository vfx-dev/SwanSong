/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.thaumcraft;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.ventooth.swansong.api.ShaderStateInfo;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import com.ventooth.swansong.shader.StateGraph;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.client.renderers.tile.TileEldritchObeliskRenderer;

import net.minecraft.tileentity.TileEntity;

@Mixin(TileEldritchObeliskRenderer.class)
public abstract class TileEldritchObeliskRendererMixin {
    @Shadow
    private boolean inrange;

    @Inject(method = "renderTileEntityAt",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V",
                     ordinal = 0),
            require = 1)
    private void prePortals(TileEntity te,
                            double x,
                            double y,
                            double z,
                            float f,
                            CallbackInfo ci,
                            @Share("portal_pass") LocalBooleanRef portalPass) {
        portalPass.set(false);
        if (!inrange) {
            return;
        }

        if (ShaderEngine.graph.isManaged()) {
            if (ShaderStateInfo.shadowPassActive()) {
                inrange = false;
                return;
            }

            GL11.glDisable(GL11.GL_LIGHTING);
            if (!ShaderEngine.hasPortalShader()) {
                inrange = false;
            }

            ShaderEngine.graph.push(StateGraph.Stack.Portal);
            ShaderState.pushBlockEntity();
            ShaderState.portal();
            ShaderState.updatePortalEyeState(false, false, false, true);
            portalPass.set(true);
        }
    }

    @Inject(method = "renderTileEntityAt",
            at = @At(value = "INVOKE",
                     target = "Lorg/lwjgl/opengl/GL11;glPopMatrix()V",
                     ordinal = 0),
            require = 1)
    private void postPortals(TileEntity te,
                             double x,
                             double y,
                             double z,
                             float f,
                             CallbackInfo ci,
                             @Share("portal_pass") LocalBooleanRef portalPass) {
        if (portalPass.get()) {
            GL11.glEnable(GL11.GL_LIGHTING);
            ShaderState.nextBlockEntity(te);
            ShaderState.popBlockEntity();
            ShaderEngine.graph.pop(StateGraph.Stack.Portal);
        }
    }
}
