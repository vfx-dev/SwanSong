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

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;

import java.util.Objects;

//TODO convert to record
public final class Localization {
    private final String name;
    private final ObjectList<String> description;
    private final Object2ObjectMap<String, String> options;

    public Localization(String name, ObjectList<String> description, Object2ObjectMap<String, String> options) {
        this.name = name;
        this.description = description;
        this.options = options;
    }

    public static String localize(Locale locale, String key) {
        return locale.formatMessage(key, new Object[0]);
    }

    public static Localization create(Locale locale,
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

    public static Localization createProfile(Locale locale, ObjectList<String> options) {
        return createRaw(locale, I18n.format("gui.swansong.shaders.profile.key"), "profile", "profile.", null, options);
    }

    public static Localization createScreen(Locale locale, @Nullable String screenName) {
        return createRaw(locale,
                         screenName == null ? I18n.format("gui.swansong.shaders.root.title") : screenName,
                         screenName == null ? "screen" : "screen." + screenName,
                         null,
                         null,
                         null);
    }

    private static final int maxDescLineWidthPx = 250;

    private static void subdivideLine(String line, ObjectList<String> output) {
        val fr = Minecraft.getMinecraft().fontRenderer;
        output.addAll(fr.listFormattedStringToWidth("- " + line, maxDescLineWidthPx));
    }

    private static Localization createRaw(Locale locale,
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
                subdivideLine(line, finalLines);
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

    public String name() {
        return name;
    }

    public ObjectList<String> description() {
        return description;
    }

    public Object2ObjectMap<String, String> options() {
        return options;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (Localization) obj;
        return Objects.equals(this.name, that.name) &&
               Objects.equals(this.description, that.description) &&
               Objects.equals(this.options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, options);
    }

    @Override
    public String toString() {
        return "Localization[" +
               "name=" +
               name +
               ", " +
               "description=" +
               description +
               ", " +
               "options=" +
               options +
               ']';
    }

}
