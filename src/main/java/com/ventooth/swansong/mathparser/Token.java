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


import java.util.Objects;

//TODO convert to record
public final class Token {
    private final int offset;
    private final TokenType type;
    private final String text;

    public Token(int offset, TokenType type, String text) {
        this.offset = offset;
        this.type = type;
        this.text = text;
    }

    public int until() {
        return offset + text.length();
    }

    public int offset() {
        return offset;
    }

    public TokenType type() {
        return type;
    }

    public String text() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (Token) obj;
        return this.offset == that.offset &&
               Objects.equals(this.type, that.type) &&
               Objects.equals(this.text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, type, text);
    }

    @Override
    public String toString() {
        return "Token[" + "offset=" + offset + ", " + "type=" + type + ", " + "text=" + text + ']';
    }

}
