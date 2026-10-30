/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader;

import com.ventooth.swansong.uniforms.CompiledUniform;
import com.ventooth.swansong.uniforms.Type;
import com.ventooth.swansong.uniforms.UniformDef;
import com.ventooth.swansong.uniforms.compiler.UniformCodegen;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ShaderStateTest {
    @Test
    void customUniformsCallPlatformOverrides() {
        var state = new ShaderState() {
            @Override
            public double farPlane() {
                return 128;
            }
        };
        var codegen = new UniformCodegen();
        codegen.register(state.uniforms()
                              .funcLookup());

        var result = codegen.generate(List.of(new UniformDef("doubleFar", Type.Float, "far * 2.0", true)));
        Assertions.assertTrue(result.errors.isEmpty(), result.errors::toString);
        result.carrier.update();
        Assertions.assertEquals(256, ((CompiledUniform.Float) result.uniforms.get("doubleFar")).value(), 1e-9);
    }
}
