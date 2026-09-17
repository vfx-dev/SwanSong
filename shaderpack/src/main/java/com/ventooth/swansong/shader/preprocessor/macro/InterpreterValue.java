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

public sealed interface InterpreterValue {
    boolean asBool();

    record IntValue(int value) implements InterpreterValue {

        @Override
        public boolean asBool() {
            return value != 0;
        }
    }

    record DoubleValue(double value) implements InterpreterValue {

        @Override
        public boolean asBool() {
            return value != 0d;
        }
    }
}
