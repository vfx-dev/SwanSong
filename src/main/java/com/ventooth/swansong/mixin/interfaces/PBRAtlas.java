/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.interfaces;

import net.minecraft.util.ResourceLocation;

public interface PBRAtlas {
    /**
     * Used for setting the location of the texture map when populating
     */
    void swan$mapLoc(ResourceLocation location);
}
