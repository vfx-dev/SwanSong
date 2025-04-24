/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.interfaces;

import com.ventooth.swansong.sufrace.PBRTexture2D;
import com.ventooth.swansong.sufrace.TextureMeta;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.ResourceLocation;

public interface PBRTextureHolder {
    /**
     * @return {@code true} if the current base texture is valid
     *
     * @implNote Internally depends on {@link #swan$supportsPbr()} returning {@code true}
     * @apiNote This function must be called before most other methods can be used, but is not itself an indication of a fault.
     */
    boolean swan$isValid();

    /**
     * Called on texture load to initialize the base texture
     *
     * @param base Base texture location
     * @param width Base texture width
     * @param height Base texture height
     * @param blur If the base texture is using nearest or linear scaling
     * @param clamp If the base texture is clamped or tiling
     */
    void swan$baseInit(ResourceLocation base, int width, int height, boolean blur, boolean clamp);

    /**
     * @return Human readable description of this state
     */
    String swan$baseToString();

    /**
     * @return Base resource location for this texture
     *
     * @throws IllegalStateException if {@link #swan$isValid()} returns {@code false}
     */
    ResourceLocation swan$base() throws IllegalStateException;

    /**
     * @return Meta info about this texture
     *
     * @throws IllegalStateException if {@link #swan$isValid()} returns {@code false}
     */
    TextureMeta swan$meta() throws IllegalStateException;

    /**
     * @return Width of the base texture
     *
     * @throws IllegalStateException if {@link #swan$isValid()} returns {@code false}
     */
    int swan$width() throws IllegalStateException;

    /**
     * @return Height of the base texture
     *
     * @throws IllegalStateException if {@link #swan$isValid()} returns {@code false}
     */
    int swan$height() throws IllegalStateException;

    /**
     * @return {@code true} if this texture supports PBR.
     *
     * @apiNote This exists as this interface is implemented top-level, with this method overridden by specific textures that actually need PBR containers
     */
    default boolean swan$supportsPbr() {
        return false;
    }

    /**
     * @return PBR Texture bundle if the PBR texture has been initialized
     *
     * @throws IllegalStateException if {@link #swan$isValid()} returns {@code false}
     */
    @Nullable PBRTexture2D.Bundle swan$pbrTex() throws IllegalStateException;

    /**
     * @param pbrTex New PBR Texture bundle
     *
     * @return Previous PBR Texture bundle
     *
     * @throws IllegalStateException if {@link #swan$isValid()} returns {@code false}
     */
    @Nullable PBRTexture2D.Bundle swan$pbrTex(@Nullable PBRTexture2D.Bundle pbrTex) throws IllegalStateException;
}
