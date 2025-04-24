/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.debug;

import com.ventooth.swansong.StackStateTracker;
import com.ventooth.swansong.config.DebugConfig;
import com.ventooth.swansong.config.ModuleConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.lwjgl.opengl.KHRDebug;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GLDebugGroups {
    public static final Key GAME_LOOP = Key.of("GAME_LOOP", 0);
    public static final Key TESS_DRAW = Key.of("TESS_DRAW", 50);
    public static final Key RENDER_WORLD = Key.of("RENDER_WORLD", 100);

    public static final Key RENDER_SHADOW = Key.of("RENDER_SHADOW", 190);

    public static final Key RENDER_SHADOW_0_TERRAIN = Key.of("RENDER_SHADOW_0_TERRAIN", 200);
    public static final Key RENDER_SHADOW_0_ENTITIES = Key.of("RENDER_SHADOW_0_ENTITIES", 201);
    public static final Key RENDER_SHADOW_1_TERRAIN = Key.of("RENDER_SHADOW_1_TERRAIN", 240);
    public static final Key RENDER_SHADOW_1_ENTITIES = Key.of("RENDER_SHADOW_1_ENTITIES", 241);

    public static final Key RENDER_0_TERRAIN = Key.of("RENDER_0_TERRAIN", 290);

    public static final Key RENDER_ENTITIES = Key.of("RENDER_ENTITIES", 300);

    public static final Key RENDER_SELECTION_BOX = Key.of("RENDER_SELECTION_BOX", 310);

    public static final Key GEN_COMPOSITE_MIPS = Key.of("GEN_COMPOSITE_MIPS", 899);

    public static final Key RENDER_DEFERRED = Key.of("RENDER_DEFERRED", 900);
    public static final Key RENDER_DEFERRED_DRAW = Key.of("RENDER_DEFERRED_DRAW", 910);

    public static final Key RENDER_COMPOSITE = Key.of("RENDER_COMPOSITE", 1000);
    public static final Key RENDER_COMPOSITE_DRAW = Key.of("RENDER_COMPOSITE_DRAW", 1010);

    public static final Key RENDER_COMPOSITE_FINAL = Key.of("RENDER_COMPOSITE_FINAL", 1100);
    public static final Key RENDER_COMPOSITE_FINAL_DRAW = Key.of("RENDER_COMPOSITE_FINAL_DRAW", 1110);

    private static final StackStateTracker<Void> stack = new StackStateTracker<>(true);

    private static final boolean ENABLED = ModuleConfig.Debug && DebugConfig.UseGLDebugGroups;

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static void push(@NonNull GLDebugGroups.Key group) {
        if (isEnabled()) {
            stack.push(group.key, null);
            KHRDebug.glPushDebugGroup(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, group.id, group.value);
        }
    }

    public static void pop(@NonNull GLDebugGroups.Key group) {
        if (isEnabled()) {
            stack.pop(group.key);
            KHRDebug.glPopDebugGroup();
        }
    }

    public static final class Key {
        private final StackStateTracker.Key key;

        public final String value;
        public final int id;

        public Key(String value, int id) {
            this.key = new StackStateTracker.Key(value);

            this.value = value;
            this.id = id;
        }

        public static Key of(String value, int id) {
            return new Key(value, id);
        }

        public void push() {
            GLDebugGroups.push(this);
        }

        public void pop() {
            GLDebugGroups.pop(this);
        }
    }
}
