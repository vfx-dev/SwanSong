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

public record WorldSample(int eyeInWater,
                          double nightVision,
                          int blindnessTicks,
                          int rawEyeBrightness,
                          double rainStrength,
                          long worldTime,
                          long totalWorldTime,
                          int moonPhase,
                          double skyR,
                          double skyG,
                          double skyB,
                          int biomeId) {
    public static final WorldSample EMPTY = new WorldSample(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
}
