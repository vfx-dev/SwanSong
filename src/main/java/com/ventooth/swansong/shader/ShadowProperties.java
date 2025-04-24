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

import com.ventooth.swansong.shader.loader.ShaderLoaderOutParams;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.val;

@Builder(access = AccessLevel.PRIVATE)
public class ShadowProperties {
    private static final int NUM_SHADOW_PASSES = 2;

    public final int resolution;
    // Null implies an orthographic shadow map
    public final Double fov;
    public final double distance;
    public final double intervalSize;

    private final Pass[] passes;

    public boolean depthHWFilterEnabled(int pass) {
        assertPassValid(pass);
        return passes[pass].depthHWFilterEnabled;
    }

    public boolean depthFilterNearest(int pass) {
        assertPassValid(pass);
        return passes[pass].depthFilterNearest;
    }

    public boolean depthMipmapEnabled(int pass) {
        assertPassValid(pass);
        return passes[pass].depthMipmapEnabled;
    }

    public boolean colorFilterNearest(int pass) {
        assertPassValid(pass);
        return passes[pass].colorFilterNearest;
    }

    public boolean colorMipmapEnabled(int pass) {
        assertPassValid(pass);
        return passes[pass].colorMipmapEnabled;
    }

    private static void assertPassValid(int pass) {
        if (pass < 0 || pass >= NUM_SHADOW_PASSES) {
            throw new IllegalArgumentException("Invalid shadow pass " + pass);
        }
    }

    @Builder
    public static class Pass {
        public final boolean depthHWFilterEnabled;
        public final boolean depthFilterNearest;
        public final boolean depthMipmapEnabled;
        public final boolean colorFilterNearest;
        public final boolean colorMipmapEnabled;
    }

    public static ShadowProperties from(ShaderLoaderOutParams outParams, float quality) {
        val builder = builder();
        builder.resolution((int) (outParams.shadowMapResolution * quality));
        builder.fov(outParams.shadowMapFov);
        builder.distance(outParams.shadowDistance);
        builder.intervalSize(outParams.shadowIntervalSize);

        val pass0 = Pass.builder();
        val pass1 = Pass.builder();
        pass0.depthHWFilterEnabled(outParams.shadowHardwareFiltering0);
        pass1.depthHWFilterEnabled(outParams.shadowHardwareFiltering1);
        pass0.depthFilterNearest(outParams.shadowDepth0Nearest);
        pass1.depthFilterNearest(outParams.shadowDepth1Nearest);
        pass0.depthMipmapEnabled(outParams.shadowDepth0Mipmap);
        pass1.depthMipmapEnabled(outParams.shadowDepth1Mipmap);

        pass0.colorFilterNearest(outParams.shadowColor0Nearest);
        pass1.colorFilterNearest(outParams.shadowColor1Nearest);
        pass0.colorMipmapEnabled(outParams.shadowColor0Mipmap);
        pass1.colorMipmapEnabled(outParams.shadowColor1Mipmap);

        builder.passes(new Pass[]{pass0.build(), pass1.build()});
        return builder.build();
    }
}
