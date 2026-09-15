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
import lombok.val;
import org.jetbrains.annotations.NotNull;

public class Lexer {
    private final char[] characters;
    private int offset = 0;
    private Token buffer;

    public Lexer(String str) {
        this.characters = str.toCharArray();
    }

    private void expect(char c) throws UnexpectedCharException {
        if (!hasNextChar()) {
            throw new UnexpectedCharException(offset, c, "End of file");
        }
        char c2 = nextChar();
        if (c2 != c) {
            throw new UnexpectedCharException(offset - 1, c, c2);
        }
    }

    public boolean hasNext() throws UnexpectedCharException {
        if (buffer != null) {
            return true;
        }
        //Prefetch
        while (true) {
            if (!hasNextChar()) {
                return false;
            }
            val pc = offset;
            val c = nextChar();
            if (Character.isWhitespace(c)) {
                continue;
            }
            buffer = switch (c) {
                case '+' -> new Token(pc, TokenType.Plus, "+");
                case '-' -> new Token(pc, TokenType.Minus, "-");
                case '*' -> new Token(pc, TokenType.Mul, "*");
                case '/' -> new Token(pc, TokenType.Div, "/");
                case '%' -> new Token(pc, TokenType.Mod, "%");
                case '&' -> {
                    if (!hasNextChar()) {
                        yield new Token(pc, TokenType.And, "&");
                    }
                    val c2 = nextChar();
                    if (c2 != '&') {
                        offset--;
                        yield new Token(pc, TokenType.And, "&");
                    }
                    yield new Token(pc, TokenType.And, "&&");
                }
                case '|' -> {
                    if (!hasNextChar()) {
                        yield new Token(pc, TokenType.Or, "|");
                    }
                    val c2 = nextChar();
                    if (c2 != '|') {
                        offset--;
                        yield new Token(pc, TokenType.Or, "|");
                    }
                    yield new Token(pc, TokenType.Or, "||");
                }
                case '>' -> {
                    if (!hasNextChar()) {
                        yield new Token(pc, TokenType.Greater, ">");
                    }
                    val c2 = nextChar();
                    if (c2 != '=') {
                        offset--;
                        yield new Token(pc, TokenType.Greater, ">");
                    }
                    yield new Token(pc, TokenType.GreaterEqual, ">=");
                }
                case '<' -> {
                    if (!hasNextChar()) {
                        yield new Token(pc, TokenType.Less, "<");
                    }
                    val c2 = nextChar();
                    if (c2 != '=') {
                        offset--;
                        yield new Token(pc, TokenType.Less, "<");
                    }
                    yield new Token(pc, TokenType.LessEqual, "<=");
                }
                case '!' -> {
                    if (!hasNextChar()) {
                        yield new Token(pc, TokenType.Not, "!");
                    }
                    val c2 = nextChar();
                    if (c2 != '=') {
                        offset--;
                        yield new Token(pc, TokenType.Not, "!");
                    }
                    yield new Token(pc, TokenType.NotEqual, "!=");
                }
                case '=' -> {
                    expect('=');
                    yield new Token(pc, TokenType.Equal, "==");
                }
                case ',' -> new Token(pc, TokenType.Comma, ",");
                case '(' -> new Token(pc, TokenType.LeftParen, "(");
                case ')' -> new Token(pc, TokenType.RightParen, ")");
                case '.' -> new Token(pc, TokenType.Dot, ".");
                default -> {
                    if (c >= '0' && c <= '9') {
                        yield tokenizeNumber(pc, c);
                    }
                    if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                        yield tokenizeIdentifier(pc, c);
                    }
                    throw new UnexpectedCharException(offset - 1, "whitespace  +-*/%&|><!=,().  a-z  A-Z  0-9", c);
                }
            };
            return true;
        }
    }

    public @NotNull Token peek() {
        val res = buffer;
        if (res == null) {
            throw new AssertionError();
        }
        return res;
    }

    public @NotNull Token next() {
        val res = buffer;
        if (res == null) {
            throw new AssertionError();
        }
        buffer = null;
        return res;
    }

    private Token tokenizeNumber(int pc, char c) {
        val num = new StringBuilder(32);
        num.append(c);
        boolean hasDot = false;
        while (true) {
            if (!hasNextChar()) {
                return new Token(pc, hasDot ? TokenType.Float : TokenType.Integer, num.toString());
            }
            val c2 = nextChar();
            if (c2 >= '0' && c2 <= '9') {
                num.append(c2);
                continue;
            }
            if (c2 == '.') {
                if (hasDot) {
                    offset--;
                    return new Token(pc, TokenType.Float, num.toString());
                }
                hasDot = true;
                num.append(c2);
                continue;
            }
            offset--;
            return new Token(pc, hasDot ? TokenType.Float : TokenType.Integer, num.toString());
        }
    }

    private Token tokenizeIdentifier(int pc, char c) {
        val ident = new StringBuilder(32);
        ident.append(c);
        while (true) {
            if (!hasNextChar()) {
                return tokenFromIdentifierText(pc, ident);
            }
            val c2 = nextChar();
            if (c2 >= 'a' && c2 <= 'z' || c2 >= 'A' && c2 <= 'Z' || c2 >= '0' && c2 <= '9' || c2 == '_') {
                ident.append(c2);
                continue;
            }
            offset--;
            return tokenFromIdentifierText(pc, ident);
        }
    }

    private Token tokenFromIdentifierText(int pc, StringBuilder b) {
        if ("true".contentEquals(b)) {
            return new Token(pc, TokenType.True, "true");
        } else if ("false".contentEquals(b)) {
            return new Token(pc, TokenType.False, "false");
        }
        return new Token(pc, TokenType.Identifier, b.toString());
    }

    private boolean hasNextChar() {
        return offset < characters.length;
    }

    private char nextChar() {
        return characters[offset++];
    }

    @RequiredArgsConstructor
    public static class UnexpectedCharException extends ParserException {
        public final int at;
        public final String expected;
        public final String got;

        public UnexpectedCharException(int at, String expected, char got) {
            this(at, expected, Character.toString(got));
        }

        public UnexpectedCharException(int at, char expected, char got) {
            this(at, Character.toString(expected), Character.toString(got));
        }

        public UnexpectedCharException(int at, char expected, String got) {
            this(at, Character.toString(expected), got);
        }
    }

}
