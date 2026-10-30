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

import com.ventooth.swansong.resources.pack.DimensionInfo;
import com.ventooth.swansong.resources.pack.ShaderPack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

class LangPackLocalizerTest {
    @Test
    void currentLanguageOverridesEnglish() {
        var pack = new MemoryPack(Map.of("/shaders/lang/en_US.lang", "a=Hello %d\nb=English\n#c=comment\nnoEquals\n",
                                         "/shaders/lang/de_DE.lang", "b=Deutsch\n"));
        var localizer = new TestLocalizer(pack, "de_DE");

        Assertions.assertEquals("Hello 5", localizer.packText("a", 5));
        Assertions.assertEquals("Deutsch", localizer.packText("b"));
        Assertions.assertEquals("#c", localizer.packText("#c"));
        Assertions.assertEquals("missing", localizer.packText("missing"));
    }

    private static final class TestLocalizer extends LangPackLocalizer {
        TestLocalizer(ShaderPack pack, String languageCode) {
            super(pack, languageCode);
        }

        @Override
        public String modText(String key, Object... args) {
            return key;
        }

        @Override
        public List<String> wrapToWidth(String line, int maxWidthPx) {
            return List.of(line);
        }
    }

    private static final class MemoryPack extends ShaderPack {
        private final Map<String, String> files;

        MemoryPack(Map<String, String> files) {
            super("memory");
            this.files = files;
        }

        @Override
        public InputStream get(String path) throws FileNotFoundException {
            var file = files.get(path);
            if (file == null) {
                throw new FileNotFoundException(path);
            }
            return new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public boolean has(String path) {
            return files.containsKey(path);
        }

        @Override
        public String getWorldSpecialization(DimensionInfo dimension) {
            return null;
        }
    }
}
