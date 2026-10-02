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

import org.joml.Matrix4dc;
import org.joml.Vector3dc;
import org.joml.Vector3ic;

// TODO: Migrate stuff from OldShaderState here
public abstract class ShaderState {
    public void updateAtlasSize(int width, int height) {
        ShaderStateOld.updateAtlasSize(width, height);
    }

    public void preCelestialRotate() {
        ShaderStateOld.preCelestialRotate();
    }

    public void postCelestialRotate() {
        ShaderStateOld.postCelestialRotate();
    }

    public float blockAoLight() {
        return ShaderStateOld.blockAoLight();
    }

    public float blockLightLevel(float old) {
        return ShaderStateOld.blockLightLevel(old);
    }

    public void updateCamera(boolean withUpdate) {
        ShaderStateOld.updateCamera(withUpdate);
    }

    public void updateFogMode(int mode) {
        ShaderStateOld.updateFogMode(mode);
    }

    public void updateFogColor(double r, double g, double b) {
        ShaderStateOld.updateFogColor(r, g, b);
    }

    public void updateSubTick(float subTick) {
        ShaderStateOld.updateSubTick(subTick);
    }

    public void nextEntity(int newEntityId) {
        ShaderStateOld.nextEntity(newEntityId);
    }

    public void nextBlockEntity(int newEntityId) {
        ShaderStateOld.nextBlockEntity(newEntityId);
    }

    public void setHeldItemTranslucent(boolean translucent) {
        ShaderStateOld.setHeldItemTranslucent(translucent);
    }

    public void updateEntityColor(double r, double g, double b, double mixFactor) {
        ShaderStateOld.updateEntityColor(r, g, b, mixFactor);
    }

    public void resetEntityColor() {
        ShaderStateOld.resetEntityColor();
    }

    public float getSubTick() {
        return ShaderStateOld.getSubTick();
    }

    public void updateRenderStage(MCRenderStage stage) {
        ShaderStateOld.updateRenderStage(stage);
    }

    public Vector3dc skyColor() {
        return ShaderStateOld.skyColor();
    }

    public Vector3dc fogColor() {
        return ShaderStateOld.fogColor();
    }

    public void setUpPosition() {
        ShaderStateOld.setUpPosition();
    }

    public Vector3dc camPos() {
        return ShaderStateOld.camPos();
    }

    public Vector3ic camPosInt() {
        return ShaderStateOld.camPosInt();
    }

    public Matrix4dc shadowModelView() {
        return ShaderStateOld.shadowModelView();
    }

    public void setCameraShadow(int size, double dist, Double fov, double intervalStep) {
        ShaderStateOld.setCameraShadow(size, dist, fov, intervalStep);
    }
}
