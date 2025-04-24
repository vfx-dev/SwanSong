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

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.joml.Vector4dc;

import java.util.Objects;

// TODO: BSL also has `dynamicHandLight=true` and `particles.ordering=mixed`, are these needed?
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ShaderLoaderOutParams {
    //region properties

    // TODO: PROP Wire_As_Config Needs_Test_Shader
    public final @Nullable Quality clouds;
    // TODO: PROP Wire_As_Config Needs_Test_Shader
    public final @Nullable Boolean moon;
    // TODO: PROP Wire_As_Config Needs_Test_Shader
    public final @Nullable Boolean sun;
    // TODO: PROP Required_By_BSL
    public final @Nullable Boolean underwaterOverlay;
    // TODO: PROP Required_By_BSL
    public final @Nullable Boolean vignette;
    // TODO: PROP Required_By_BSL
    public final @Nullable Boolean oldHandLight;
    public final @Nullable Boolean oldLighting;

    // TODO: PROP Needs_Test_Shader
    public final boolean shadowTerrain;
    // TODO: PROP Needs_Test_Shader
    public final boolean shadowTranslucent;
    // TODO: PROP Needs_Test_Shader
    public final boolean shadowEntities;
    // TODO: PROP Needs_Test_Shader
    public final boolean shadowBlockEntities;

    // TODO: PROP Needs_Test_Shader
    public final boolean backFaceSolid;
    // TODO: PROP Needs_Test_Shader Depends_On_Beddium
    public final boolean backFaceCutout;
    // TODO: PROP Needs_Test_Shader Depends_On_Beddium
    public final boolean backFaceCutoutMipped;
    // TODO: PROP Needs_Test_Shader
    public final boolean backFaceTranslucent;

    // TODO: PROP Required_By_BSL
    public final boolean frustumCulling;
    // TODO: PROP Required_By_BSL
    public final boolean shadowCulling;
    // TODO: PROP Needs_Test_Shader
    public final boolean rainDepth;
    // TODO: PROP Required_By_BSL
    public final boolean beaconBeamDepth;

    // TODO: PROP Required_By_BSL
    public final @NotNull
    @Unmodifiable ObjectList<StagedTexture> textures;
    // TODO: PROP Required_By_BSL
    public final @Nullable String noiseTexture;
    //endregion

    //region glsl
    // TODO: PROP Needs_Test_Shader
    public final @Nullable Double ambientOcclusionLevel;
    public final double sunPathRotation;
    // TODO: PROP Needs_Test_Shader
    public final double eyeBrightnessHalfLife;
    // TODO: PROP Needs_Test_Shader
    public final double centerDepthHalfLife;
    public final double drynessHalfLife;
    public final double wetnessHalfLife;

    public final @NotNull
    @Unmodifiable ObjectList<String> bufferClearDisabled;
    // TODO: PROP Needs_Test_Shader
    public final @NotNull
    @Unmodifiable Object2ObjectMap<String, Vector4dc> bufferClearColor;
    public final @NotNull
    @Unmodifiable Object2ObjectMap<String, String> bufferFormat;

    public final boolean shadowDepth0Mipmap;
    public final boolean shadowDepth0Nearest;
    public final boolean shadowDepth1Mipmap;
    public final boolean shadowDepth1Nearest;

    public final boolean shadowColor0Mipmap;
    public final boolean shadowColor0Nearest;
    public final boolean shadowColor1Mipmap;
    public final boolean shadowColor1Nearest;

    // TODO: Load for Tea shader
    public final @Nullable Integer noiseTextureResolution;

    public final double shadowDistance;
    // TODO: PROP Needs_Test_Shader
    public final double shadowDistanceRenderMul;
    public final boolean shadowHardwareFiltering0;
    public final boolean shadowHardwareFiltering1;
    public final double shadowIntervalSize;
    // TODO: PROP Needs_Test_Shader
    public final @Nullable Double shadowMapFov;
    public final int shadowMapResolution;
    //endregion

    @Setter
    public static class Builder {
        //region properties
        public Quality clouds = null;
        public Boolean moon = null;
        public Boolean sun = null;
        public Boolean underwaterOverlay = null;
        public Boolean vignette = null;
        public Boolean oldHandLight = null;
        public Boolean oldLighting = null;

        public boolean shadowTerrain = true;
        public boolean shadowTranslucent = true;
        public boolean shadowEntities = true;
        public boolean shadowBlockEntities = true;

        public boolean backFaceSolid = true;
        public boolean backFaceCutout = true;
        public boolean backFaceCutoutMipped = true;
        public boolean backFaceTranslucent = true;

        public boolean frustumCulling = true;
        public boolean shadowCulling = true;
        public boolean rainDepth = false;
        public boolean beaconBeamDepth = true;

        public final Object2ObjectMap<String, Object2ObjectMap<String, String>> textures = new Object2ObjectLinkedOpenHashMap<>();
        public String noiseTexture = null;
        //endregion

        //region glsl
        public Double ambientOcclusionLevel = null;
        public double sunPathRotation = 0.0;
        public double eyeBrightnessHalfLife = 10.0;
        public double centerDepthHalfLife = 1.0;
        public double drynessHalfLife = 200.0;
        public double wetnessHalfLife = 600.0;

        public final ObjectSet<String> bufferClearDisabled = new ObjectLinkedOpenHashSet<>();
        public final Object2ObjectMap<String, Vector4dc> bufferClearColor = new Object2ObjectLinkedOpenHashMap<>();
        public final Object2ObjectMap<String, String> bufferFormat = new Object2ObjectLinkedOpenHashMap<>();

        public boolean shadowDepth0Mipmap = false;
        public boolean shadowDepth0Nearest = false;
        public boolean shadowDepth1Mipmap = false;
        public boolean shadowDepth1Nearest = false;

        public boolean shadowColor0Mipmap = false;
        public boolean shadowColor0Nearest = false;
        public boolean shadowColor1Mipmap = false;
        public boolean shadowColor1Nearest = false;

        public Integer noiseTextureResolution = null;

        public double shadowDistance = 160.0;
        public double shadowDistanceRenderMul = -1.0;
        public boolean shadowHardwareFiltering0 = false;
        public boolean shadowHardwareFiltering1 = false;
        public double shadowIntervalSize = 2.0;
        public Double shadowMapFov = null;
        public int shadowMapResolution = 1024;
        //endregion

        Builder() {
        }

        public ShaderLoaderOutParams build() {
            val textures = new ObjectArrayList<StagedTexture>();
            Object2ObjectMaps.fastForEach(this.textures, entry1 -> {
                val stage = entry1.getKey();
                Object2ObjectMaps.fastForEach(entry1.getValue(), entry2 -> {
                    val bufferName = entry2.getKey();
                    val path = entry2.getValue();
                    textures.add(new StagedTexture(stage, bufferName, path));
                });
            });
            //@formatter:off
            return new ShaderLoaderOutParams(
                    //properties
                    clouds, moon, sun, underwaterOverlay, vignette, oldHandLight, oldLighting,
                    shadowTerrain, shadowTranslucent, shadowEntities, shadowBlockEntities,
                    backFaceSolid, backFaceCutout, backFaceCutoutMipped, backFaceTranslucent,
                    frustumCulling, shadowCulling, rainDepth, beaconBeamDepth,
                    ObjectLists.unmodifiable(textures), noiseTexture,

                    //glsl
                    ambientOcclusionLevel, sunPathRotation, eyeBrightnessHalfLife, centerDepthHalfLife, drynessHalfLife, wetnessHalfLife,
                    ObjectLists.unmodifiable(new ObjectArrayList<>(bufferClearDisabled)), Object2ObjectMaps.unmodifiable(bufferClearColor), Object2ObjectMaps.unmodifiable(bufferFormat),
                    shadowDepth0Mipmap, shadowDepth0Nearest, shadowDepth1Mipmap, shadowDepth1Nearest,
                    shadowColor0Mipmap, shadowColor0Nearest, shadowColor1Mipmap, shadowColor1Nearest,

                    noiseTextureResolution,

                    shadowDistance, shadowDistanceRenderMul, shadowHardwareFiltering0, shadowHardwareFiltering1, shadowIntervalSize, shadowMapFov, shadowMapResolution
            );
            //@formatter:on
        }
    }

    public enum Quality {
        Fancy,
        Fast,
        Off
    }

    //TODO convert to record
    public static final class StagedTexture {
        private final String stage;
        private final String bufferName;
        private final String path;

        public StagedTexture(String stage, String bufferName, String path) {
            this.stage = stage;
            this.bufferName = bufferName;
            this.path = path;
        }

        public String stage() {
            return stage;
        }

        public String bufferName() {
            return bufferName;
        }

        public String path() {
            return path;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (StagedTexture) obj;
            return Objects.equals(this.stage, that.stage) &&
                   Objects.equals(this.bufferName, that.bufferName) &&
                   Objects.equals(this.path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stage, bufferName, path);
        }

        @Override
        public String toString() {
            return "StagedTexture[" +
                   "stage=" +
                   stage +
                   ", " +
                   "bufferName=" +
                   bufferName +
                   ", " +
                   "path=" +
                   path +
                   ']';
        }

    }
}
