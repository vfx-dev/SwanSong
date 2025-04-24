/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms;

import com.falsepattern.lib.util.MathUtil;
import lombok.SneakyThrows;
import lombok.val;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector4d;
import org.joml.Vector4dc;

public final class Builtins {
    public static final UniformFunctionRegistry REGISTRY = register();

    @SneakyThrows
    private static UniformFunctionRegistry register() {
        val klass = Builtins.class;
        val reg = new UniformFunctionRegistry.Single();
        reg.pure(klass.getDeclaredMethod("pi"));
        reg.pure(klass.getDeclaredMethod("vec2", double.class));
        reg.pure(klass.getDeclaredMethod("vec2", double.class, double.class));
        reg.pure(klass.getDeclaredMethod("vec3", double.class));
        reg.pure(klass.getDeclaredMethod("vec3", double.class, double.class, double.class));
        reg.pure(klass.getDeclaredMethod("vec4", double.class));
        reg.pure(klass.getDeclaredMethod("vec4", double.class, double.class, double.class, double.class));
        reg.pure(klass.getDeclaredMethod("radians", double.class), "radians", "torad");
        reg.pure(klass.getDeclaredMethod("degrees", double.class), "degrees", "todeg");
        reg.pure(klass.getDeclaredMethod("sin", double.class));
        reg.pure(klass.getDeclaredMethod("cos", double.class));
        reg.pure(klass.getDeclaredMethod("asin", double.class));
        reg.pure(klass.getDeclaredMethod("acos", double.class));
        reg.pure(klass.getDeclaredMethod("atan", double.class));
        reg.pure(klass.getDeclaredMethod("atan2", double.class, double.class), "atan2", "atan");
        reg.pure(klass.getDeclaredMethod("exp", double.class));
        reg.pure(klass.getDeclaredMethod("pow", double.class, double.class));
        reg.pure(klass.getDeclaredMethod("exp2", double.class));
        reg.pure(klass.getDeclaredMethod("exp10", double.class));
        reg.pure(klass.getDeclaredMethod("log", double.class));
        reg.pure(klass.getDeclaredMethod("log", double.class, double.class));
        reg.pure(klass.getDeclaredMethod("log2", double.class));
        reg.pure(klass.getDeclaredMethod("log10", double.class));
        reg.pure(klass.getDeclaredMethod("sqrt", double.class));
        reg.pure(klass.getDeclaredMethod("abs", double.class));
        reg.pure(klass.getDeclaredMethod("abs", int.class));
        reg.pure(klass.getDeclaredMethod("abs", Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("abs", Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("abs", Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("signum", double.class), "signum", "sign");
        reg.pure(klass.getDeclaredMethod("signum", int.class), "signum", "sign");
        reg.pure(klass.getDeclaredMethod("floor", double.class));
        reg.pure(klass.getDeclaredMethod("floor", Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("floor", Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("floor", Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("ceil", double.class));
        reg.pure(klass.getDeclaredMethod("ceil", Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("ceil", Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("ceil", Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("frac", double.class));
        reg.pure(klass.getDeclaredMethod("min", double.class, double.class));
        reg.pure(klass.getDeclaredMethod("min", int.class, int.class));
        reg.pure(klass.getDeclaredMethod("min", Vector2dc.class, Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("min", Vector3dc.class, Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("min", Vector4dc.class, Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("max", double.class, double.class));
        reg.pure(klass.getDeclaredMethod("max", int.class, int.class));
        reg.pure(klass.getDeclaredMethod("max", Vector2dc.class, Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("max", Vector3dc.class, Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("max", Vector4dc.class, Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("clamp", double.class, double.class, double.class));
        reg.pure(klass.getDeclaredMethod("clamp", int.class, int.class, int.class));
        reg.pure(klass.getDeclaredMethod("clamp", Vector2dc.class, Vector2dc.class, Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("clamp", Vector3dc.class, Vector3dc.class, Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("clamp", Vector4dc.class, Vector4dc.class, Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("mix", double.class, double.class, double.class));
        reg.pure(klass.getDeclaredMethod("edge", double.class, double.class));
        reg.pure(klass.getDeclaredMethod("edge", int.class, int.class));
        reg.pure(klass.getDeclaredMethod("fmod", double.class, double.class));
        reg.pure(klass.getDeclaredMethod("between", double.class, double.class, double.class));
        reg.pure(klass.getDeclaredMethod("equals", double.class, double.class, double.class));
        return reg;
    }

    private static final double LOG2 = Math.log(2);

    public static double pi() {
        return Math.PI;
    }

    public static Vector2dc vec2(double s) {
        return new Vector2d(s);
    }

    public static Vector2dc vec2(double x, double y) {
        return new Vector2d(x, y);
    }

    public static Vector3dc vec3(double s) {
        return new Vector3d(s);
    }

    public static Vector3dc vec3(double x, double y, double z) {
        return new Vector3d(x, y, z);
    }

    public static Vector4dc vec4(double s) {
        return new Vector4d(s);
    }

    public static Vector4dc vec4(double x, double y, double z, double w) {
        return new Vector4d(x, y, z, w);
    }

    public static double radians(double deg) {
        return Math.toRadians(deg);
    }

    public static double degrees(double rad) {
        return Math.toDegrees(rad);
    }

    public static double sin(double x) {
        return Math.sin(x);
    }

    public static double cos(double x) {
        return Math.cos(x);
    }

    public static double asin(double x) {
        return Math.asin(x);
    }

    public static double acos(double x) {
        return Math.acos(x);
    }

    public static double atan(double x) {
        return Math.atan(x);
    }

    public static double atan2(double y, double x) {
        return Math.atan2(y, x);
    }

    public static double exp(double x) {
        return Math.exp(x);
    }

    public static double pow(double x, double y) {
        return Math.pow(x, y);
    }

    public static double exp2(double x) {
        return Math.pow(2, x);
    }

    public static double exp10(double x) {
        return Math.pow(10, x);
    }

    public static double log(double x) {
        return Math.log(x);
    }

    public static double log(double base, double value) {
        return Math.log(value) / Math.log(base);
    }

    public static double log2(double x) {
        return Math.log(x) / LOG2;
    }

    public static double log10(double x) {
        return Math.log10(x);
    }

    public static double sqrt(double x) {
        return Math.sqrt(x);
    }

    public static double abs(double x) {
        return Math.abs(x);
    }

    public static int abs(int x) {
        return Math.abs(x);
    }

    public static Vector2dc abs(Vector2dc vec) {
        return vec.absolute(new Vector2d());
    }

    public static Vector3dc abs(Vector3dc vec) {
        return vec.absolute(new Vector3d());
    }

    public static Vector4dc abs(Vector4dc vec) {
        return vec.absolute(new Vector4d());
    }

    public static double signum(double x) {
        return Math.signum(x);
    }

    public static int signum(int x) {
        return Integer.compare(x, 0);
    }

    public static double floor(double x) {
        return Math.floor(x);
    }

    public static Vector2dc floor(Vector2dc vec) {
        return vec.floor(new Vector2d());
    }

    public static Vector3dc floor(Vector3dc vec) {
        return vec.floor(new Vector3d());
    }

    public static Vector4dc floor(Vector4dc vec) {
        return vec.floor(new Vector4d());
    }

    public static double ceil(double x) {
        return Math.ceil(x);
    }

    public static Vector2dc ceil(Vector2dc vec) {
        return vec.ceil(new Vector2d());
    }

    public static Vector3dc ceil(Vector3dc vec) {
        return vec.ceil(new Vector3d());
    }

    public static Vector4dc ceil(Vector4dc vec) {
        return vec.ceil(new Vector4d());
    }

    public static double frac(double x) {
        return MathUtil.frac(x);
    }

    public static double min(double x, double y) {
        return Math.min(x, y);
    }

    public static int min(int x, int y) {
        return Math.min(x, y);
    }

    public static Vector2dc min(Vector2dc a, Vector2dc b) {
        return a.min(b, new Vector2d());
    }

    public static Vector3dc min(Vector3dc a, Vector3dc b) {
        return a.min(b, new Vector3d());
    }

    public static Vector4dc min(Vector4dc a, Vector4dc b) {
        return a.min(b, new Vector4d());
    }

    public static double max(double x, double y) {
        return Math.max(x, y);
    }

    public static int max(int x, int y) {
        return Math.max(x, y);
    }

    public static Vector2dc max(Vector2dc a, Vector2dc b) {
        return a.max(b, new Vector2d());
    }

    public static Vector3dc max(Vector3dc a, Vector3dc b) {
        return a.max(b, new Vector3d());
    }

    public static Vector4dc max(Vector4dc a, Vector4dc b) {
        return a.max(b, new Vector4d());
    }

    public static double clamp(double x, double min, double max) {
        return MathUtil.clamp(x, min, max);
    }

    public static int clamp(int x, int min, int max) {
        return MathUtil.clamp(x, min, max);
    }

    public static Vector2dc clamp(Vector2dc x, Vector2dc min, Vector2dc max) {
        return new Vector2d(MathUtil.clamp(x.x(), min.x(), max.x()), MathUtil.clamp(x.y(), min.y(), max.y()));
    }

    public static Vector3dc clamp(Vector3dc x, Vector3dc min, Vector3dc max) {
        return new Vector3d(MathUtil.clamp(x.x(), min.x(), max.x()),
                            MathUtil.clamp(x.y(), min.y(), max.y()),
                            MathUtil.clamp(x.z(), min.z(), max.z()));
    }

    public static Vector4dc clamp(Vector4dc x, Vector4dc min, Vector4dc max) {
        return new Vector4d(MathUtil.clamp(x.x(), min.x(), max.x()),
                            MathUtil.clamp(x.y(), min.y(), max.y()),
                            MathUtil.clamp(x.z(), min.z(), max.z()),
                            MathUtil.clamp(x.w(), min.w(), max.w()));
    }

    public static double mix(double x, double y, double a) {
        return org.joml.Math.fma(y - x, a, x);
    }

    public static double edge(double k, double x) {
        return x < k ? 0 : 1;
    }

    public static int edge(int k, int x) {
        return x < k ? 0 : 1;
    }

    public static double fmod(double x, double y) {
        return x - y * floor(x / y);
    }

    public static boolean between(double a, double min, double max) {
        return a >= min && a <= max;
    }

    public static boolean equals(double a, double b, double epsilon) {
        return abs(a - b) <= epsilon;
    }

}
