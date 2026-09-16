/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.loader.config;

import java.util.Collections;
import java.util.List;

public interface PackLocalizer {
    String packText(String key, Object... args);

    String modText(String key, Object... args);

    List<String> wrapToWidth(String line, int maxWidthPx);

    PackLocalizer NONE = new PackLocalizer() {
        @Override
        public String packText(String key, Object... args) {
            return key;
        }

        @Override
        public String modText(String key, Object... args) {
            return key;
        }

        @Override
        public List<String> wrapToWidth(String line, int maxWidthPx) {
            return Collections.singletonList(line);
        }
    };
}
