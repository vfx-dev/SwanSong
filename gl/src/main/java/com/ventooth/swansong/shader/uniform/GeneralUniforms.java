/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.uniform;

import com.ventooth.swansong.shader.ShaderState;
import com.ventooth.swansong.uniforms.UniformFunctionLookup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.joml.Matrix4dc;
import org.joml.Vector2ic;
import org.joml.Vector3dc;
import org.joml.Vector4dc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;


public final class GeneralUniforms {
    private final List<Uniform<?>> live;
    private final List<Uniform<?>> general;
    private final UniformFunctionLookup funcLookup;

    public GeneralUniforms(ShaderState state) {
        //@formatter:off
        live = new GeneralUniformListBuilder()
            .addInt("fogMode", state::fogMode)
            .addVec3("fogColor", state::fogColor)
            .addVec3("skyColor", state::skyColor)
            .addVec4("entityColor", state::entityColor)
            .addInt("entityId", state::entityId)
            .addInt("blockEntityId", state::blockEntityId)
            .addVec3("sunPosition", state::sunPosition)
            .addVec3("moonPosition", state::moonPosition)
            .addVec3("shadowLightPosition", state::shadowLightPosition)
            .addVec3("upPosition", state::upPos)
            .addMat4("gbufferProjection", state::projectionMat)
            .addMat4("gbufferModelViewInverse", state::modelViewMatInv)
            .addMat4("gbufferPreviousProjection", state::prevProjectionMat)
            .addMat4("gbufferModelView", state::modelViewMat)
            .addMat4("gbufferProjectionInverse", state::projectionMatInv)
            .addMat4("gbufferPreviousModelView", state::prevModelViewMat)
            .addMat4("shadowProjection", state::shadowProjection)
            .addMat4("shadowProjectionInverse", state::shadowProjectionInverse)
            .addMat4("shadowModelView", state::shadowModelView)
            .addMat4("shadowModelViewInverse", state::shadowModelViewInverse)
            .addInt("renderStage", state::renderStage)
            .addVec2i("atlasSize", state::atlasSize)
            .build();

        general = new GeneralUniformListBuilder(live)
            .addInt("heldItemId", state::heldItemId)
            .addInt("heldBlockLightValue", state::heldBlockLightValue)
            .addInt("worldTime", state::worldTime)
            .addInt("worldDay", state::worldDay)
            .addInt("moonPhase", state::moonPhase)
            .addInt("frameCounter", state::frameCounter)
            .addFloat("frameTime", state::frameTime)
            .addFloat("frameTimeCounter", state::frameTimeCounter)
            .addFloat("sunAngle", state::sunAngle)
            .addFloat("shadowAngle", state::shadowAngle)
            .addFloat("rainStrength", state::rainStrength)
            .addFloat("aspectRatio", state::aspectRatio)
            .addFloat("viewWidth", state::viewWidth)
            .addFloat("viewHeight", state::viewHeight)
            .addFloat("near", state::nearPlane)
            .addFloat("far", state::farPlane)
            .addVec3("previousCameraPosition", state::prevCamPos)
            .addVec3("cameraPosition", state::camPos)
            .addFloat("wetness", state::wetness)
            .addFloat("eyeAltitude", state::eyeAltitude)
            .addVec2i("eyeBrightness", state::eyeBrightness)
            .addVec2i("eyeBrightnessSmooth", state::eyeBrightnessSmooth)
            .addVec2i("terrainTextureSize", UniformGetterDanglingWires::terrainTextureSize)
            .addInt("terrainIconSize", UniformGetterDanglingWires::terrainIconSize)
            .addInt("isEyeInWater", state::isEyeInWater)
            .addFloat("nightVision", state::nightVision)
            .addFloat("blindness", state::blindness)
            .addFloat("screenBrightness", state::screenBrightness)
            .addBool("hideGUI", state::isGuiHidden)
            .addFloat("centerDepthSmooth", state::centerDepthSmooth)
            .build();
        //@formatter:on

        try {
            val clazz = ShaderState.class;
            val reg = new UniformFunctionLookup.Single();

            reg.impure(state, clazz.getDeclaredMethod("camPos"), "cameraPosition");
            reg.impure(state, clazz.getDeclaredMethod("eyeAltitude"));
            //iris stuff
            reg.impure(state, clazz.getDeclaredMethod("camPosFract"), "cameraPositionFract");
            reg.impure(state, clazz.getDeclaredMethod("camPosIntD"), "cameraPositionInt");
            reg.impure(state, clazz.getDeclaredMethod("prevCamPos"), "previousCameraPosition");
            reg.impure(state, clazz.getDeclaredMethod("prevCamPosFract"), "previousCameraPositionFract");
            reg.impure(state, clazz.getDeclaredMethod("prevCamPosIntD"), "previousCameraPositionInt");

            reg.impure(state, clazz.getDeclaredMethod("upPos"), "upPosition");
            reg.impure(state, clazz.getDeclaredMethod("eyeBrightnessD"), "eyeBrightness");
            reg.impure(state, clazz.getDeclaredMethod("eyeBrightnessSmoothD"), "eyeBrightnessSmooth");
            reg.impure(state, clazz.getDeclaredMethod("centerDepthSmooth"));
            reg.impure(state, clazz.getDeclaredMethod("isEyeInWater"));
            reg.impure(state, clazz.getDeclaredMethod("blindness"));
            //TODO darknessFactor
            //TODO darknessLightFactor
            reg.impure(state, clazz.getDeclaredMethod("nightVision"));
            //TODO playerMood
            reg.impure(state, clazz.getDeclaredMethod("isGuiHidden"), "hideGUI");
            reg.impure(state, clazz.getDeclaredMethod("viewHeight"));
            reg.impure(state, clazz.getDeclaredMethod("viewWidth"));
            reg.impure(state, clazz.getDeclaredMethod("aspectRatio"));
            reg.impure(state, clazz.getDeclaredMethod("screenBrightness"));
            reg.impure(state, clazz.getDeclaredMethod("frameCounter"));
            reg.impure(state, clazz.getDeclaredMethod("frameTime"));
            reg.impure(state, clazz.getDeclaredMethod("frameTimeCounter"));
            //TODO entityId
            //TODO blockEntityId
            reg.impure(state, clazz.getDeclaredMethod("heldItemId"));
            reg.impure(state, clazz.getDeclaredMethod("heldBlockLightValue"));
            reg.impure(state, clazz.getDeclaredMethod("sunPosition"));
            reg.impure(state, clazz.getDeclaredMethod("moonPosition"));
            reg.impure(state, clazz.getDeclaredMethod("shadowLightPosition"));
            reg.impure(state, clazz.getDeclaredMethod("sunAngle"));
            reg.impure(state, clazz.getDeclaredMethod("shadowAngle"));
            reg.impure(state, clazz.getDeclaredMethod("moonPhase"));
            reg.impure(state, clazz.getDeclaredMethod("rainStrength"));
            reg.impure(state, clazz.getDeclaredMethod("wetness"));
            reg.impure(state, clazz.getDeclaredMethod("worldTime"));
            reg.impure(state, clazz.getDeclaredMethod("worldDay"));
            reg.impure(state, clazz.getDeclaredMethod("biome"));
            reg.impure(state, clazz.getDeclaredMethod("nearPlane"), "near");
            reg.impure(state, clazz.getDeclaredMethod("farPlane"), "far");
            //TODO alphaTestRef
            //TODO atlasSize
            //TODO renderStage
            reg.impure(state, clazz.getDeclaredMethod("skyColor"));

            funcLookup = reg;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Someone messed up; [BIG_TIME]", e);
        }
    }

