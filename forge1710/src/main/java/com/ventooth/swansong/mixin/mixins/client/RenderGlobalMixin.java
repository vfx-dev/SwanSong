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

import com.ventooth.swansong.mixin.extensions.RenderGlobalExt;
import com.ventooth.swansong.platform.ArchaicShaderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.RenderGlobal;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin implements RenderGlobalExt {
    @Unique
    // TODO: Proper shader engine init logic?
    private ArchaicShaderEngine swan$shaderEngine = new ArchaicShaderEngine();
}
