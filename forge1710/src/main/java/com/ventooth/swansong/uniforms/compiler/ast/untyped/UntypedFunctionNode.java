/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms.compiler.ast.untyped;

import com.ventooth.swansong.uniforms.compiler.ast.UntypedNode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class UntypedFunctionNode implements UntypedNode {
    public final String name;
    public final List<UntypedNode> params;
}
