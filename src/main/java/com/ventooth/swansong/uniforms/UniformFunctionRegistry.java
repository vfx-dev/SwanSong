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
import java.util.Objects;

public abstract class UniformFunctionRegistry {
    protected abstract @Nullable Iterable<UniformFunction> registeredMethods(String name);

    public UniformFunction resolve(String name, List<Type> paramTypes) {
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

    public static class Single extends UniformFunctionRegistry {
        private final Map<String, List<UniformFunction>> registeredMethods = new HashMap<>();

        public void pure(Method method) {
            addWithNames(UniformFunction.of(method, true, false), method.getName());
        }

        public void pure(Method method, String... names) {
            addWithNames(UniformFunction.of(method, true, false), names);
        }

        public void impure(Method method) {
            addWithNames(UniformFunction.of(method, false, false), method.getName());
        }

        public void impure(Method method, String... names) {
            addWithNames(UniformFunction.of(method, false, false), names);
        }

        public void statefulIndexed(Method method) {
            addWithNames(UniformFunction.of(method, false, true), method.getName());
        }

        public void statefulIndexed(Method method, String... names) {
            addWithNames(UniformFunction.of(method, false, true), names);
        }

        public void addWithNames(UniformFunction uni, String... names) {
            for (val name : names) {
                registeredMethods.computeIfAbsent(name, ignored -> new ArrayList<>())
                                 .add(uni);
            }
        }

        @Override
        protected Iterable<UniformFunction> registeredMethods(String name) {
            return registeredMethods.getOrDefault(name, Collections.emptyList());
        }
    }

    public static class Multi extends UniformFunctionRegistry {
        private final List<UniformFunctionRegistry> subRegistries = new ArrayList<>();

        @Override
        protected Iterable<UniformFunction> registeredMethods(String name) {
            return new MultiIterable(name, subRegistries);
        }

        public void add(UniformFunctionRegistry subRegistry) {
            subRegistries.add(subRegistry);
        }

        //TODO convert to record
        private static final class MultiIterable implements Iterable<UniformFunction> {
            private final String name;
            private final Iterable<UniformFunctionRegistry> subIterables;

            private MultiIterable(String name, Iterable<UniformFunctionRegistry> subIterables) {
                this.name = name;
                this.subIterables = subIterables;
            }

            @Override
            public @NotNull Iterator<UniformFunction> iterator() {
                return new MultiIterator(name, subIterables.iterator());
            }

            public String name() {
                return name;
            }

            public Iterable<UniformFunctionRegistry> subIterables() {
                return subIterables;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (MultiIterable) obj;
                return Objects.equals(this.name, that.name) && Objects.equals(this.subIterables, that.subIterables);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, subIterables);
            }

            @Override
            public String toString() {
                return "MultiIterable[" + "name=" + name + ", " + "subIterables=" + subIterables + ']';
            }


            @RequiredArgsConstructor
            private static class MultiIterator implements Iterator<UniformFunction> {
                private final String name;
                private final Iterator<UniformFunctionRegistry> subIterables;
                private Iterator<UniformFunction> current = null;

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
                public UniformFunction next() {
                    return current.next();
                }
            }
        }
    }
}
