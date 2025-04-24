/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.preprocessor.util;

import it.unimi.dsi.fastutil.ints.IntList;
import lombok.val;

public class RenderTargetParser {
    private static final String PREFIX = "/*";
    private static final String DRAWBUFFERS_PREFIX = "DRAWBUFFERS:";
    private static final int DRAWBUFFERS_PREFIX_LEN = DRAWBUFFERS_PREFIX.length();
    private static final String RENDERTARGETS_PREFIX = "RENDERTARGETS:";
    private static final int RENDERTARGETS_PREFIX_LEN = RENDERTARGETS_PREFIX.length();
    private static final String SUFFIX = "*/";

    public static IntList parseRenderTargetList(String line) {
        val prefixIndex = line.indexOf(PREFIX);
        val suffixIndex = line.indexOf(SUFFIX);
        if (prefixIndex < 0 || suffixIndex < prefixIndex + 2) {
            return null;
        }

        int pos = StringUtils.firstNonWhitespace(line, prefixIndex + 2, line.length());
        int[] indices;
        if (line.startsWith(DRAWBUFFERS_PREFIX, pos)) {
            indices = parseDrawBuffers(line.substring(pos + DRAWBUFFERS_PREFIX_LEN, suffixIndex)
                                           .trim());
        } else if (line.startsWith(RENDERTARGETS_PREFIX, pos)) {
            indices = parseRenderTargets(line.substring(pos + RENDERTARGETS_PREFIX_LEN, suffixIndex)
                                             .trim());
        } else {
            indices = null;
        }
        if (indices == null) {
            return null;
        }
        return IntList.of(indices);
    }

    private static int[] parseRenderTargets(String data) {
        val numbers = data.split(",");
        if (numbers.length == 0) {
            return null;
        }
        val indices = new int[numbers.length];
        for (int i = 0, charsLength = numbers.length; i < charsLength; i++) {
            var c = numbers[i];
            try {
                indices[i] = Integer.parseInt(c);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return indices;
    }

    private static int[] parseDrawBuffers(String data) {
        val chars = data.toCharArray();
        if (chars.length == 0) {
            return null;
        }
        val indices = new int[chars.length];
        for (int i = 0, charsLength = chars.length; i < charsLength; i++) {
            var c = chars[i];
            if (c < '0' || c > '9') {
                return null;
            }
            indices[i] = c - '0';
        }
        return indices;
    }
}
