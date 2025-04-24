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

import com.ventooth.swansong.shader.config.ConfigEntry;
import com.ventooth.swansong.shader.preprocessor.Option;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;

import java.util.Objects;

public class ConfigProfile extends ConfigEntryBase implements ConfigEntry.Profile {
    private final ObjectList<Option> allOptions;
    private final ObjectList<Profile> profiles;
    private int currentProfile = -1;
    private int defaultProfile = -1;
    private int initialProfile = -1;
    private boolean detected = false;

    public ConfigProfile(Locale locale, ObjectList<Option> allOptions, Object2ObjectMap<String, String> profiles) {
        super(Localization.createProfile(locale, new ObjectArrayList<>(profiles.keySet())));
        val profs = new ObjectArrayList<Profile>();
        Object2ObjectMaps.fastForEach(profiles, entry -> {
            val name = entry.getKey();
            val value = entry.getValue();
            val profile = Profile.parse(profs, this.locale, name, value);
            profs.add(profile);
        });
        this.allOptions = allOptions;
        this.profiles = profs;
    }

    @Override
    public void nextValue() {
        ensureDetected();
        val prev = currentProfile;
        val prof = (prev + 1) % profiles.size();
        currentProfile = prof;
        profiles.get(prof)
                .apply(allOptions);
    }

    @Override
    public void prevValue() {
        ensureDetected();
        val s = profiles.size();
        val prev = currentProfile;
        val prof = prev <= 0 ? s - 1 : prev - 1;
        currentProfile = prof;
        profiles.get(prof)
                .apply(allOptions);
    }

    @Override
    public void resetToDefault() {
        detected = false;
    }

    @Override
    public boolean isDefault() {
        ensureDetected();
        return currentProfile == defaultProfile;
    }

    @Override
    public boolean isModified() {
        ensureDetected();
        return currentProfile != initialProfile;
    }

    @Override
    public String valueName() {
        ensureDetected();
        if (currentProfile == -1) {
            return I18n.format("gui.swansong.shaders.profile.custom");
        }
        return profiles.get(currentProfile)
                       .localizedName();
    }

    private void ensureDetected() {
        if (detected) {
            return;
        }
        detected = true;
        defaultProfile = detectProfile(true);
        currentProfile = detectProfile(false);
        initialProfile = currentProfile;
    }

    private int detectProfile(boolean isDefault) {
        int highestDetected = -1;
        for (int i = 0, profilesSize = profiles.size(); i < profilesSize; i++) {
            var profile = profiles.get(i);
            if (profile.matches(allOptions, isDefault)) {
                highestDetected = i;
            }
        }
        return highestDetected;
    }

    //TODO convert to record
    public static final class Profile {
        private final String localizedName;
        private final String name;
        private final @Unmodifiable Object2ObjectMap<String, Option.Value> optionSettings;

        public Profile(String localizedName,
                       String name,
                       @Unmodifiable Object2ObjectMap<String, Option.Value> optionSettings) {
            this.localizedName = localizedName;
            this.name = name;
            this.optionSettings = optionSettings;
        }

        public static Profile parse(ObjectList<Profile> knownProfiles,
                                    Localization locale,
                                    String unlocalizedName,
                                    String unparsedValue) {
            val localizedName = locale.options()
                                      .getOrDefault(unlocalizedName, unlocalizedName);
            val settings = parseConfigValues(knownProfiles, unparsedValue);
            return new Profile(localizedName, unlocalizedName, settings);
        }

        private static @Unmodifiable Object2ObjectMap<String, Option.Value> parseConfigValues(ObjectList<Profile> knownProfiles,
                                                                                              String values) {
            val parts = values.trim()
                              .split("\\s+");
            val result = new Object2ObjectOpenHashMap<String, Option.Value>();
            for (val part : parts) {
                val halves = part.split("=", 2);
                if (halves.length == 1) {
                    if (part.startsWith("profile.")) {
                        val profName = part.substring("profile.".length());
                        for (val prof : knownProfiles) {
                            if (prof.name()
                                    .equals(profName)) {
                                result.putAll(prof.optionSettings());
                                break;
                            }
                        }
                        continue;
                    }
                    if (part.startsWith("!")) {
                        result.put(part.substring(1), Option.Value.Bool.False);
                    } else {
                        result.put(part, Option.Value.Bool.True);
                    }
                } else {
                    val name = halves[0];
                    val value = Option.Value.detect(halves[1]);
                    result.put(name, value);
                }
            }
            return Object2ObjectMaps.unmodifiable(result);
        }

        public boolean matches(ObjectList<Option> allOptions, boolean isDefault) {
            for (val option : allOptions) {
                val expectedValue = optionSettings.get(option.name);
                if (expectedValue == null) {
                    continue;
                }
                val optVal = isDefault ? option.getDefaultValue() : option.getCurrentValue();
                if (!Option.valueMatches(optVal, expectedValue)) {
                    return false;
                }
            }
            return true;
        }

        public void apply(ObjectList<Option> allOptions) {
            for (val option : allOptions) {
                val expectedValue = optionSettings.get(option.name);
                if (expectedValue == null) {
                    continue;
                }
                option.setCurrentValue(expectedValue);
            }
        }

        public String localizedName() {
            return localizedName;
        }

        public String name() {
            return name;
        }

        public @Unmodifiable Object2ObjectMap<String, Option.Value> optionSettings() {
            return optionSettings;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Profile) obj;
            return Objects.equals(this.localizedName, that.localizedName) &&
                   Objects.equals(this.name, that.name) &&
                   Objects.equals(this.optionSettings, that.optionSettings);
        }

        @Override
        public int hashCode() {
            return Objects.hash(localizedName, name, optionSettings);
        }

        @Override
        public String toString() {
            return "Profile[" +
                   "localizedName=" +
                   localizedName +
                   ", " +
                   "name=" +
                   name +
                   ", " +
                   "optionSettings=" +
                   optionSettings +
                   ']';
        }

    }
}
