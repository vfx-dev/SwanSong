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

/**
 * Currently only used to feed: <a href="https://shaders.properties/current/reference/uniforms/rendering/#renderstage">uniform int renderStage</a>
 * <p>
 * Ordered as defined within
 * <a href="https://github.com/IrisShaders/Iris/blob/b4b2c22c1587264989d746ae57423c32cf6dfcb4/common/src/main/java/net/irisshaders/iris/pipeline/WorldRenderingPhase.java">WorldRenderingPhase</a>
 * in Iris.
 * <p>
 * And documentation sourced from: <a href="https://shaders.properties/current/reference/uniforms/rendering/#renderstage">shaders.properties/.../render_stages</a>
 */
public enum MCRenderStage {
    /**
     * Undefined (should not occur normally)
     *
     * @implNote Treated as fallback
     */
    NONE,
    /**
     * The upper portion of the sky not including stars or textures like the sun/moon.
     * <p>
     * TODO: Wire this up
     */
    SKY,
    /**
     * The lower portion of the sky used to generate the sunset effect,
     * does not include textures like the sun/moon.
     * <p>
     * TODO: Wire this up
     */
    SUNSET,
    /**
     * Custom sky texture from resource pack, such as from OptiFine or FabricSkyBoxes.
     * <p>
     * TODO: No custom sky, needs example to validate, is this a V1 candidate?
     */
    CUSTOM_SKY,
    /**
     * Sun texture in the sky.
     */
    SUN,
    /**
     * Moon texture in the sky.
     */
    MOON,
    /**
     * Vanilla star geometry in the sky.
     */
    STARS,
    /**
     * Void sky, which renders below the player when they are below bedrock.
     * <p>
     * TODO: Wire this up
     */
    VOID,
    /**
     * Solid terrain geometry.
     */
    TERRAIN_SOLID,
    /**
     * Intended for cutout geometry with mipmaps.
     *
     * @implNote Unused, falls back to {@link MCRenderStage#TERRAIN_SOLID}
     */
    TERRAIN_CUTOUT_MIPPED,
    /**
     * Intended for cutout geometry without mipmaps.
     *
     * @implNote Unused, falls back to {@link MCRenderStage#TERRAIN_SOLID}
     */
    TERRAIN_CUTOUT,
    /**
     * Entity geometry including nametag text.
     */
    ENTITIES,
    /**
     * Block entity geometry.
     */
    BLOCK_ENTITIES,
    /**
     * End portal, thaumcraft pylon, etc.
     */
    BLOCK_ENTITIES_PORTAL,
    /**
     * Intended to be used for the block cracks texture.
     *
     * @implNote Unused, falls back to {@link MCRenderStage#TERRAIN_SOLID}
     */
    DESTROY,
    /**
     * Player block selection outline, does not include any other lines (such as fishing lines and leads)
     */
    OUTLINE,
    /**
     * Intended to be used for debug view lines.
     * <p>
     * TODO: Wire this up
     */
    DEBUG,
    /**
     * Solid or cutout geometry for held items/blocks.
     */
    HAND_SOLID,
    /**
     * Transparent terrain (such as water and stained glass)
     */
    TERRAIN_TRANSLUCENT,
    /**
     * Indented to be used for the tripwire string texture
     *
     * @implNote Unused, falls back to {@link MCRenderStage#TERRAIN_SOLID}
     */
    TRIPWIRE,
    /**
     * All particle geometry
     */
    PARTICLES,
    /**
     * Vanilla cloud geometry
     */
    CLOUDS,
    /**
     * Rain and snow geometry (not including particle effects)
     */
    RAIN_SNOW,
    /**
     * World border geometry.
     *
     * @implNote Not relevant on MC [1.7.10]
     */
    WORLD_BORDER,
    /**
     * Transparent geometry for held items/blocks
     */
    HAND_TRANSLUCENT,
    ;

    private final String macro = "MC_RENDER_STAGE_" + name();

    /**
     * @return Macro name to be used in a shader
     */
    public String macro() {
        return this.macro;
    }

    /**
     * @return Macro value to be used in a shader and updated in uniform
     */
    public int value() {
        return ordinal();
    }
}
