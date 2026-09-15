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
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.KHRDebug;

import java.text.MessageFormat;

@RequiredArgsConstructor
public enum DebugMarker {
    GENERIC(100),
    FRAMEBUFFER_BLIT(200),
    FRAMEBUFFER_BIND(210),
    FRAMEBUFFER_BIND_DRAW(211),
    FRAMEBUFFER_BIND_READ(212),
    TEXTURE_COLOR_BLIT(500),
    TEXTURE_DEPTH_BLIT(510),
    TEXTURE_MIP_GEN(520),
    SCREENSHOT_TAKEN(530),

    ;

    private static final Logger log = LogManager.getLogger("GLDebug|Marker");
    private static boolean withTraceLog = false;

    private final int id;

    public static boolean isEnabled() {
        return DebugConfig.GLDebugMarkers;
    }

    public void insert() {
        if (!isEnabled()) {
            return;
        }

        doInsertMessage(name());
    }

    public void insert(String message) {
        if (!isEnabled()) {
            return;
        }

        doInsertMessage(name() + ": " + message);
    }

    public void insertFormat(String format, Object... args) {
        if (!isEnabled()) {
            return;
        }

        doInsertMessage(name() + ": " + MessageFormat.format(format, args));
    }

    private void doInsertMessage(String message) {
        if (withTraceLog) {
            log.trace(message);
        }
        KHRDebug.glDebugMessageInsert(KHRDebug.GL_DEBUG_SOURCE_APPLICATION,
                                      KHRDebug.GL_DEBUG_TYPE_MARKER,
                                      id,
                                      KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION,
                                      message);
    }
}
