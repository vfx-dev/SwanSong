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

import com.ventooth.swansong.Share;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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
    public static final ShaderId gbuffers_portal;
    public static final ShaderId gbuffers_instanced;

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
        gbuffers_portal             = listBuilder.addSingle("gbuffers_portal");
        gbuffers_instanced          = listBuilder.addSingle("gbuffers_instanced");
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

    private static Map<ShaderId, ShaderId> fallbacks = new HashMap<>();
    private static volatile boolean registryLocked = false;

    public static @Nullable ShaderId getFallback(ShaderId loc) {
        if (registryLocked) {
            return fallbacks.get(loc);
        } else {
            throw new IllegalStateException("Registry must be locked before a fallback can be resolved!");
        }
    }

    public static void registerInternalFallbacks() {
        registerFallback(gbuffers_basic, null);
        registerFallback(gbuffers_skybasic, gbuffers_basic);
        registerFallback(gbuffers_textured, gbuffers_basic);
        registerFallback(gbuffers_skytextured, gbuffers_textured);
        registerFallback(gbuffers_clouds, gbuffers_textured);
        registerFallback(gbuffers_beaconbeam, gbuffers_textured);
        registerFallback(gbuffers_armor_glint, gbuffers_textured);
        registerFallback(gbuffers_spidereyes, gbuffers_textured);
        registerFallback(gbuffers_textured_lit, gbuffers_textured);
        registerFallback(gbuffers_item, gbuffers_textured_lit);
        registerFallback(gbuffers_entities, gbuffers_textured_lit);
        registerFallback(gbuffers_weather, gbuffers_textured_lit);
        registerFallback(gbuffers_hand, gbuffers_textured_lit);
        registerFallback(gbuffers_hand_water, gbuffers_hand);
        registerFallback(gbuffers_terrain, gbuffers_textured_lit);
        registerFallback(gbuffers_terrain_solid, gbuffers_terrain);
        registerFallback(gbuffers_terrain_cutout_mip, gbuffers_terrain);
        registerFallback(gbuffers_terrain_cutout, gbuffers_terrain);
        registerFallback(gbuffers_damagedblock, gbuffers_terrain);
        registerFallback(gbuffers_water, gbuffers_terrain);
        registerFallback(gbuffers_block, gbuffers_terrain);
        registerFallback(gbuffers_portal, gbuffers_block);
        registerFallback(gbuffers_instanced, gbuffers_entities);

        registerFallback(blit_color_mismatched, null);
        registerFallback(blit_depth_mismatched, null);
        // Relevant for MacOS as the identical ones need GLSL 130
        registerFallback(blit_color_identical, blit_color_mismatched);
        registerFallback(blit_depth_identical, blit_depth_mismatched);
    }

    public static synchronized void registerFallback(@NonNull ShaderId shader,
                                                     @Nullable ShaderId fallback) {
        if (registryLocked) {
            throw new IllegalStateException("Registry is already locked! Register shaders in the init phase!");
        }
        if (fallbacks.containsKey(shader)) {
            throw new IllegalStateException(shader + " is already registered!");
        }
        fallbacks.put(shader, fallback);

        Share.log.debug("Registered Shader Fallback: [{}] -> [{}]", toStr(shader), toStr(fallback));
    }

    public static synchronized void validateRegistry() {
        registryLocked = true;
        fallbacks.forEach((shader, fallback) -> {
            if (fallback == null) {
                return;
            }
            if (!fallbacks.containsKey(fallback)) {
                throw new IllegalStateException("Shader " + shader + " defined a missing fallback: " + fallback);
            }
        });
        fallbacks = Collections.unmodifiableMap(fallbacks);
        Share.log.info("Locked Shader Type Registry with {} entries:", fallbacks.size());

        val reverse = new HashMap<ShaderId, List<ShaderId>>();
        fallbacks.forEach((shader, fallback) -> {
            reverse.computeIfAbsent(fallback, loc -> new ArrayList<>())
                   .add(shader);
        });
        reverse.forEach((fallback, shaders) -> {
            val sj = new StringJoiner(" ");
            shaders.forEach(shader -> sj.add(toStr(shader)));
            Share.log.info("[{}] <- [{}]", toStr(fallback), sj);
        });
    }

    private static String toStr(@Nullable ShaderId loc) {
        if (loc == null) {
            return "NULL";
        }
        val name = loc.path();
        val domain = loc.namespace();
        if ("minecraft".equals(domain)) {
            return name;
        } else {
            return domain + ":" + name;
        }
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
