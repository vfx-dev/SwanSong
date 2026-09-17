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

import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public record UniformFunction(@Nullable Method constantFoldMethod,
                              String javaOwner,
                              String javaName,
                              Type returns,
                              List<Type> params,
                              boolean statefulIndexed) {

    public static UniformFunction of(Method method, boolean constantFoldable, boolean statefulIndexed) {
        val returns = Type.of(method.getReturnType());
        val javaParams = method.getParameterTypes();
        val params = new ArrayList<Type>(javaParams.length);
        for (val javaParam : javaParams) {
            params.add(Type.of(javaParam));
        }
        return new UniformFunction(constantFoldable ? method : null,
                                   org.objectweb.asm.Type.getInternalName(method.getDeclaringClass()),
                                   method.getName(),
                                   returns,
                                   params,
                                   statefulIndexed);
    }

}
