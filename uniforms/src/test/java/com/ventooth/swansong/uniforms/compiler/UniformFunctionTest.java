/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms.compiler;

import com.ventooth.swansong.uniforms.UniformFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UniformFunctionTest {
    @Test
    void simpleTest() {
        // TODO: Expand tests
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> UniformFunction.builder()
                                                     .method(MockUniformFunctions.class.getDeclaredMethod("methodA"))
                                                     .build());
        Assertions.assertDoesNotThrow(() -> UniformFunction.builder()
                                                           .method(MockUniformFunctions.class.getDeclaredMethod(
                                                                   "methodB"))
                                                           .build());
        Assertions.assertDoesNotThrow(() -> UniformFunction.builder()
                                                           .instance(new MockUniformFunctions())
                                                           .method(MockUniformFunctions.class.getDeclaredMethod(
                                                                   "methodC"))
                                                           .build());
    }
}
