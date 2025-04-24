/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.loader;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ShaderLoaderInParams {
    final double handDepth;
    final double renderQuality;
    final double shadowQuality;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private double handDepth = 1; //Default
        private double renderQuality = 1; //Default
        private double shadowQuality = 1; //Default

        public Builder handDepth(double value) {
            this.handDepth = value;
            return this;
        }

        public Builder renderQuality(double value) {
            this.renderQuality = value;
            return this;
        }

        public Builder shadowQuality(double value) {
            this.shadowQuality = value;
            return this;
        }

        public ShaderLoaderInParams build() {
            return new ShaderLoaderInParams(handDepth, renderQuality, shadowQuality);
        }
    }
}
