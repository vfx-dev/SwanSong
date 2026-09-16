/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.platform;

import com.ventooth.swansong.resources.pack.ShaderPack;
import com.ventooth.swansong.shader.loader.config.PackLocalizer;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;

import java.util.ArrayList;
import java.util.List;

public final class MinecraftPackLocalizer implements PackLocalizer {
    private final Locale packLocale;

    public MinecraftPackLocalizer(ShaderPack pack) {
        packLocale = new Locale();
        val current = Minecraft.getMinecraft()
                               .getLanguageManager()
                               .getCurrentLanguage();
        val langs = new ArrayList<String>();
        langs.add("en_US");
        if (!"en_US".equals(current.getLanguageCode())) {
            langs.add(current.getLanguageCode());
        }
        packLocale.loadLocaleDataFiles(new ShaderpackResourceManagerAdapter(pack), langs);
    }

    @Override
    public String packText(String key, Object... args) {
        return packLocale.formatMessage(key, args);
    }

    @Override
    public String modText(String key, Object... args) {
        return I18n.format(key, args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> wrapToWidth(String line, int maxWidthPx) {
        return Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(line, maxWidthPx);
    }
}
