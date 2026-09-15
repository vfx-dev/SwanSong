/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.rotarycraft;

import Reika.RotaryCraft.Auxiliary.IORenderer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.tileentity.TileEntity;

@Mixin(value = IORenderer.class, remap = false)
public abstract class IORendererMixin {
    @WrapMethod(method = "renderIO",
                require = 1)
    private static void wrap_RenderIO(TileEntity te,
                                      double posX,
                                      double posY,
                                      double posZ,
                                      Operation<Void> original) {
        if (ShaderEngine.graph.isManaged()) {
            // No rendering on the shadow pass
            if (ShaderEngine.graph.isShadowPass()) {
                return;
            }

            ShaderEngine.graph.push(StateGraph.Stack.AABBOutline);
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            original.call(te, posX, posY, posZ);
            GL11.glPopAttrib();
            ShaderEngine.graph.pop(StateGraph.Stack.AABBOutline);
        } else {
            original.call(te, posX, posY, posZ);
        }
    }
}
