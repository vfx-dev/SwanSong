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

import lombok.SneakyThrows;
import lombok.val;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector4d;
import org.joml.Vector4dc;

public class VecUtil {
    public static final UniformFunctionRegistry REGISTRY = register();

    @SneakyThrows
    private static UniformFunctionRegistry register() {
        val klass = VecUtil.class;
        val reg = new UniformFunctionRegistry.Single();
        reg.pure(klass.getDeclaredMethod("add", Vector2dc.class, Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("sub", Vector2dc.class, Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("mul", Vector2dc.class, Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("div", Vector2dc.class, Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("rem", Vector2dc.class, Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("neg", Vector2dc.class));
        reg.pure(klass.getDeclaredMethod("swiz", Vector2dc.class, int.class));

        reg.pure(klass.getDeclaredMethod("add", Vector3dc.class, Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("sub", Vector3dc.class, Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("mul", Vector3dc.class, Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("div", Vector3dc.class, Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("rem", Vector3dc.class, Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("neg", Vector3dc.class));
        reg.pure(klass.getDeclaredMethod("swiz", Vector3dc.class, int.class));

        reg.pure(klass.getDeclaredMethod("add", Vector4dc.class, Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("sub", Vector4dc.class, Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("mul", Vector4dc.class, Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("div", Vector4dc.class, Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("rem", Vector4dc.class, Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("neg", Vector4dc.class));
        reg.pure(klass.getDeclaredMethod("swiz", Vector4dc.class, int.class));
        return reg;
    }

    public static Vector2dc add(Vector2dc a, Vector2dc b) {
        return a.add(b, new Vector2d());
    }

    public static Vector2dc sub(Vector2dc a, Vector2dc b) {
        return a.sub(b, new Vector2d());
    }

    public static Vector2dc mul(Vector2dc a, Vector2dc b) {
        return a.mul(b, new Vector2d());
    }

    public static Vector2dc div(Vector2dc a, Vector2dc b) {
        return a.div(b, new Vector2d());
    }

    public static Vector2dc rem(Vector2dc a, Vector2dc b) {
        return new Vector2d(a.x() % b.x(), a.y() % b.y());
    }

    public static Vector2dc neg(Vector2dc in) {
        return in.negate(new Vector2d());
    }

    public static double swiz(Vector2dc vec, int idx) {
        return vec.get(idx);
    }

    public static Vector3dc add(Vector3dc a, Vector3dc b) {
        return a.add(b, new Vector3d());
    }

    public static Vector3dc sub(Vector3dc a, Vector3dc b) {
        return a.sub(b, new Vector3d());
    }

    public static Vector3dc mul(Vector3dc a, Vector3dc b) {
        return a.mul(b, new Vector3d());
    }

    public static Vector3dc div(Vector3dc a, Vector3dc b) {
        return a.div(b, new Vector3d());
    }

    public static Vector3dc rem(Vector3dc a, Vector3dc b) {
        return new Vector3d(a.x() % b.x(), a.y() % b.y(), a.z() % b.z());
    }

    public static Vector3dc neg(Vector3dc in) {
        return in.negate(new Vector3d());
    }

    public static double swiz(Vector3dc vec, int idx) {
        return vec.get(idx);
    }

    public static Vector4dc add(Vector4dc a, Vector4dc b) {
        return a.add(b, new Vector4d());
    }

    public static Vector4dc sub(Vector4dc a, Vector4dc b) {
        return a.sub(b, new Vector4d());
    }

    public static Vector4dc mul(Vector4dc a, Vector4dc b) {
        return a.mul(b, new Vector4d());
    }

    public static Vector4dc div(Vector4dc a, Vector4dc b) {
        return a.div(b, new Vector4d());
    }

    public static Vector4dc rem(Vector4dc a, Vector4dc b) {
        return new Vector4d(a.x() % b.x(), a.y() % b.y(), a.z() % b.z(), a.w() % b.w());
    }

    public static Vector4dc neg(Vector4dc in) {
        return in.negate(new Vector4d());
    }

    public static double swiz(Vector4dc vec, int idx) {
        return vec.get(idx);
    }
}
