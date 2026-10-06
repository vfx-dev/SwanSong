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

// TODO: Invert `exposed`, as it currently means 'generate getter'
//  Flipping it makes it more clear, as in a flag to NOT generate a getter!
//  eg: !exposed -> isVariable
public record UniformDef(String name, Type type, String expression, boolean exposed) {}
