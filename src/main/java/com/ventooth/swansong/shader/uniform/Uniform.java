/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.uniform;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.joml.Matrix4dc;
import org.joml.Vector2dc;
import org.joml.Vector2ic;
import org.joml.Vector3dc;
import org.joml.Vector4dc;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.function.Supplier;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true,
           chain = false)
public abstract class Uniform<T> {
    @Getter
    protected final String name;

    protected int lastLocation = -1;

    // TODO: This is meant to have logic which can cache the last state, to avoid "redundant" uploads.
    //       Last impl kinda sucked, so I yeeted it for the time being.
    public void update() {
        if (lastLocation != -1) {
            load(lastLocation);
        }
    }

    public abstract T value();

    public abstract void load(int location);

    public void reset() {
        lastLocation = -1;
    }

    // region Setters

    // Literally here only because we don't have LWJGL 3 uh mem stack thing...
    private static final FloatBuffer tempFloatBuf = BufferUtils.createFloatBuffer(16);

    @FunctionalInterface
    public interface Setter<T> {
        void set(int location, T value);
    }

    @FunctionalInterface
    public interface BooleanSetter extends Setter<Boolean> {
        @Override
        @Deprecated
        default void set(int location, Boolean value) {
            setBoolean(location, value);
        }

        void setBoolean(int location, boolean value);
    }

    @FunctionalInterface
    public interface IntSetter extends Setter<Integer> {
        @Override
        @Deprecated
        default void set(int location, Integer value) {
            setInt(location, value);
        }

        void setInt(int location, int value);
    }

    @FunctionalInterface
    public interface FloatSetter extends Setter<Float> {
        @Override
        @Deprecated
        default void set(int location, Float value) {
            setFloat(location, value);
        }

        void setFloat(int location, float value);
    }

    @FunctionalInterface
    public interface DoubleSetter extends Setter<Double> {
        @Override
        @Deprecated
        default void set(int location, Double value) {
            setDouble(location, value);
        }

        void setDouble(int location, double value);
    }

    public static void set(int location, boolean v) {
        GL20.glUniform1i(location, v ? 1 : 0);
    }

    public static void set(int location, int v) {
        GL20.glUniform1i(location, v);
    }

    public static void set(int location, Vector2ic v) {
        GL20.glUniform2i(location, v.x(), v.y());
    }

    public static void set(int location, float v) {
        GL20.glUniform1f(location, v);
    }

    public static void set(int location, double v) {
        GL20.glUniform1f(location, (float) v);
    }

    public static void set(int location, Vector3dc v) {
        GL20.glUniform3f(location, (float) v.x(), (float) v.y(), (float) v.z());
    }

    public static void set(int location, Vector2dc v) {
        GL20.glUniform2f(location, (float) v.x(), (float) v.y());
    }

    public static void set(int location, Vector4dc v) {
        GL20.glUniform4f(location, (float) v.x(), (float) v.y(), (float) v.z(), (float) v.w());
    }

    public static void set(int location, Matrix4dc v) {
        v.get(tempFloatBuf);
        GL20.glUniformMatrix4(location, false, tempFloatBuf);
    }

    // endregion

    // region Getters
    @FunctionalInterface
    public interface BooleanSupplier extends Supplier<Boolean> {
        @Override
        @Deprecated
        default Boolean get() {
            return getBoolean();
        }

        boolean getBoolean();
    }

    @FunctionalInterface
    public interface IntSupplier extends Supplier<Integer> {
        @Override
        @Deprecated
        default Integer get() {
            return getInt();
        }

        int getInt();
    }

    @FunctionalInterface
    public interface FloatSupplier extends Supplier<Float> {
        @Override
        @Deprecated
        default Float get() {
            return getFloat();
        }

        float getFloat();
    }

    @FunctionalInterface
    public interface DoubleSupplier extends Supplier<Double> {
        @Override
        @Deprecated
        default Double get() {
            return getDouble();
        }

        double getDouble();
    }
    // endregion

    // region Subtypes
    public static class OfBoolean extends Uniform<Boolean> {
        private final BooleanSupplier getter;
        private final BooleanSetter setter;

        public OfBoolean(String name, BooleanSupplier getter, BooleanSetter setter) {
            super(name);
            this.getter = getter;
            this.setter = setter;
        }

        @Deprecated
        @Override
        public Boolean value() {
            return getter.getBoolean();
        }

        public boolean booleanValue() {
            return getter.getBoolean();
        }

        @Override
        public void load(int location) {
            val value = booleanValue();
            setter.setBoolean(location, value);
            lastLocation = location;
        }
    }

    public static class OfInt extends Uniform<Integer> {
        private final IntSupplier getter;
        private final IntSetter setter;

        public OfInt(String name, IntSupplier getter, IntSetter setter) {
            super(name);
            this.getter = getter;
            this.setter = setter;
        }

        @Deprecated
        @Override
        public Integer value() {
            return getter.getInt();
        }

        public int intValue() {
            return getter.getInt();
        }

        @Override
        public void load(int location) {
            val value = intValue();
            setter.setInt(location, value);
            lastLocation = location;
        }
    }

    public static class OfFloat extends Uniform<Float> {
        private final FloatSupplier getter;
        private final FloatSetter setter;

        public OfFloat(String name, FloatSupplier getter, FloatSetter setter) {
            super(name);
            this.getter = getter;
            this.setter = setter;
        }

        @Deprecated
        @Override
        public Float value() {
            return getter.getFloat();
        }

        public float floatValue() {
            return getter.getFloat();
        }

        @Override
        public void load(int location) {
            val value = floatValue();
            setter.setFloat(location, value);
            lastLocation = location;
        }
    }

    public static class OfDouble extends Uniform<Double> {
        private final DoubleSupplier getter;
        private final DoubleSetter setter;

        public OfDouble(String name, DoubleSupplier getter, DoubleSetter setter) {
            super(name);
            this.getter = getter;
            this.setter = setter;
        }

        @Deprecated
        @Override
        public Double value() {
            return getter.getDouble();
        }

        public double doubleValue() {
            return getter.getDouble();
        }

        @Override
        public void load(int location) {
            val value = doubleValue();
            setter.setDouble(location, value);
            lastLocation = location;
        }
    }

    public static class Of<T> extends Uniform<T> {
        private final Supplier<T> getter;
        private final Setter<T> setter;

        public Of(String name, Supplier<T> getter, Setter<T> setter) {
            super(name);
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public T value() {
            return getter.get();
        }

        @Override
        public void load(int location) {
            val value = value();
            setter.set(location, value);
            lastLocation = location;
        }
    }
    // endregion
}
