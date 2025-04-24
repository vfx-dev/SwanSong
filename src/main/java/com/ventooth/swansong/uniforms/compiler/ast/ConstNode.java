/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms.compiler.ast;

import com.ventooth.swansong.uniforms.Type;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector4d;
import org.joml.Vector4dc;

public interface ConstNode extends UntypedNode, TypedNode {
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class Int implements ConstNode {
        public final int value;

        private static final int CACHE_RANGE = 128;

        private static final Int[] posCache = new Int[CACHE_RANGE];
        private static final Int[] negCache = new Int[CACHE_RANGE];

        static {
            for (int i = 0; i < CACHE_RANGE; i++) {
                posCache[i] = new Int(i);
                negCache[i] = new Int(~i);
            }
        }

        public static Int of(int value) {
            if (value >= 0 && value < CACHE_RANGE) {
                return posCache[value];
            }
            if (value < 0 && value >= -CACHE_RANGE) {
                return negCache[~value];
            }
            return new Int(value);
        }

        @Override
        public @NotNull Type outputType() {
            return Type.Int;
        }
    }

    @RequiredArgsConstructor
    final class Float implements ConstNode {
        public final double value;

        @Override
        public @NotNull Type outputType() {
            return Type.Float;
        }
    }

    @RequiredArgsConstructor
    final class Vec2 implements ConstNode {
        public final double x;
        public final double y;

        public Vec2(Vector2dc vec) {
            this(vec.x(), vec.y());
        }

        @Override
        public @NotNull Type outputType() {
            return Type.Vec2;
        }

        public Vector2dc value() {
            return new Vector2d(x, y);
        }
    }

    @RequiredArgsConstructor
    final class Vec3 implements ConstNode {
        public final double x;
        public final double y;
        public final double z;

        public Vec3(Vector3dc vec) {
            this(vec.x(), vec.y(), vec.z());
        }

        @Override
        public @NotNull Type outputType() {
            return Type.Vec3;
        }

        public Vector3dc value() {
            return new Vector3d(x, y, z);
        }
    }

    @RequiredArgsConstructor
    final class Vec4 implements ConstNode {
        public final double x;
        public final double y;
        public final double z;
        public final double w;

        public Vec4(Vector4dc vec) {
            this(vec.x(), vec.y(), vec.z(), vec.w());
        }

        @Override
        public @NotNull Type outputType() {
            return Type.Vec4;
        }

        public Vector4dc value() {
            return new Vector4d(x, y, z, w);
        }
    }

    enum Bool implements ConstNode {
        True,
        False;

        public static Bool of(boolean value) {
            return value ? True : False;
        }

        public boolean value() {
            return switch (this) {
                case True -> true;
                case False -> false;
            };
        }

        @Override
        public @NotNull Type outputType() {
            return Type.Bool;
        }

        @Override
        public String toString() {
            return Boolean.toString(value());
        }
    }
}
