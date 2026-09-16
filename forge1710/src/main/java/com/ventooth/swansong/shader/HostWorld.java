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

public interface HostWorld {
    double cameraX();

    double cameraY();

    double cameraZ();

    double celestialAngle();

    double farPlane();

    boolean isGuiHidden();

    double screenBrightness();

    int displayWidth();

    int displayHeight();

    HostWorld NONE = new HostWorld() {
        @Override
        public double cameraX() {
            return 0;
        }

        @Override
        public double cameraY() {
            return 0;
        }

        @Override
        public double cameraZ() {
            return 0;
        }

        @Override
        public double celestialAngle() {
            return 0;
        }

        @Override
        public double farPlane() {
            return 0;
        }

        @Override
        public boolean isGuiHidden() {
            return false;
        }

        @Override
        public double screenBrightness() {
            return 0;
        }

        @Override
        public int displayWidth() {
            return 0;
        }

        @Override
        public int displayHeight() {
            return 0;
        }
    };
}
