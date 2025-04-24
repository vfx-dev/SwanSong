/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mathparser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TokenType {
    Plus("+"),
    Minus("-"),
    Mul("*"),
    Div("/"),
    Mod("%"),
    Not("!"),
    And("&&"),
    Or("||"),
    GreaterEqual(">="),
    Greater(">"),
    LessEqual("<="),
    Less("<"),
    Equal("=="),
    NotEqual("!="),
    Comma(","),
    LeftParen("("),
    RightParen(")"),
    Dot("."),
    Integer("an integer"),
    Float("a float"),
    False("false"),
    True("true"),
    Identifier("an identifier");

    public final String readable;


    @Override
    public String toString() {
        return readable;
    }
}
