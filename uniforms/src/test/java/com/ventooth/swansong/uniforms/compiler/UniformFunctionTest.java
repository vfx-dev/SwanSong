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
import lombok.val;
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
        val implicitFn = Assertions.assertDoesNotThrow(() -> UniformFunction.builder()
                                                                            .instance(new MockUniformFunctions())
                                                                            .method(MockUniformFunctions.class.getDeclaredMethod(
                                                                                    "indexedMethod",
                                                                                    int.class,
                                                                                    double.class))
                                                                            .indexedImplicit(true)
                                                                            .build());
        Assertions.assertEquals(1, implicitFn.paramTypes().size());
        Assertions.assertEquals("(ID)I", implicitFn.desc());

        val explicitFn = Assertions.assertDoesNotThrow(() -> UniformFunction.builder()
                                                                            .instance(new MockUniformFunctions())
                                                                            .method(MockUniformFunctions.class.getDeclaredMethod(
                                                                                    "indexedMethod",
                                                                                    int.class,
                                                                                    double.class))
                                                                            .indexedExplicit(true)
                                                                            .build());
        Assertions.assertEquals(2, explicitFn.paramTypes().size());
        Assertions.assertEquals("(ID)I", explicitFn.desc());
    }
}
