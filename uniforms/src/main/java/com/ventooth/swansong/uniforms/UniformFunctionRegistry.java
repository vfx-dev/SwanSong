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
    void pure(Method method);

    void pure(Method method, String... names);

    void impure(Method method);

    void impure(Method method, String... names);

    void statefulIndexed(Method method);

    void statefulIndexed(Method method, String... names);

    void addWithNames(UniformFunctionOld uni, String... names);
}
