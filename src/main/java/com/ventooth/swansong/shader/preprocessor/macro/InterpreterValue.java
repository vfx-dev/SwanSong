/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.preprocessor.macro;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

@ApiStatus.NonExtendable
public /*sealed*/ interface InterpreterValue {
    boolean asBool();

    //TODO convert to record
    static final class IntValue implements InterpreterValue {
        private final int value;

        public IntValue(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }

        @Override
        public boolean asBool() {
            return value != 0;
        }

        public int value() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (IntValue) obj;
            return this.value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

    }

    //TODO convert to record
    static final class DoubleValue implements InterpreterValue {
        private final double value;

        public DoubleValue(double value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }

        @Override
        public boolean asBool() {
            return value != 0d;
        }

        public double value() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (DoubleValue) obj;
            return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

    }
}
