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

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class UniformFunctionRegistryOld {
    protected abstract @Nullable Iterable<UniformFunctionOld> registeredMethods(String name);

    public UniformFunctionOld resolve(String name, List<Type> paramTypes) {
        val methods = registeredMethods(name);
        if (methods == null) {
            return null;
        }
        for (val method : methods) {
            if (method.params()
                      .equals(paramTypes)) {
                return method;
            }
        }
        // type coercion lookup
        outer:
        for (val method : methods) {
            val methodParams = method.params();
            if (methodParams.size() != paramTypes.size()) {
                continue;
            }
            for (int i = 0; i < methodParams.size(); i++) {
                val methodParam = methodParams.get(i);
                val coerced = Type.tryCoerce(paramTypes.get(i), methodParam);
                if (coerced != methodParam) {
                    continue outer;
                }
            }
            return method;
        }
        return null;
    }

    public static class Single extends UniformFunctionRegistryOld {
        private final Map<String, List<UniformFunctionOld>> registeredMethods = new HashMap<>();

        public void pure(Method method) {
            addWithNames(UniformFunctionOld.of(method, true, false), method.getName());
        }

        public void pure(Method method, String... names) {
            addWithNames(UniformFunctionOld.of(method, true, false), names);
        }

        public void impure(Method method) {
            addWithNames(UniformFunctionOld.of(method, false, false), method.getName());
        }

        public void impure(Method method, String... names) {
            addWithNames(UniformFunctionOld.of(method, false, false), names);
        }

        public void statefulIndexed(Method method) {
            addWithNames(UniformFunctionOld.of(method, false, true), method.getName());
        }

        public void statefulIndexed(Method method, String... names) {
            addWithNames(UniformFunctionOld.of(method, false, true), names);
        }

        public void addWithNames(UniformFunctionOld uni, String... names) {
            for (val name : names) {
                registeredMethods.computeIfAbsent(name, ignored -> new ArrayList<>())
                                 .add(uni);
            }
        }

        @Override
        protected Iterable<UniformFunctionOld> registeredMethods(String name) {
            return registeredMethods.getOrDefault(name, Collections.emptyList());
        }
    }

    public static class Multi extends UniformFunctionRegistryOld {
        private final List<UniformFunctionRegistryOld> subRegistries = new ArrayList<>();

        @Override
        protected Iterable<UniformFunctionOld> registeredMethods(String name) {
            return new MultiIterable(name, subRegistries);
        }

        public void add(UniformFunctionRegistryOld subRegistry) {
            subRegistries.add(subRegistry);
        }

        private record MultiIterable(String name,
                                     Iterable<UniformFunctionRegistryOld> subIterables) implements Iterable<UniformFunctionOld> {

            @Override
            public @NotNull Iterator<UniformFunctionOld> iterator() {
                return new MultiIterator(name, subIterables.iterator());
            }

            @RequiredArgsConstructor
            private static class MultiIterator implements Iterator<UniformFunctionOld> {
                private final String name;
                private final Iterator<UniformFunctionRegistryOld> subIterables;
                private Iterator<UniformFunctionOld> current = null;

                @Override
                public boolean hasNext() {
                    outer:
                    while (true) {
                        while (current == null) {
                            if (!subIterables.hasNext()) {
                                break outer;
                            }
                            val subReg = subIterables.next();
                            if (subReg == null) {
                                continue;
                            }
                            val methods = subReg.registeredMethods(name);
                            if (methods == null) {
                                continue;
                            }
                            current = methods.iterator();
                        }
                        val hasNext = current.hasNext();
                        if (hasNext) {
                            return true;
                        }
                        current = null;
                    }
                    return false;
                }

                @Override
                public UniformFunctionOld next() {
                    return current.next();
                }
            }
        }
    }
}
