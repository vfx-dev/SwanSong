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

@FunctionalInterface
// TODO: [VEN_PLS_REVIEW] is this seam enough for other platforms' shadow culling?
public interface ShadowPassRenderer {
    void renderShadowPass(ShadowProperties shadow, DrawBuffers buffers);

    ShadowPassRenderer NONE = (shadow, buffers) -> {};
}
