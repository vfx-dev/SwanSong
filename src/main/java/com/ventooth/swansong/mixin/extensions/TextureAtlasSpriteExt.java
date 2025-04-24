/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.extensions;

import lombok.SneakyThrows;
import lombok.val;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class TextureAtlasSpriteExt {
    private static final MethodHandle constructor;

    static {
        try {
            val m = TextureAtlasSprite.class.getDeclaredConstructor(String.class);
            m.setAccessible(true);
            constructor = MethodHandles.lookup()
                                       .unreflectConstructor(m);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public static TextureAtlasSprite newInstance(String spriteName) {
        return (TextureAtlasSprite) constructor.invokeExact(spriteName);
    }
}
