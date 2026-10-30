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

import com.ventooth.swansong.Share;
import com.ventooth.swansong.resources.pack.ShaderPack;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class LangPackLocalizer implements PackLocalizer {
    private static final Pattern NUMERIC_FORMAT = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");

    private final Map<String, String> translations = new HashMap<>();

    protected LangPackLocalizer(ShaderPack pack, String languageCode) {
        load(pack, "en_US");
        if (!"en_US".equals(languageCode)) {
            load(pack, languageCode);
        }
    }

    private void load(ShaderPack pack, String languageCode) {
        val path = pack.absolutize(null, "lang/" + languageCode + ".lang");
        try {
            if (!pack.has(path)) {
                return;
            }
            try (val reader = new BufferedReader(new InputStreamReader(pack.get(path), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    val split = line.indexOf('=');
                    if (line.isEmpty() || line.charAt(0) == '#' || split < 0) {
                        continue;
                    }
                    translations.put(line.substring(0, split),
                                     NUMERIC_FORMAT.matcher(line.substring(split + 1))
                                                   .replaceAll("%$1s"));
                }
            }
        } catch (IOException e) {
            Share.log.warn("Failed to read shader pack language file {}", path, e);
        }
    }

    @Override
    public String packText(String key, Object... args) {
        val value = translations.getOrDefault(key, key);
        try {
            return String.format(value, args);
        } catch (IllegalFormatException e) {
            return "Format error: " + value;
        }
    }
}
