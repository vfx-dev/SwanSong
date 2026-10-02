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

import com.ventooth.swansong.shader.ShaderStateOld;
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
            .addInt("fogMode", ShaderStateOld::fogMode)
            .addVec3("fogColor", ShaderStateOld::fogColor)
            .addVec3("skyColor", ShaderStateOld::skyColor)
            .addVec4("entityColor", ShaderStateOld::entityColor)
            .addInt("entityId", ShaderStateOld::entityId)
            .addInt("blockEntityId", ShaderStateOld::blockEntityId)
            .addVec3("sunPosition", ShaderStateOld::sunPosition)
            .addVec3("moonPosition", ShaderStateOld::moonPosition)
            .addVec3("shadowLightPosition", ShaderStateOld::shadowLightPosition)
            .addVec3("upPosition", ShaderStateOld::upPos)
            .addMat4("gbufferProjection", ShaderStateOld::projectionMat)
            .addMat4("gbufferModelViewInverse", ShaderStateOld::modelViewMatInv)
            .addMat4("gbufferPreviousProjection", ShaderStateOld::prevProjectionMat)
            .addMat4("gbufferModelView", ShaderStateOld::modelViewMat)
            .addMat4("gbufferProjectionInverse", ShaderStateOld::projectionMatInv)
            .addMat4("gbufferPreviousModelView", ShaderStateOld::prevModelViewMat)
            .addMat4("shadowProjection", ShaderStateOld::shadowProjection)
            .addMat4("shadowProjectionInverse", ShaderStateOld::shadowProjectionInverse)
            .addMat4("shadowModelView", ShaderStateOld::shadowModelView)
            .addMat4("shadowModelViewInverse", ShaderStateOld::shadowModelViewInverse)
            .addInt("renderStage", ShaderStateOld::renderStage)
            .addVec2i("atlasSize", ShaderStateOld::atlasSize)
            .build();

    private final static List<Uniform<?>> GENERAL_UNIFORMS = new GeneralUniformListBuilder(LIVE_UNIFORMS)
            .addInt("heldItemId", ShaderStateOld::heldItemId)
            .addInt("heldBlockLightValue", ShaderStateOld::heldBlockLightValue)
            .addInt("worldTime", ShaderStateOld::worldTime)
            .addInt("worldDay", ShaderStateOld::worldDay)
            .addInt("moonPhase", ShaderStateOld::moonPhase)
            .addInt("frameCounter", ShaderStateOld::frameCounter)
            .addFloat("frameTime", ShaderStateOld::frameTime)
            .addFloat("frameTimeCounter", ShaderStateOld::frameTimeCounter)
            .addFloat("sunAngle", ShaderStateOld::sunAngle)
            .addFloat("shadowAngle", ShaderStateOld::shadowAngle)
            .addFloat("rainStrength", ShaderStateOld::rainStrength)
            .addFloat("aspectRatio", ShaderStateOld::aspectRatio)
            .addFloat("viewWidth", ShaderStateOld::viewWidth)
            .addFloat("viewHeight", ShaderStateOld::viewHeight)
            .addFloat("near", ShaderStateOld::nearPlane)
            .addFloat("far", ShaderStateOld::farPlane)
            .addVec3("previousCameraPosition", ShaderStateOld::prevCamPos)
            .addVec3("cameraPosition", ShaderStateOld::camPos)
            .addFloat("wetness", ShaderStateOld::wetness)
            .addFloat("eyeAltitude", ShaderStateOld::eyeAltitude)
            .addVec2i("eyeBrightness", ShaderStateOld::eyeBrightness)
            .addVec2i("eyeBrightnessSmooth", ShaderStateOld::eyeBrightnessSmooth)
            .addVec2i("terrainTextureSize", UniformGetterDanglingWires::terrainTextureSize)
            .addInt("terrainIconSize", UniformGetterDanglingWires::terrainIconSize)
            .addInt("isEyeInWater", ShaderStateOld::isEyeInWater)
            .addFloat("nightVision", ShaderStateOld::nightVision)
            .addFloat("blindness", ShaderStateOld::blindness)
            .addFloat("screenBrightness", ShaderStateOld::screenBrightness)
            .addBool("hideGUI", ShaderStateOld::isGuiHidden)
            .addFloat("centerDepthSmooth", ShaderStateOld::centerDepthSmooth)
            .build();
    //@formatter:on

    static {
        ShaderStateOld.setUniformUpdateTask(() -> LIVE_UNIFORMS.forEach(Uniform::update));
    }

    private final static UniformFunctionRegistry UNIFORM_FUNCTION_REGISTRY;

    static {
        try {
            val clazz = ShaderStateOld.class;
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
