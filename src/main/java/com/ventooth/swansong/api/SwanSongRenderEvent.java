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

@ApiStatus.Experimental
public abstract class SwanSongRenderEvent extends SwanSongEvent {
    @Override
    public final boolean isCancelable() {
        return false;
    }

    /**
     * Experimental hook for doing instanced rendering.
     * <p>
     * Figure out the draw calls yourself, nerd.
     */
    @ApiStatus.Experimental
    public static final class InstancedEntities extends SwanSongRenderEvent {
        public final Pass pass;

        public InstancedEntities(int pass) {
            this.pass = (pass == 0) ? Pass.OPAQUE : Pass.TRANSLUCENT;
        }
    }

    // TODO: Any way we can make this like, cleaner?
    @ApiStatus.Experimental
    public enum Pass {
        OPAQUE,
        TRANSLUCENT
    }
}
