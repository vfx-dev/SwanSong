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

import com.ventooth.swansong.mixin.interfaces.ShaderRenderGlobal;
import com.ventooth.swansong.mixin.interfaces.ShaderRenderGlobalHolder;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;

@Mixin(Render.class)
public abstract class RenderMixin implements ShaderRenderGlobalHolder {
    @Unique
    private ShaderRenderGlobal swan$shaderRenderGlobal;

    @Override
    public ShaderRenderGlobal swan$shaderRenderGlobal() {
        if (swan$shaderRenderGlobal == null) {
            val rg = Minecraft.getMinecraft().renderGlobal;
            if (rg == null) {
                throw new IllegalStateException("ShaderRenderGlobal not initialized!");
            }
            //noinspection CastToIncompatibleInterface
            swan$shaderRenderGlobal = (ShaderRenderGlobal) rg;
        }
        return swan$shaderRenderGlobal;
    }
}
