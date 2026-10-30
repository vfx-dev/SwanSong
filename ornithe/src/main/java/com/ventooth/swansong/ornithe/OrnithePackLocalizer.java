/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.ornithe;

import com.ventooth.swansong.resources.pack.ShaderPack;
import com.ventooth.swansong.shader.loader.config.LangPackLocalizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resource.language.I18n;

import java.util.List;

public final class OrnithePackLocalizer extends LangPackLocalizer {
    public OrnithePackLocalizer(ShaderPack pack) {
        super(pack,
              Minecraft.getInstance()
                       .getLanguageManager()
                       .getLanguage()
                       .getCode());
    }

    @Override
    public String modText(String key, Object... args) {
        return I18n.translate(key, args);
    }

    @Override
    public List<String> wrapToWidth(String line, int maxWidthPx) {
        return Minecraft.getInstance().textRenderer.split(line, maxWidthPx);
    }
}
