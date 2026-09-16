/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.gl;

import com.ventooth.swansong.EnvInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GLEnvProbe {
    public static EnvInfo probe(String mcVersion, String swansongVersion) {
        return new EnvInfo(mcVersion,
                           swansongVersion,
                           System.getProperty("os.name"),
                           GL11.glGetString(GL11.GL_VENDOR),
                           GL11.glGetString(GL11.GL_RENDERER),
                           GL11.glGetString(GL11.GL_VERSION),
                           splitExtensions(GL11.glGetString(GL11.GL_EXTENSIONS)),
                           GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION),
                           GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE),
                           GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS));
    }

    private static List<String> splitExtensions(String extensions) {
        if (extensions == null) {
            return Collections.emptyList();
        }
        val trimmed = extensions.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(trimmed.split("\\s+"));
    }
}
