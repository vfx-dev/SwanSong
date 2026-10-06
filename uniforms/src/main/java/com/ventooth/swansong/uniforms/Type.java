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

import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2dc;
import org.joml.Vector3dc;
import org.joml.Vector4dc;
import org.objectweb.asm.Opcodes;

import java.util.Locale;

@NoArgsConstructor
public enum Type {
    Bool,
    Int,
    Float,
    Vec2,
    Vec3,
    Vec4;

    // @formatter:off
    private static final String INAME_BOOL  = org.objectweb.asm.Type.getInternalName(CompiledUniform.Bool.class);
    private static final String INAME_INT   = org.objectweb.asm.Type.getInternalName(CompiledUniform.Int.class);
    private static final String INAME_FLOAT = org.objectweb.asm.Type.getInternalName(CompiledUniform.Float.class);
    private static final String INAME_VEC2  = org.objectweb.asm.Type.getInternalName(CompiledUniform.Vec2.class);
    private static final String INAME_VEC3  = org.objectweb.asm.Type.getInternalName(CompiledUniform.Vec3.class);
    private static final String INAME_VEC4  = org.objectweb.asm.Type.getInternalName(CompiledUniform.Vec4.class);

    private static final String DESC_BOOL   = "Z";
    private static final String DESC_INT    = "I";
    private static final String DESC_FLOAT  = "D";
    private static final String DESC_VEC2   = org.objectweb.asm.Type.getDescriptor(Vector2dc.class);
    private static final String DESC_VEC3   = org.objectweb.asm.Type.getDescriptor(Vector3dc.class);
    private static final String DESC_VEC4   = org.objectweb.asm.Type.getDescriptor(Vector4dc.class);
    // @formatter:on

    public String internalTypeName() {
        //noinspection DuplicatedCode
        return switch (this) {
            case Bool -> INAME_BOOL;
            case Int -> INAME_INT;
            case Float -> INAME_FLOAT;
            case Vec2 -> INAME_VEC2;
            case Vec3 -> INAME_VEC3;
            case Vec4 -> INAME_VEC4;
        };
    }

    public String descriptor() {
        //noinspection DuplicatedCode
        return switch (this) {
            case Bool -> DESC_BOOL;
            case Int -> DESC_INT;
            case Float -> DESC_FLOAT;
            case Vec2 -> DESC_VEC2;
            case Vec3 -> DESC_VEC3;
            case Vec4 -> DESC_VEC4;
        };
    }

    public int returnOpcode() {
        return switch (this) {
            case Bool, Int -> Opcodes.IRETURN;
            case Float -> Opcodes.DRETURN;
            case Vec2, Vec3, Vec4 -> Opcodes.ARETURN;
        };
    }


    public static Type of(Class<?> klass) {
        if (boolean.class == klass) {
            return Bool;
        }
        if (int.class == klass) {
            return Int;
        }
        if (double.class == klass) {
            return Float;
        }
        if (Vector2dc.class.isAssignableFrom(klass)) {
            return Vec2;
        }
        if (Vector3dc.class.isAssignableFrom(klass)) {
            return Vec3;
        }
        if (Vector4dc.class.isAssignableFrom(klass)) {
            return Vec4;
        }
        throw new IllegalArgumentException(klass.getName());
    }

    public static Type of(String shaderPackType) {
        return switch (shaderPackType.toLowerCase(Locale.ROOT)) {
            case "bool" -> Bool;
            case "int" -> Int;
            case "float" -> Float;
            case "vec2" -> Vec2;
            case "vec3" -> Vec3;
            case "vec4" -> Vec4;
            default -> throw new IllegalArgumentException();
        };
    }

    public static String methodDescriptor(Type returns, Type... params) {
        val result = new StringBuilder("(");
        for (val param : params) {
            result.append(param.descriptor());
        }
        result.append(")");
        result.append(returns.descriptor());
        return result.toString();
    }

    public static Type coerce(Type a, Type b) throws TypeCoercionException {
        val ret = tryCoerce(a, b);
        if (ret == null) {
            throw new TypeCoercionException();
        }
        return ret;
    }

    public static @Nullable Type tryCoerce(Type a, Type b) {
        return switch (a) {
            case Bool -> switch (b) {
                case Bool -> a;
                default -> b;
            };
            case Int -> switch (b) {
                case Bool, Int -> a;
                default -> b;
            };
            case Float -> switch (b) {
                case Bool, Int, Float -> a;
                default -> b;
            };
            case Vec2 -> switch (b) {
                case Bool, Int, Float, Vec2 -> a;
                default -> null;
            };
            case Vec3 -> switch (b) {
                case Bool, Int, Float, Vec3 -> a;
                default -> null;
            };
            case Vec4 -> switch (b) {
                case Bool, Int, Float, Vec4 -> a;
                default -> null;
            };
            default -> null;
        };
    }

    public static class TypeCoercionException extends RuntimeException {

    }
}
