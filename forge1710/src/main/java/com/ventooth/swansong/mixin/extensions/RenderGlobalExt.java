/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.extensions;

import com.ventooth.swansong.platform.ArchaicShaderEngine;

/// An extension of [net.minecraft.client.renderer.RenderGlobal] that attaches the [ArchaicShaderEngine] to it.
public interface RenderGlobalExt {
    ArchaicShaderEngine swan$engine();
}
