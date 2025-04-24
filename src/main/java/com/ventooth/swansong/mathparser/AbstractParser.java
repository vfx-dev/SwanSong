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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@RequiredArgsConstructor
public abstract class AbstractParser<Node> {
    protected final Lexer lexer;

    public Node parse() throws ParserException {
        if (!lexer.hasNext()) {
            throw new UnexpectedEndOfTokenStreamException("Nothing to parse");
        }
        return parseExpr();
    }

    protected Node parseExpr() throws ParserException {
        if (!lexer.hasNext()) {
            return null;
        }
        var primaries = new ArrayList<Node>();
        var operators = new ArrayList<Operator>();
        while (true) {
            if (!lexer.hasNext()) {
                throw new UnexpectedEndOfTokenStreamException("Unterminated binary operation");
            }
            try {
                primaries.add(parsePrimaryExpr());
            } catch (UnexpectedTokenException e) {
                e.add(TokenType.Mul,
                      TokenType.Div,
                      TokenType.Mod,
                      TokenType.Plus,
                      TokenType.Minus,
                      TokenType.GreaterEqual,
                      TokenType.Greater,
                      TokenType.LessEqual,
                      TokenType.Less,
                      TokenType.Equal,
                      TokenType.NotEqual,
                      TokenType.And,
                      TokenType.Or);
                throw e;
            }
            if (!lexer.hasNext()) {
                break;
            }
            val op = switch (lexer.peek()
                                  .type()) {
                case Mul -> Operator.Mul;
                case Div -> Operator.Div;
                case Mod -> Operator.Rem;
                case Plus -> Operator.Add;
                case Minus -> Operator.Sub;
                case GreaterEqual -> Operator.Ge;
                case Greater -> Operator.Gt;
                case LessEqual -> Operator.Le;
                case Less -> Operator.Lt;
                case Equal -> Operator.Eq;
                case NotEqual -> Operator.Ne;
                case And -> Operator.And;
                case Or -> Operator.Or;
                default -> null;
            };
            if (op == null) {
                break;
            }
            operators.add(op);
            lexer.next();
        }
        if (primaries.size() == 1) {
            return primaries.get(0);
        }
        return evaluatePrecedence(primaries, operators);
    }

    protected Node evaluatePrecedence(List<Node> primaries, List<Operator> operators) throws ParserException {
        val opSize = operators.size();
        val primSize = primaries.size();
        if (primSize != opSize + 1) {
            throw new AssertionError();
        }
        if (opSize == 0) {
            return primaries.get(0);
        }
        int current = 0;
        int index = -1;
        for (int i = 0; i < opSize; i++) {
            val op = operators.get(i);
            if (op.precedence > current) {
                index = i;
                current = op.precedence;
            }
        }
        val left = evaluatePrecedence(primaries.subList(0, index + 1), operators.subList(0, index));
        val right = evaluatePrecedence(primaries.subList(index + 1, primSize), operators.subList(index + 1, opSize));
        return createBinaryOperation(left, right, operators.get(index));
    }

    protected Node parsePrimaryExpr() throws ParserException {
        val token = lexer.next();
        return switch (token.type()) {
            case Integer -> createIntegerConstant(Integer.parseInt(token.text()));
            case Float -> createFloatConstant(Double.parseDouble(token.text()));
            case True -> createBoolConstant(true);
            case False -> createBoolConstant(false);
            case Identifier -> parseFunOrVar(token.text());
            case LeftParen -> {
                val expr = parseExpr();
                if (!lexer.hasNext()) {
                    throw new UnexpectedEndOfTokenStreamException("Unterminated parenthesis");
                }
                val tok = lexer.next();
                if (tok.type() != TokenType.RightParen) {
                    throw new UnexpectedTokenException(TokenType.RightParen, tok);
                }
                yield expr;
            }
            case Minus -> {
                if (!lexer.hasNext()) {
                    throw new UnexpectedEndOfTokenStreamException("Dangling minus sign");
                }
                val expr = parsePrimaryExpr();
                yield createUnaryMinus(expr);
            }
            case Not -> {
                if (!lexer.hasNext()) {
                    throw new UnexpectedEndOfTokenStreamException("Dangling not sign");
                }
                val expr = parsePrimaryExpr();
                yield createUnaryNot(expr);
            }
            default -> throw new UnexpectedTokenException(Arrays.asList(TokenType.Integer,
                                                                        TokenType.Float,
                                                                        TokenType.True,
                                                                        TokenType.False,
                                                                        TokenType.Identifier,
                                                                        TokenType.LeftParen,
                                                                        TokenType.Not,
                                                                        TokenType.Minus), token);
        };
    }

