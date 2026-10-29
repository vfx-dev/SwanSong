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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public final class ShaderTypes {
    public static final ShaderId gbuffers_basic;
    public static final ShaderId gbuffers_skybasic;
    public static final ShaderId gbuffers_textured;
    public static final ShaderId gbuffers_skytextured;
    public static final ShaderId gbuffers_clouds;
    public static final ShaderId gbuffers_beaconbeam;
    public static final ShaderId gbuffers_armor_glint;
    public static final ShaderId gbuffers_spidereyes;
    public static final ShaderId gbuffers_textured_lit;
    public static final ShaderId gbuffers_item;
    public static final ShaderId gbuffers_entities;
    public static final ShaderId gbuffers_weather;
    public static final ShaderId gbuffers_hand;
    public static final ShaderId gbuffers_hand_water;
    public static final ShaderId gbuffers_terrain;
    public static final ShaderId gbuffers_terrain_solid;
    public static final ShaderId gbuffers_terrain_cutout_mip;
    public static final ShaderId gbuffers_terrain_cutout;
    public static final ShaderId gbuffers_damagedblock;
    public static final ShaderId gbuffers_water;
    public static final ShaderId gbuffers_block;

    public static final ShaderId shadow;

    public static final @Unmodifiable ObjectList<ShaderId> deferredList;
    public static final @Unmodifiable ObjectList<ShaderId> compositeList;

    public static final ShaderId _final;

    public static final @Unmodifiable ObjectList<ShaderId> general;

    static {
        val listBuilder = new ShaderTypeListBuilder();
        // @formatter:off

        // GBuffers
        gbuffers_basic              = listBuilder.addSingle("gbuffers_basic");
        gbuffers_skybasic           = listBuilder.addSingle("gbuffers_skybasic");
        gbuffers_textured           = listBuilder.addSingle("gbuffers_textured");
        gbuffers_skytextured        = listBuilder.addSingle("gbuffers_skytextured");
        gbuffers_clouds             = listBuilder.addSingle("gbuffers_clouds");
        gbuffers_beaconbeam         = listBuilder.addSingle("gbuffers_beaconbeam");
        gbuffers_armor_glint        = listBuilder.addSingle("gbuffers_armor_glint");
        gbuffers_spidereyes         = listBuilder.addSingle("gbuffers_spidereyes");
        gbuffers_textured_lit       = listBuilder.addSingle("gbuffers_textured_lit");
        gbuffers_item               = listBuilder.addSingle("gbuffers_item");
        gbuffers_entities           = listBuilder.addSingle("gbuffers_entities");
        gbuffers_weather            = listBuilder.addSingle("gbuffers_weather");
        gbuffers_hand               = listBuilder.addSingle("gbuffers_hand");
        gbuffers_hand_water         = listBuilder.addSingle("gbuffers_hand_water");
        gbuffers_terrain            = listBuilder.addSingle("gbuffers_terrain");
        gbuffers_terrain_solid      = listBuilder.addSingle("gbuffers_terrain_solid");
        gbuffers_terrain_cutout_mip = listBuilder.addSingle("gbuffers_terrain_cutout_mip");
        gbuffers_terrain_cutout     = listBuilder.addSingle("gbuffers_terrain_cutout");
        gbuffers_damagedblock       = listBuilder.addSingle("gbuffers_damagedblock");
        gbuffers_water              = listBuilder.addSingle("gbuffers_water");
        gbuffers_block              = listBuilder.addSingle("gbuffers_block");
        // Shadows
        shadow                      = listBuilder.addSingle("shadow");
        // Composites
        deferredList                = listBuilder.addMulti("deferred", 100);
        compositeList               = listBuilder.addMulti("composite", 100);
        _final                      = listBuilder.addSingle("final");
        // @formatter:on

        general = listBuilder.build();
    }

    public static final ShaderId blit_color_identical;
    public static final ShaderId blit_depth_identical;
    public static final ShaderId blit_color_mismatched;
    public static final ShaderId blit_depth_mismatched;

    public static final @Unmodifiable ObjectList<ShaderId> internal;

    static {
        val listBuilder = new ShaderTypeListBuilder();

        // Blit
        blit_color_identical = listBuilder.addSingle("swansong:blit_identical/blit_color");
        blit_depth_identical = listBuilder.addSingle("swansong:blit_identical/blit_depth");
        blit_color_mismatched = listBuilder.addSingle("swansong:blit_mismatched/blit_color");
        blit_depth_mismatched = listBuilder.addSingle("swansong:blit_mismatched/blit_depth");


        internal = listBuilder.build();
    }

    private static final Map<ShaderId, ShaderId> fallbacks = createFallbacks();

    public static @Nullable ShaderId getFallback(ShaderId loc) {
        return fallbacks.get(loc);
    }

    private static Map<ShaderId, ShaderId> createFallbacks() {
        val map = new HashMap<ShaderId, ShaderId>();
        register(map, gbuffers_basic, null);
        register(map, gbuffers_skybasic, gbuffers_basic);
        register(map, gbuffers_textured, gbuffers_basic);
        register(map, gbuffers_skytextured, gbuffers_textured);
        register(map, gbuffers_clouds, gbuffers_textured);
        register(map, gbuffers_beaconbeam, gbuffers_textured);
        register(map, gbuffers_armor_glint, gbuffers_textured);
        register(map, gbuffers_spidereyes, gbuffers_textured);
        register(map, gbuffers_textured_lit, gbuffers_textured);
        register(map, gbuffers_item, gbuffers_textured_lit);
        register(map, gbuffers_entities, gbuffers_textured_lit);
        register(map, gbuffers_weather, gbuffers_textured_lit);
        register(map, gbuffers_hand, gbuffers_textured_lit);
        register(map, gbuffers_hand_water, gbuffers_hand);
        register(map, gbuffers_terrain, gbuffers_textured_lit);
        register(map, gbuffers_terrain_solid, gbuffers_terrain);
        register(map, gbuffers_terrain_cutout_mip, gbuffers_terrain);
        register(map, gbuffers_terrain_cutout, gbuffers_terrain);
        register(map, gbuffers_damagedblock, gbuffers_terrain);
        register(map, gbuffers_water, gbuffers_terrain);
        register(map, gbuffers_block, gbuffers_terrain);
        register(map, blit_color_mismatched, null);
        register(map, blit_depth_mismatched, null);
        // Relevant for MacOS as the identical ones need GLSL 130
        register(map, blit_color_identical, blit_color_mismatched);
        register(map, blit_depth_identical, blit_depth_mismatched);

        map.forEach((shader, fallback) -> {
            if (fallback != null && !map.containsKey(fallback)) {
                throw new IllegalStateException("Shader " + shader + " defined a missing fallback: " + fallback);
            }
        });
        return Collections.unmodifiableMap(map);
    }

    private static void register(Map<ShaderId, ShaderId> map, ShaderId shader, @Nullable ShaderId fallback) {
        if (map.containsKey(shader)) {
            throw new IllegalStateException(shader + " is already registered!");
        }
        map.put(shader, fallback);
    }

    private static class ShaderTypeListBuilder {
        final ObjectList<ShaderId> list = new ObjectArrayList<>();

        @Unmodifiable
        ObjectList<ShaderId> addMulti(String baseName, int count) {
            val list = new ObjectArrayList<ShaderId>();
            list.add(addSingle(baseName));
            for (var i = 1; i < count; i++) {
                list.add(addSingle(baseName + i));
            }
            return ObjectLists.unmodifiable(list);
        }

        ShaderId addSingle(String name) {
            val loc = ShaderId.of(name);
            list.add(loc);
            return loc;
        }

        @Unmodifiable
        ObjectList<ShaderId> build() {
            return ObjectLists.unmodifiable(list);
        }
    }
}
