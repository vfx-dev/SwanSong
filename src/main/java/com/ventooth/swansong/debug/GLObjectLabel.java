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
import com.ventooth.swansong.gl.GLFramebuffer;
import com.ventooth.swansong.gl.GLProgram;
import com.ventooth.swansong.gl.GLShader;
import com.ventooth.swansong.gl.GLTexture;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.KHRDebug;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GLObjectLabel {
    private static final boolean ENABLED = ModuleConfig.Debug && DebugConfig.UseGLObjectLabels;

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static void set(GLShader obj, String label) {
        KHRDebug.glObjectLabel(KHRDebug.GL_SHADER, obj.glName, label);
    }

    public static void set(GLProgram obj, String label) {
        KHRDebug.glObjectLabel(KHRDebug.GL_PROGRAM, obj.glName, label);
    }

    public static void set(GLFramebuffer obj, String label) {
        KHRDebug.glObjectLabel(GL30.GL_FRAMEBUFFER, obj.glName, label);
    }

    public static void set(GLTexture obj, String label) {
        KHRDebug.glObjectLabel(GL11.GL_TEXTURE, obj.glName, label);
    }
}
