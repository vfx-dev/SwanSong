/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.texbuf;

import lombok.RequiredArgsConstructor;
import org.lwjgl.opengl.GL11;

@RequiredArgsConstructor
public class BufferConfig {
    public final String name;
    public final boolean clear;
    public final int format;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public String name;
        public boolean clear = true;
        public int format = GL11.GL_RGBA8;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder clear(boolean clear) {
            this.clear = clear;
            return this;
        }

        public Builder format(int format) {
            this.format = format;
            return this;
        }

        public BufferConfig build() {
            return new BufferConfig(name, clear, format);
        }
    }
}
