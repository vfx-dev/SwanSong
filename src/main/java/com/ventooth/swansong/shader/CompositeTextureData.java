/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader;

import lombok.Getter;

public enum CompositeTextureData {
    // @formatter:off
    colortex0   ( 0, "colortex0"   , "gcolor", "texture"),
    colortex1   ( 1, "colortex1"   , "gdepth"),
    colortex2   ( 2, "colortex2"   , "gnormal"),
    colortex3   ( 3, "colortex3"   , "composite"),
    colortex4   ( 7, "colortex4"   , "gaux1"),
    colortex5   ( 8, "colortex5"   , "gaux2"),
    colortex6   ( 9, "colortex6"   , "gaux3"),
    colortex7   (10, "colortex7"   , "gaux4"),
    colortex8   (16, "colortex8"   ),
    colortex9   (17, "colortex9"   ),
    colortex10  (18, "colortex10"  ),
    colortex11  (19, "colortex11"  ),
    colortex12  (20, "colortex12"  ),
    colortex13  (21, "colortex13"  ),
    colortex14  (22, "colortex14"  ),
    colortex15  (23, "colortex15"  ),
    shadowtex0  ( 4, "shadowtex0"  , "shadow"),
    shadowtex1  ( 5, "shadowtex1"  , "watershadow"),
    depthtex0   ( 6, "depthtex0"   , "gdepthtex"),
    depthtex1   (11, "depthtex1"   ),
    depthtex2   (12, "depthtex2"   ),
    shadowcolor0(13, "shadowcolor0", "shadowcolor"),
    shadowcolor1(14, "shadowcolor1"),
    noisetex    (15, "noisetex"),
    blitsrc     (30, "blitsrc"),
    // @formatter:on
    ;

    @Getter
    private final int gpuIndex;
    @Getter
    private final String[] names;

    CompositeTextureData(int gpuIndex, String... names) {
        this.gpuIndex = gpuIndex;
        this.names = names;
    }
}
