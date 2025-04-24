/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms.compiler.frontend;

import com.ventooth.swansong.mathparser.AbstractParser;
import com.ventooth.swansong.mathparser.Lexer;
import com.ventooth.swansong.uniforms.compiler.ast.ConstNode;
import com.ventooth.swansong.uniforms.compiler.ast.UntypedNode;
import com.ventooth.swansong.uniforms.compiler.ast.untyped.UntypedBinaryNode;
import com.ventooth.swansong.uniforms.compiler.ast.untyped.UntypedFunctionNode;
import com.ventooth.swansong.uniforms.compiler.ast.untyped.UntypedSwizzleNode;
import com.ventooth.swansong.uniforms.compiler.ast.untyped.UntypedUnaryNode;
import com.ventooth.swansong.uniforms.compiler.ast.untyped.UntypedVarNode;

import java.util.List;

public class UntypedParser extends AbstractParser<UntypedNode> {
    public UntypedParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    protected UntypedNode createFunctionCall(String name, List<UntypedNode> args) {
        return new UntypedFunctionNode(name, args);
    }

    @Override
    protected UntypedNode createVariable(String name) {
        return new UntypedVarNode(name);
    }

    @Override
    protected UntypedNode createBinaryOperation(UntypedNode left, UntypedNode right, Operator operator) {
        return new UntypedBinaryNode(left, right, operator);
    }

    @Override
    protected UntypedNode createIntegerConstant(int value) {
        return ConstNode.Int.of(value);
    }

    @Override
    protected UntypedNode createFloatConstant(double value) {
        return new ConstNode.Float(value);
    }

    @Override
    protected UntypedNode createBoolConstant(boolean value) {
        return ConstNode.Bool.of(value);
    }

    @Override
    protected UntypedNode createUnaryNot(UntypedNode value) {
        return new UntypedUnaryNode(value, UntypedUnaryNode.Op.Not);
    }

    @Override
    protected UntypedNode createUnaryMinus(UntypedNode value) {
        return new UntypedUnaryNode(value, UntypedUnaryNode.Op.Minus);
    }

    @Override
    protected UntypedNode createSwizzle(UntypedNode value, int swizzleIndex) {
        return new UntypedSwizzleNode(value, swizzleIndex);
    }
}
