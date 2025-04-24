/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.preprocessor.macro;

import com.ventooth.swansong.mathparser.AbstractParser;
import com.ventooth.swansong.mathparser.Lexer;
import com.ventooth.swansong.mathparser.ParserException;
import com.ventooth.swansong.mathparser.TokenType;
import com.ventooth.swansong.shader.preprocessor.Option;
import lombok.val;

import java.util.List;
import java.util.Map;

public class MacroExpressionInterpreter extends AbstractParser<InterpreterValue> {
    private final Map<String, Option.Value> defines;

    public static InterpreterValue interpret(String code, Map<String, Option.Value> defines) throws ParserException {
        int commentIndex = code.indexOf("//");
        if (commentIndex >= 0) {
            code = code.substring(0, commentIndex);
        }
        return new MacroExpressionInterpreter(new Lexer(code), defines).parse();
    }

    private MacroExpressionInterpreter(Lexer lexer, Map<String, Option.Value> defines) {
        super(lexer);
        this.defines = defines;
    }

    @Override
    protected InterpreterValue createFunctionCall(String name, List<InterpreterValue> args) throws ParserException {
        throw new ParserException("Macro interpreter cannot process functional macros!");
    }

    @Override
    protected InterpreterValue createVariable(String name) throws ParserException {
        if (lexer.hasNext() && name.equals("defined")) {
            val lookahead = lexer.peek();
            if (lookahead.type() == TokenType.Identifier) {
                lexer.next();
                return new InterpreterValue.IntValue(defines.containsKey(lookahead.text()) ? 1 : 0);
            }
        }
        val define = defines.get(name);
        if (define == null) {
            return new InterpreterValue.IntValue(0);
        }
        return interpret(define.toString(), defines);
    }

    @Override
    protected InterpreterValue createBinaryOperation(InterpreterValue left, InterpreterValue right, Operator operator) {
        return switch (operator) {
            case And -> new InterpreterValue.IntValue(left.asBool() && right.asBool() ? 1 : 0);
            case Or -> new InterpreterValue.IntValue(left.asBool() || right.asBool() ? 1 : 0);
            default -> operateUpcast(left, right, operator);
        };
    }

    @Override
    protected InterpreterValue createIntegerConstant(int value) {
        return new InterpreterValue.IntValue(value);
    }

    @Override
    protected InterpreterValue createFloatConstant(double value) {
        return new InterpreterValue.DoubleValue(value);
    }

    @Override
    protected InterpreterValue createBoolConstant(boolean value) {
        return new InterpreterValue.IntValue(value ? 1 : 0);
    }

    @Override
    protected InterpreterValue createUnaryNot(InterpreterValue value) {
        return new InterpreterValue.IntValue(value.asBool() ? 0 : 1);
    }

    @Override
    protected InterpreterValue createUnaryMinus(InterpreterValue value) {
        if (value instanceof InterpreterValue.IntValue i) {
            return new InterpreterValue.IntValue(-i.value());
        } else if (value instanceof InterpreterValue.DoubleValue d) {
            return new InterpreterValue.DoubleValue(-d.value());
        } else {
            throw new AssertionError();
        }
    }

    @Override
    protected InterpreterValue createSwizzle(InterpreterValue value, int swizzleIndex) throws ParserException {
        throw new ParserException("Macro interpreter cannot process vector swizzles!");
    }

    private InterpreterValue operateUpcast(InterpreterValue left, InterpreterValue right, Operator operator) {
        if (left instanceof InterpreterValue.DoubleValue dl) {
            if (right instanceof InterpreterValue.DoubleValue dr) {
                return new InterpreterValue.DoubleValue(operate(dl.value(), dr.value(), operator));
            } else {
                return new InterpreterValue.DoubleValue(operate(dl.value(),
                                                                ((InterpreterValue.IntValue) right).value(),
                                                                operator));
            }
        } else if (right instanceof InterpreterValue.DoubleValue dr) {
            return new InterpreterValue.DoubleValue(operate(((InterpreterValue.IntValue) left).value(),
                                                            dr.value(),
                                                            operator));
        } else {
            return new InterpreterValue.IntValue(operate(((InterpreterValue.IntValue) left).value(),
                                                         ((InterpreterValue.IntValue) right).value(),
                                                         operator));
        }
    }

    private int operate(int left, int right, Operator operator) {
        return switch (operator) {
            case Add -> left + right;
            case Sub -> left - right;
            case Mul -> left * right;
            case Div -> left / right;
            case Rem -> left % right;
            case Eq -> left == right ? 1 : 0;
            case Ne -> left != right ? 1 : 0;
            case Ge -> left >= right ? 1 : 0;
            case Gt -> left > right ? 1 : 0;
            case Le -> left <= right ? 1 : 0;
            case Lt -> left < right ? 1 : 0;
            default -> throw new AssertionError();
        };
    }

    private double operate(double left, double right, Operator operator) {
        return switch (operator) {
            case Add -> left + right;
            case Sub -> left - right;
            case Mul -> left * right;
            case Div -> left / right;
            case Rem -> left % right;
            case Eq -> left == right ? 1 : 0;
            case Ne -> left != right ? 1 : 0;
            case Ge -> left >= right ? 1 : 0;
            case Gt -> left > right ? 1 : 0;
            case Le -> left <= right ? 1 : 0;
            case Lt -> left < right ? 1 : 0;
            default -> throw new AssertionError();
        };
    }
}
