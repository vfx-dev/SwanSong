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

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record Localization(String name,
                           ObjectList<String> description,
                           Object2ObjectMap<String, String> options) {

    public static String localize(PackLocalizer locale, String key) {
        return locale.packText(key);
    }

    public static Localization create(PackLocalizer locale,
                                      boolean isScreen,
                                      String nameKey,
                                      @Nullable String description,
                                      ObjectList<String> optionsUnlocalized) {
        return createRaw(locale,
                         nameKey,
                         (isScreen ? "screen." : "option.") + nameKey,
                         "value." + nameKey + ".",
                         description,
                         optionsUnlocalized);
    }

    public static Localization createProfile(PackLocalizer locale, ObjectList<String> options) {
        return createRaw(locale,
                         locale.modText("gui.swansong.shaders.profile.key"),
                         "profile",
                         "profile.",
                         null,
                         options);
    }

    public static Localization createScreen(PackLocalizer locale, @Nullable String screenName) {
        return createRaw(locale,
                         screenName == null ? locale.modText("gui.swansong.shaders.root.title") : screenName,
                         screenName == null ? "screen" : "screen." + screenName,
                         null,
                         null,
                         null);
    }

    private static final int maxDescLineWidthPx = 250;

    private static void subdivideLine(PackLocalizer locale, String line, ObjectList<String> output) {
        output.addAll(locale.wrapToWidth("- " + line, maxDescLineWidthPx));
    }

    private static Localization createRaw(PackLocalizer locale,
                                          String name,
                                          String nameBase,
                                          @Nullable String optionBase,
                                          @Nullable String description,
                                          @Nullable ObjectList<String> optionsUnlocalized) {
        String localizedName;
        {
            localizedName = localize(locale, nameBase);
            if (nameBase.equals(localizedName)) {
                localizedName = name == null ? nameBase : name;
            }
        }
        String localizedComment;
        {
            val key = nameBase + ".comment";
            localizedComment = localize(locale, key);
            if (key.equals(localizedComment)) {
                localizedComment = description;
            }
        }
        ObjectList<String> commentLines;
        if (localizedComment == null) {
            commentLines = ObjectLists.emptyList();
        } else {
            val lines = new ObjectArrayList<>(localizedComment.split("\\. "));
            val finalLines = new ObjectArrayList<String>();
            for (val line : lines) {
                subdivideLine(locale, line, finalLines);
            }
            commentLines = ObjectLists.unmodifiable(finalLines);
        }
        if (optionBase == null || optionsUnlocalized == null) {
            return new Localization(localizedName, commentLines, Object2ObjectMaps.emptyMap());
        }
        final Object2ObjectMap<String, String> options;
        if (optionsUnlocalized.isEmpty()) {
            options = Object2ObjectMaps.emptyMap();
        } else {
            val theOptions = new Object2ObjectOpenHashMap<String, String>();
            for (val option : optionsUnlocalized) {
                val optionKey = optionBase + option;
                val optionLoc = localize(locale, optionKey);
                theOptions.put(option, localize(locale, Objects.equals(optionKey, optionLoc) ? option : optionLoc));
            }
            options = Object2ObjectMaps.unmodifiable(theOptions);
        }
        return new Localization(localizedName, commentLines, options);
    }

}
