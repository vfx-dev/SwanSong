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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public sealed interface UniformFunctionLookup {
    @Nullable UniformFunction lookup(String name, List<Type> paramTypes);

    @NoArgsConstructor
    final class Single implements UniformFunctionLookup {
        private final Map<String, List<UniformFunction>> fnMap = new HashMap<>();

        public UniformFunction pure(Method method) {
            return pure(method, method.getName());
        }

        public UniformFunction pure(Method method, String... names) {
            return add(UniformFunction.builder()
                                      .method(method)
                                      .pure(true)
                                      .build(), names);
        }

        public UniformFunction impure(Method method) {
            return impure(method, method.getName());
        }

        public UniformFunction impure(Method method, String... names) {
            return add(UniformFunction.builder()
                                      .method(method)
                                      .build(), names);
        }

        public UniformFunction impure(Object instance, Method method) {
            return impure(instance, method, method.getName());
        }

        public UniformFunction impure(Object instance, Method method, String... names) {
            return add(UniformFunction.builder()
                                      .instance(instance)
                                      .method(method)
                                      .build(), names);
        }

        public UniformFunction idxExplicit(Method method) {
            return idxExplicit(method, method.getName());
        }

        public UniformFunction idxExplicit(Method method, String... names) {
            return add(UniformFunction.builder()
                                      .indexedExplicit(true)
                                      .method(method)
                                      .build(), names);
        }

        public UniformFunction idxExplicit(Object instance, Method method) {
            return idxExplicit(instance, method, method.getName());
        }

        public UniformFunction idxExplicit(Object instance, Method method, String... names) {
            return add(UniformFunction.builder()
                                      .instance(instance)
                                      .method(method)
                                      .indexedExplicit(true)
                                      .build(), names);
        }

        public UniformFunction idxImplicit(Method method) {
            return idxImplicit(method, method.getName());
        }

        public UniformFunction idxImplicit(Method method, String... names) {
            return add(UniformFunction.builder()
                                      .method(method)
                                      .indexedImplicit(true)
                                      .build(), names);
        }

        public UniformFunction idxImplicit(Object instance, Method method) {
            return idxImplicit(instance, method, method.getName());
        }

        public UniformFunction idxImplicit(Object instance, Method method, String... names) {
            return add(UniformFunction.builder()
                                      .instance(instance)
                                      .method(method)
                                      .indexedImplicit(true)
                                      .build(), names);
        }

        public UniformFunction add(UniformFunction fn, String... names) {
            for (val name : names) {
                add(fn, name);
            }
            return fn;
        }

        public UniformFunction add(UniformFunction fn, String name) {
            fnMap.computeIfAbsent(name, _ -> new ArrayList<>())
                 .add(fn);
            return fn;
        }

        @Override
        public @Nullable UniformFunction lookup(String name, List<Type> paramTypes) {
            val fnList = fnMap.get(name);
            if (fnList == null) {
                return null;
            }

            for (val fn : fnList) {
                if (fn.paramTypes()
                      .equals(paramTypes)) {
                    return fn;
                }
            }

            outer:
            for (val fn : fnList) {
                val fnParamTypes = fn.paramTypes();
                val numParams = paramTypes.size();
                if (fnParamTypes.size() != numParams) {
                    continue;
                }

                // Try coercion
                for (var i = 0; i < numParams; i++) {
                    val fnParamType = fnParamTypes.get(i);
                    val coerced = Type.tryCoerce(paramTypes.get(i), fnParamType);
                    if (coerced != fnParamType) {
                        continue outer;
                    }
                }
                return fn;
            }
            return null;
        }
    }

    @NoArgsConstructor
    final class Multi implements UniformFunctionLookup {
        private final List<UniformFunctionLookup> lookups = new ArrayList<>();

        public void add(UniformFunctionLookup lookup) {
            lookups.add(lookup);
        }

        @Override
        public @Nullable UniformFunction lookup(String name, List<Type> paramTypes) {
            for (val lookup : lookups) {
                val fn = lookup.lookup(name, paramTypes);
                if (fn != null) {
                    return fn;
                }
            }
            return null;
        }
    }
}
