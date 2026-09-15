/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms.compiler.ast.typed;

import com.ventooth.swansong.uniforms.Type;
import com.ventooth.swansong.uniforms.compiler.ast.TypedNode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class TypedMathNode implements TypedNode {
    public final TypedNode left;
    public final TypedNode right;
    public final Op op;

    @Override
    public @NotNull Type outputType() {
        return left.outputType();
    }

    public enum Op {
        Add,
        Sub,
        Mul,
        Div,
        Rem
    }
}
