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

import java.lang.reflect.Method;

public interface UniformFunctionRegistry {
    UniformFunction pure(Method method);

    UniformFunction pure(Method method, String... names);

    UniformFunction impure(Method method);

    UniformFunction impure(Method method, String... names);

    UniformFunction impure(Object instance, Method method);

    UniformFunction impure(Object instance, Method method, String... names);

    UniformFunction idxExplicit(Method method);

    UniformFunction idxExplicit(Method method, String... names);

    UniformFunction idxExplicit(Object instance, Method method);

    UniformFunction idxExplicit(Object instance, Method method, String... names);

    UniformFunction idxImplicit(Method method);

    UniformFunction idxImplicit(Method method, String... names);

    UniformFunction idxImplicit(Object instance, Method method);

    UniformFunction idxImplicit(Object instance, Method method, String... names);

    UniformFunction add(UniformFunction fn, String... names);

    UniformFunction add(UniformFunction fn, String name);
}
