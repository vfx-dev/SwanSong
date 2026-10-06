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

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface UniformFunctionResolver {
    @Nullable UniformFunction resolve(String name, List<Type> paramTypes);
}
