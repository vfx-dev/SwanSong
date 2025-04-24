/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.config;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Here we define the config entries which may be provided to a user interface to configure the a shader pack.
 */
public /*sealed*/ interface ConfigEntry {
    /**
     * @return Localized name of the underlying option
     *
     * @implSpec Expected to be pre-computed on initialization.
     */
    String optionName();

    /**
     * @return Localized name of the current value
     *
     * @apiNote May be disregarded by {@link Screen} and {@link Toggleable}
     * @implSpec Expected to be pre-computed on initialization.
     */
    String valueName();

    /**
     * @return A potentially empty list of the localized description line-by-line
     *
     * @apiNote Meant to be presented as a tooltip to the user
     */
    @Unmodifiable
    ObjectList<String> description();

    /**
     * A screen which is either the {@link RootScreen root} or a nested sub menu containing options.
     */
    interface Screen extends ConfigEntry {
        /**
         * @return Localized title of this config screen
         *
         * @apiNote May be disregarded if the screen is {@link RootScreen}.
         * @implSpec Expected to be pre-computed on initialization.
         */
        String screenTitle();

        /**
         * All entries present in this particular screen.
         * <p>
         * {@code null} values represent empty entries.
         *
         * @return All entries contained within this screen
         *
         * @apiNote It's expected that the {@link Screen#columnsHint()} is used to determine the number of columns
         * in the screen layout.
         * @implSpec May be empty, no issue with that
         */
        @Unmodifiable
        ObjectList<@Nullable ConfigEntry> entries();

        /**
         * @return Suggestion of how many columns this screen should have
         *
         * @apiNote Treat this value as a suggestion, ensuring stuff is visible when presented
         * @implSpec The default value is expected to be {@code 2}.
         */
        int columnsHint();

        /**
         * Resets the values of the current screen.
         */
        void reset();
    }

    interface RootScreen extends Screen {
        /**
         * Saves the current config to the `*.txt` shader config.
         */
        void save();

        /**
         * Resets all config values to defaults
         */
        void fullReset();

        /**
         * Cancels configuration and reverts all values to the initial values.
         */
        void cancel();

        /**
         * @return {@code true} if any value has been modified compared to the *saved* config.
         */
        boolean isModified();
    }

    /**
     * Usually represents a label which is a config only visually present with no interaction.
     */
    interface Fixed extends ConfigEntry {

    }

    /**
     * Mutable configuration entries may represent the following:
     * <ul>
     *     <li>Profile Buttons</li>
     *     <li>Cycling Buttons</li>
     *     <li>Toggle Buttons</li>
     *     <li>Sliders</li>
     * </ul>
     * <p>
     * These entries internally contain a reference to some relevant option which can be mutated through them.
     *
     * @implSpec
     */
    /*sealed*/ interface Mutable extends ConfigEntry {
        /**
         * Resets this entry to the internal default value.
         */
        void resetToDefault();

        /**
         * @return {@code true} if the current value is not default
         */
        boolean isDefault();

        /**
         * @return {@code true} if the current value has been modified compared to the *saved* config.
         */
        boolean isModified();
    }

    /**
     * Represents cycling buttons
     */
    interface Switchable extends Mutable {
        /**
         * Cycles to the next value, usually on a left-click.
         */
        void nextValue();

        /**
         * Cycles to the next value, usually on a right-click.
         */
        void prevValue();
    }

    /**
     * Represents the profile button
     */
    interface Profile extends Switchable {

    }

    /**
     * Represents toggleable buttons
     */
    interface Toggleable extends Mutable {
        /**
         * @return Updated value after the toggle
         */
        boolean toggle();

        /**
         * @param value New toggle value
         */
        void setValue(boolean value);

        /**
         * @return Current toggle value
         */
        boolean getValue();
    }

    /**
     * Represents slider options.
     *
     * @implSpec The implementation is expected to clamp the values provided.
     * @implNote A slider with only one valid position is a valid slider, weirdly still considered 'mutable'
     * here but the value will always be default.
     */
    interface Draggable extends Mutable {
        /**
         * Usually called when a slider is dragged.
         *
         * @param value New value clamped into the range: (0.0-1.0)
         *
         * @return Updated value in the range: (0.0-1.0)
         */
        float setValue(float value);

        /**
         * @return Current value in the range: (0.0-1.0)
         */
        float getValue();
    }
}
