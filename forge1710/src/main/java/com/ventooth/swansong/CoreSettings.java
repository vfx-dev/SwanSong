/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreSettings {
    public static boolean glDebugMarkers = false;

    public static boolean glDebugGroups = false;

    public static boolean glObjectLabels = false;

    public static double handDepth = 1;

    public static double renderQuality = 1;

    public static double shadowQuality = 1;

    public static boolean allowDepthOfField = false;
}
