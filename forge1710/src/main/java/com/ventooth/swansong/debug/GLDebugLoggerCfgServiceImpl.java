/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.debug;

import com.ventooth.swansong.config.DebugConfig;
import com.ventooth.swansong.config.ModuleConfig;
import com.ventooth.venterceptor.api.service.GLDebugLoggerCfgService;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public final class GLDebugLoggerCfgServiceImpl implements GLDebugLoggerCfgService {
    private static final boolean ENABLED = ModuleConfig.Debug && DebugConfig.ApplyVenterceptorLogFilters;

    public static boolean isEnabled() {
        return ENABLED;
    }

    @Override
    public void apply(@NotNull CfgConsumer cfgConsumer) {
        if (!isEnabled()) {
            return;
        }

        // We're basically just gonna filter out some annoying Nvidia messages

        // Texture state usage warning: The texture object (0) bound to texture image unit 0
        // does not have a defined base level and cannot be used for texture mapping.
        cfgConsumer.filterMessages("API:OTHER", 0x00020084);

        // Framebuffer detailed info: The driver allocated storage for renderbuffer 1.
        cfgConsumer.filterMessages("API:OTHER", 0x00020061);

        // Buffer detailed info: Buffer object 2 (bound to GL_PIXEL_PACK_BUFFER_ARB, usage hint is GL_STREAM_READ)
        // has been mapped in DMA CACHED memory.
        cfgConsumer.filterMessages("API:OTHER", 0x00020071);

        // Program/shader state performance warning: Vertex shader in program 143 is being recompiled based on GL state.
        cfgConsumer.filterMessages("API:PERFORMANCE", 0x00020092);

        // Pixel-path performance warning: Pixel transfer is synchronized with 3D rendering.
        cfgConsumer.filterMessages("API:PERFORMANCE", 0x00020052);
    }
}
