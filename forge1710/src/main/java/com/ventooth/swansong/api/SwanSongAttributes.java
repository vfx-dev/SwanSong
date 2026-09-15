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
public final class SwanSongAttributes {
    @ApiStatus.Experimental
    public static final class Instanced {
        // @formatter:off
        public static final int staticPosition      = 0;
        public static final int staticTexture       = 1;
        public static final int staticColor         = 2;
        public static final int staticEntityData    = 3;
        public static final int staticNormal        = 4;
        public static final int staticTangent       = 5;
        public static final int staticMidTexture    = 6;
        public static final int staticEdgeTexture   = 7;

        public static final int dynamicModelMat     = 8;
        public static final int dynamicModelMat0    = dynamicModelMat;
        public static final int dynamicModelMat1    = dynamicModelMat + 1;
        public static final int dynamicModelMat2    = dynamicModelMat + 2;
        public static final int dynamicModelMat3    = dynamicModelMat + 3;

        public static final int dynamicBrightnessR  = 12;
        public static final int dynamicBrightnessG  = 13;
        public static final int dynamicBrightnessB  = 14;
        // @formatter:on
    }
}
