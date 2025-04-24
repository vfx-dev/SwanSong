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

public class StringUtils {

    public static int firstNonWhitespace(String str, int start, int len) {
        for (int i = start; i < len; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    public static int lastNonWhitespace(String str) {
        for (int i = str.length() - 1; i >= 0; i--) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}
