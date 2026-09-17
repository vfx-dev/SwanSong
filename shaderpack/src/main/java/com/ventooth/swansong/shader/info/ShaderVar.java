/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.info;

import com.ventooth.swansong.uniforms.Type;

import java.util.Objects;

//TODO convert to record
public final class ShaderVar {
    private final Variant variant;
    private final Type type;
    private final String name;
    private final String expression;

    public ShaderVar(Variant variant, Type type, String name, String expression) {
        this.variant = variant;
        this.type = type;
        this.name = name;
        this.expression = expression;
    }

    public Variant variant() {
        return variant;
    }

    public Type type() {
        return type;
    }

    public String name() {
        return name;
    }

    public String expression() {
        return expression;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (ShaderVar) obj;
        return Objects.equals(this.variant, that.variant) &&
               Objects.equals(this.type, that.type) &&
               Objects.equals(this.name, that.name) &&
               Objects.equals(this.expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant, type, name, expression);
    }

    @Override
    public String toString() {
        return "ShaderVar[" +
               "variant=" +
               variant +
               ", " +
               "type=" +
               type +
               ", " +
               "name=" +
               name +
               ", " +
               "expression=" +
               expression +
               ']';
    }

    public enum Variant {
        Uniform,
        Variable
    }
}
