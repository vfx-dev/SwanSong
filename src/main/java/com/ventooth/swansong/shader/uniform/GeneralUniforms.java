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
import com.ventooth.swansong.uniforms.UniformFunctionRegistry;
import lombok.AccessLevel;
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


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeneralUniforms {
    //@formatter:off
    private final static List<Uniform<?>> LIVE_UNIFORMS = new GeneralUniformListBuilder()
            .addInt("fogMode", ShaderState::fogMode)
            .addVec3("fogColor", ShaderState::fogColor)
            .addVec3("skyColor", ShaderState::skyColor)
            .addVec4("entityColor", ShaderState::entityColor)
            .addInt("entityId", ShaderState::entityId)
            .addInt("blockEntityId", ShaderState::blockEntityId)
            .addVec3("sunPosition", ShaderState::sunPosition)
            .addVec3("moonPosition", ShaderState::moonPosition)
            .addVec3("shadowLightPosition", ShaderState::shadowLightPosition)
            .addVec3("upPosition", ShaderState::upPos)
            .addMat4("gbufferProjection", ShaderState::projectionMat)
            .addMat4("gbufferModelViewInverse", ShaderState::modelViewMatInv)
            .addMat4("gbufferPreviousProjection", ShaderState::prevProjectionMat)
            .addMat4("gbufferModelView", ShaderState::modelViewMat)
            .addMat4("gbufferProjectionInverse", ShaderState::projectionMatInv)
            .addMat4("gbufferPreviousModelView", ShaderState::prevModelViewMat)
            .addMat4("shadowProjection", ShaderState::shadowProjection)
            .addMat4("shadowProjectionInverse", ShaderState::shadowProjectionInverse)
            .addMat4("shadowModelView", ShaderState::shadowModelView)
            .addMat4("shadowModelViewInverse", ShaderState::shadowModelViewInverse)
            .addBool("swan_portalEyeS", () -> ShaderState.portalEye()[0])
            .addBool("swan_portalEyeT", () -> ShaderState.portalEye()[1])
            .addBool("swan_portalEyeR", () -> ShaderState.portalEye()[2])
            .addBool("swan_portalEyeQ", () -> ShaderState.portalEye()[3])
            .addInt("renderStage", ShaderState::renderStage)
            .build();

    private final static List<Uniform<?>> GENERAL_UNIFORMS = new GeneralUniformListBuilder(LIVE_UNIFORMS)
            .addInt("heldItemId", ShaderState::heldItemId)
            .addInt("heldBlockLightValue", ShaderState::heldBlockLightValue)
            .addInt("worldTime", ShaderState::worldTime)
            .addInt("worldDay", ShaderState::worldDay)
            .addInt("moonPhase", ShaderState::moonPhase)
            .addInt("frameCounter", ShaderState::frameCounter)
            .addFloat("frameTime", ShaderState::frameTime)
            .addFloat("frameTimeCounter", ShaderState::frameTimeCounter)
            .addFloat("sunAngle", ShaderState::sunAngle)
            .addFloat("shadowAngle", ShaderState::shadowAngle)
            .addFloat("rainStrength", ShaderState::rainStrength)
            .addFloat("aspectRatio", ShaderState::aspectRatio)
            .addFloat("viewWidth", ShaderState::viewWidth)
            .addFloat("viewHeight", ShaderState::viewHeight)
            .addFloat("near", ShaderState::nearPlane)
            .addFloat("far", ShaderState::farPlane)
            .addVec3("previousCameraPosition", ShaderState::prevCamPos)
            .addVec3("cameraPosition", ShaderState::camPos)
            .addFloat("wetness", ShaderState::wetness)
            .addFloat("eyeAltitude", ShaderState::eyeAltitude)
            .addVec2i("eyeBrightness", ShaderState::eyeBrightness)
            .addVec2i("eyeBrightnessSmooth", ShaderState::eyeBrightnessSmooth)
            .addVec2i("terrainTextureSize", UniformGetterDanglingWires::terrainTextureSize)
            .addInt("terrainIconSize", UniformGetterDanglingWires::terrainIconSize)
            .addInt("isEyeInWater", ShaderState::isEyeInWater)
            .addFloat("nightVision", ShaderState::nightVision)
            .addFloat("blindness", ShaderState::blindness)
            .addFloat("screenBrightness", ShaderState::screenBrightness)
            .addBool("hideGUI", ShaderState::isGuiHidden)
            .addFloat("centerDepthSmooth", ShaderState::centerDepthSmooth)
            .addVec2i("atlasSize", UniformGetterDanglingWires::atlasSize)
            .build();
    //@formatter:on

    static {
        ShaderState.setUniformUpdateTask(() -> LIVE_UNIFORMS.forEach(Uniform::update));
    }

    private final static UniformFunctionRegistry UNIFORM_FUNCTION_REGISTRY;

    static {
        try {
            val clazz = ShaderState.class;
            val reg = new UniformFunctionRegistry.Single();

            reg.impure(clazz.getDeclaredMethod("camPos"), "cameraPosition");
            reg.impure(clazz.getDeclaredMethod("eyeAltitude"));
            //iris stuff
            reg.impure(clazz.getDeclaredMethod("camPosFract"), "cameraPositionFract");
            reg.impure(clazz.getDeclaredMethod("camPosIntD"), "cameraPositionInt");
            reg.impure(clazz.getDeclaredMethod("prevCamPos"), "previousCameraPosition");
            reg.impure(clazz.getDeclaredMethod("prevCamPosFract"), "previousCameraPositionFract");
            reg.impure(clazz.getDeclaredMethod("prevCamPosIntD"), "previousCameraPositionInt");

            reg.impure(clazz.getDeclaredMethod("upPos"), "upPosition");
            reg.impure(clazz.getDeclaredMethod("eyeBrightnessD"), "eyeBrightness");
            reg.impure(clazz.getDeclaredMethod("eyeBrightnessSmoothD"), "eyeBrightnessSmooth");
            reg.impure(clazz.getDeclaredMethod("centerDepthSmooth"));
            reg.impure(clazz.getDeclaredMethod("isEyeInWater"));
            reg.impure(clazz.getDeclaredMethod("blindness"));
            //TODO darknessFactor
            //TODO darknessLightFactor
            reg.impure(clazz.getDeclaredMethod("nightVision"));
            //TODO playerMood
            reg.impure(clazz.getDeclaredMethod("isGuiHidden"), "hideGUI");
            reg.impure(clazz.getDeclaredMethod("viewHeight"));
            reg.impure(clazz.getDeclaredMethod("viewWidth"));
            reg.impure(clazz.getDeclaredMethod("aspectRatio"));
            reg.impure(clazz.getDeclaredMethod("screenBrightness"));
            reg.impure(clazz.getDeclaredMethod("frameCounter"));
            reg.impure(clazz.getDeclaredMethod("frameTime"));
            reg.impure(clazz.getDeclaredMethod("frameTimeCounter"));
            //TODO entityId
            //TODO blockEntityId
            reg.impure(clazz.getDeclaredMethod("heldItemId"));
            reg.impure(clazz.getDeclaredMethod("heldBlockLightValue"));
            reg.impure(clazz.getDeclaredMethod("sunPosition"));
            reg.impure(clazz.getDeclaredMethod("moonPosition"));
            reg.impure(clazz.getDeclaredMethod("shadowLightPosition"));
            reg.impure(clazz.getDeclaredMethod("sunAngle"));
            reg.impure(clazz.getDeclaredMethod("shadowAngle"));
            reg.impure(clazz.getDeclaredMethod("moonPhase"));
            reg.impure(clazz.getDeclaredMethod("rainStrength"));
            reg.impure(clazz.getDeclaredMethod("wetness"));
            reg.impure(clazz.getDeclaredMethod("worldTime"));
            reg.impure(clazz.getDeclaredMethod("worldDay"));
            reg.impure(clazz.getDeclaredMethod("biome"));
            reg.impure(clazz.getDeclaredMethod("nearPlane"), "near");
            reg.impure(clazz.getDeclaredMethod("farPlane"), "far");
            //TODO alphaTestRef
            //TODO atlasSize
            //TODO renderStage
            reg.impure(clazz.getDeclaredMethod("skyColor"));

            UNIFORM_FUNCTION_REGISTRY = reg;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Someone messed up; [BIG_TIME]", e);
        }
    }

    public static UniformFunctionRegistry getFuncRegistry() {
        return UNIFORM_FUNCTION_REGISTRY;
    }

    public static List<Uniform<?>> get() {
        return GENERAL_UNIFORMS;
    }

    public static List<Uniform<?>> getWith(List<Uniform<?>> other) {
        val list = new ArrayList<>(other);
        list.addAll(GENERAL_UNIFORMS);
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
