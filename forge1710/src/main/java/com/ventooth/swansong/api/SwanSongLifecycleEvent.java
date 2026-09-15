/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.api;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("1.3.0")
public abstract class SwanSongLifecycleEvent extends SwanSongEvent {
    @Override
    public final boolean isCancelable() {
        return false;
    }

    // @formatter:off
    /**
     * Fires when a ShaderPack reload is triggered.
     */
    @ApiStatus.AvailableSince("1.3.0")
    public static class ShaderPackReload extends SwanSongLifecycleEvent {}

    /**
     * Fires after a ShaderPack has been unloaded.
     */
    @ApiStatus.AvailableSince("1.3.0")
    public static class ShaderPackUnloaded extends SwanSongLifecycleEvent {}

    /**
     * Fires after a ShaderPack has been successfully unloaded.
     */
    @ApiStatus.AvailableSince("1.3.0")
    public static class ShaderPackLoaded extends SwanSongLifecycleEvent {}
    // @formatter:on
}
