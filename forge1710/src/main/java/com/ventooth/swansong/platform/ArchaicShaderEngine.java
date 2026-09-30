/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.platform;

import com.ventooth.swansong.mixin.extensions.RenderGlobalExt;
import com.ventooth.swansong.shader.ShaderEngine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

public class ArchaicShaderEngine extends ShaderEngine {
    public static ArchaicShaderEngine of(RenderBlocks rb) {
        return ArchaicShaderEngine.global();
    }

    public static ArchaicShaderEngine of(Tessellator tess) {
        return ArchaicShaderEngine.global();
    }

    public static ArchaicShaderEngine of(Entity ent) {
        return ArchaicShaderEngine.global();
    }

    public static ArchaicShaderEngine of(TileEntity te) {
        return ArchaicShaderEngine.global();
    }

    public static ArchaicShaderEngine of(RenderGlobal rg) {
        return ArchaicShaderEngine.global();
    }

    public static ArchaicShaderEngine ofObj(Object o) {
        return ArchaicShaderEngine.global();
    }

    public static ArchaicShaderEngine global() {
        // TODO: Do we want a fancy exception of rg is null?
        //  or if rg is somehow not an instance of RenderGlobalExt?
        //  or if swan$engine() is null somehow?
        //noinspection CastToIncompatibleInterface
        return ((RenderGlobalExt) Minecraft.getMinecraft().renderGlobal).swan$engine();
    }
}