    public void updateLive() {
        live.forEach(Uniform::update);
    }

    public UniformFunctionLookup funcLookup() {
        return funcLookup;
    }

    public List<Uniform<?>> get() {
        return general;
    }

    public List<Uniform<?>> getWith(List<Uniform<?>> other) {
        val list = new ArrayList<>(other);
        list.addAll(general);
        return Collections.unmodifiableList(list);
    }

    @NoArgsConstructor
    private static class GeneralUniformListBuilder {
        final List<Uniform<?>> uniforms = new ArrayList<>();

        GeneralUniformListBuilder(List<Uniform<?>> uniforms) {
            this.uniforms.addAll(uniforms);
        }

        List<Uniform<?>> build() {
            return Collections.unmodifiableList(uniforms);
        }

        GeneralUniformListBuilder addBool(String name, Uniform.BooleanSupplier getter) {
            uniforms.add(new Uniform.OfBoolean(name, getter, Uniform::set));
            return this;
        }

        GeneralUniformListBuilder addInt(String name, Uniform.IntSupplier getter) {
            uniforms.add(new Uniform.OfInt(name, getter, Uniform::set));
            return this;
        }

        GeneralUniformListBuilder addVec2i(String name, Supplier<Vector2ic> getter) {
            uniforms.add(new Uniform.Of<>(name, getter, Uniform::set));
            return this;
        }

        GeneralUniformListBuilder addFloat(String name, Uniform.DoubleSupplier getter) {
            uniforms.add(new Uniform.OfDouble(name, getter, Uniform::set));
            return this;
        }

        GeneralUniformListBuilder addVec3(String name, Supplier<Vector3dc> getter) {
            uniforms.add(new Uniform.Of<>(name, getter, Uniform::set));
            return this;
        }

        GeneralUniformListBuilder addVec4(String name, Supplier<Vector4dc> getter) {
            uniforms.add(new Uniform.Of<>(name, getter, Uniform::set));
            return this;
        }

        GeneralUniformListBuilder addMat4(String name, Supplier<Matrix4dc> getter) {
            uniforms.add(new Uniform.Of<>(name, getter, Uniform::set));
            return this;
        }
    }
}