    protected Node parseFunOrVar(String name) throws ParserException {
        if (lexer.hasNext()) {
            val lookahead = lexer.peek();
            if (lookahead.type() == TokenType.Dot) {
                val variable = createVariable(name);
                return parseSwizzle(variable);
            }
            if (lookahead.type() == TokenType.LeftParen) {
                lexer.next();
                return parseFun(name);
            }
        }
        return createVariable(name);
    }

    protected Node parseSwizzle(Node value) throws ParserException {
        while (lexer.hasNext() &&
               lexer.peek()
                    .type() == TokenType.Dot) {
            lexer.next();
            if (!lexer.hasNext()) {
                throw new UnexpectedEndOfTokenStreamException("Dangling dot for swizzling");
            }
            val swizzleToken = lexer.next();
            value = switch (swizzleToken.type()) {
                case Integer -> {
                    val idx = Integer.parseInt(swizzleToken.text());
                    yield createSwizzle(value, idx);
                }
                case Identifier -> {
                    val txt = swizzleToken.text();
                    yield createSwizzle(value, switch (txt) {
                        case "x", "s", "r" -> 0;
                        case "y", "t", "g" -> 1;
                        case "z", "p", "b" -> 2;
                        case "w", "q", "a" -> 3;
                        default -> throw new ParserException("Unknown swizzle index \"" +
                                                             txt +
                                                             "\". Must be a number, or one of [x, y, z, w] or [s, t, p, q] or [r, g, b, a]");
                    });
                }
                default -> throw new UnexpectedTokenException(Arrays.asList(TokenType.Integer, TokenType.Identifier),
                                                              swizzleToken);
            };
        }
        return value;
    }

    protected Node parseFun(String name) throws ParserException {
        var args = new ArrayList<Node>();
        boolean first = true;
        while (true) {
            if (!lexer.hasNext()) {
                throw new UnexpectedEndOfTokenStreamException("Unterminated function call");
            }
            val lookaheadToken = lexer.peek();
            val lookahead = lookaheadToken.type();
            if (lookahead == TokenType.RightParen) {
                lexer.next();
                return createFunctionCall(name, args);
            }
            if (first) {
                first = false;
            } else if (lookahead != TokenType.Comma) {
                throw new UnexpectedTokenException(TokenType.Comma, lookaheadToken);
            } else {
                lexer.next();
            }
            args.add(parseExpr());
        }
    }

    protected abstract Node createFunctionCall(String name, List<Node> args) throws ParserException;

    protected abstract Node createVariable(String name) throws ParserException;

    protected abstract Node createBinaryOperation(Node left, Node right, Operator operator) throws ParserException;

    protected abstract Node createIntegerConstant(int value) throws ParserException;

    protected abstract Node createFloatConstant(double value) throws ParserException;

    protected abstract Node createBoolConstant(boolean value) throws ParserException;

    protected abstract Node createUnaryNot(Node value) throws ParserException;

    protected abstract Node createUnaryMinus(Node value) throws ParserException;

    protected abstract Node createSwizzle(Node value, int swizzleIndex) throws ParserException;

    @RequiredArgsConstructor
    public enum Operator {
        Mul(3),
        Div(3),
        Rem(3),
        Add(4),
        Sub(4),
        Ge(6),
        Gt(6),
        Le(6),
        Lt(6),
        Eq(7),
        Ne(7),
        And(11),
        Or(12);
        public final int precedence;
    }


    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UnexpectedTokenException extends ParserException {
        public Set<TokenType> expected;
        public final Token got;

        public UnexpectedTokenException(TokenType expected, Token got) {
            this(Collections.singleton(expected), got);
        }

        public UnexpectedTokenException(Collection<TokenType> expected, Token got) {
            this(expected instanceof TreeSet<TokenType> ts ? ts : new TreeSet<>(expected), got);
        }

        public void add(TokenType... expected) {
            val newEx = new TreeSet<>(this.expected);
            newEx.addAll(Arrays.asList(expected));
            this.expected = newEx;
        }
    }

    public static class UnexpectedEndOfTokenStreamException extends ParserException {
        public UnexpectedEndOfTokenStreamException(String message) {
            super(message);
        }
    }
}
