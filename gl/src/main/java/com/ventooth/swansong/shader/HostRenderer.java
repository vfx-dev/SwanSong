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

import com.ventooth.swansong.resources.pack.DimensionInfo;
import org.jetbrains.annotations.Nullable;

public interface HostRenderer {
    boolean isHandVisible();

    double fieldOfView(float partialTick);

    double farPlaneDistance();

    void applyCameraEffects(float partialTick);

    void renderFirstPersonItem(float partialTick);

    double anaglyphOffset();

    int anaglyphField();

    void bindMainFramebuffer(boolean setViewport);

    void resetLightmapCoords();

    void reloadChunkRenderers();

    @Nullable DimensionInfo currentDimension();

    void onLifecycle(Lifecycle stage);

    int mainFramebufferId();

    int mainFramebufferTexture();

    int mainFramebufferWidth();

    int mainFramebufferHeight();

    void onEngineInit();

    void onEngineDeinit();

    double handDepth();

    double renderQuality();

    double shadowQuality();

    boolean allowDepthOfField();

    enum Lifecycle {
        RELOAD_SCHEDULED,
        LOADED,
        UNLOADED
    }

    HostRenderer NONE = new HostRenderer() {
        @Override
        public boolean isHandVisible() {
            return false;
        }

        @Override
        public double fieldOfView(float partialTick) {
            return 70;
        }

        @Override
        public double farPlaneDistance() {
            return 0;
        }

        @Override
        public void applyCameraEffects(float partialTick) {
        }

        @Override
        public void renderFirstPersonItem(float partialTick) {
        }

        @Override
        public double anaglyphOffset() {
            return 0;
        }

        @Override
        public int anaglyphField() {
            return 0;
        }

        @Override
        public void bindMainFramebuffer(boolean setViewport) {
        }

        @Override
        public void resetLightmapCoords() {
        }

        @Override
        public void reloadChunkRenderers() {
        }

        @Override
        public @Nullable DimensionInfo currentDimension() {
            return null;
        }

        @Override
        public void onLifecycle(Lifecycle stage) {
        }

        @Override
        public int mainFramebufferId() {
            return 0;
        }

        @Override
        public int mainFramebufferTexture() {
            return 0;
        }

        @Override
        public int mainFramebufferWidth() {
            return 0;
        }

        @Override
        public int mainFramebufferHeight() {
            return 0;
        }

        @Override
        public void onEngineInit() {
        }

        @Override
        public void onEngineDeinit() {
        }

        @Override
        public double handDepth() {
            return 0.125;
        }

        @Override
        public double renderQuality() {
            return 1;
        }

        @Override
        public double shadowQuality() {
            return 1;
        }

        @Override
        public boolean allowDepthOfField() {
            return false;
        }
    };
}
