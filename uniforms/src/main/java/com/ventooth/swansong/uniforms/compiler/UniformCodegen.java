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

import com.ventooth.swansong.uniforms.CompiledUniform;
import com.ventooth.swansong.uniforms.UniformCarrier;
import com.ventooth.swansong.uniforms.UniformDef;
import com.ventooth.swansong.uniforms.UniformFunction;
import com.ventooth.swansong.uniforms.UniformFunctionProvider;
import com.ventooth.swansong.uniforms.UniformFunctionRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class UniformCodegen {
    // TODO: We hold onto the providers so that we can wire the reset()/update() functions from the carrier
    protected final Map<String, UniformFunctionProvider> valueProviders = new HashMap<>();

    public void register(UniformFunctionProvider provider) {
        val name = provider.name();
        if (valueProviders.containsKey(name)) {
            throw new IllegalStateException();
        }

        // TODO: Context-sensitive registry where instanced methods with `this` would resolve relative to the `provider`
        provider.init(new UniformFunctionRegistry() {
            @Override
            public void pure(Method method) {

            }

            @Override
            public void pure(Method method, String... names) {

            }

            @Override
            public void impure(Method method) {

            }

            @Override
            public void impure(Method method, String... names) {

            }

            @Override
            public void statefulIndexed(Method method) {

            }

            @Override
            public void statefulIndexed(Method method, String... names) {

            }

            @Override
            public void addWithNames(UniformFunction uni, String... names) {

            }
        });

        valueProviders.put(name, provider);
    }

    public Result generate(List<UniformDef> definitions) {
        return new Result(Collections.emptyMap(), new UniformCarrier() {
            // TODO: This thing will be code genned
            @Override
            public void reset() {
            }

            @Override
            public void update() {
            }
        }, Collections.emptyList());
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Result {
        public final Map<String, CompiledUniform> uniforms;
        public final UniformCarrier carrier;
        public final List<String> errors;
    }
}