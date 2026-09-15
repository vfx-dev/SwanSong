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

import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//TODO convert to record
public final class UniformFunction {
    private final @Nullable Method constantFoldMethod;
    private final String javaOwner;
    private final String javaName;
    private final Type returns;
    private final List<Type> params;
    private final boolean statefulIndexed;

    public UniformFunction(@Nullable Method constantFoldMethod,
                           String javaOwner,
                           String javaName,
                           Type returns,
                           List<Type> params,
                           boolean statefulIndexed) {
        this.constantFoldMethod = constantFoldMethod;
        this.javaOwner = javaOwner;
        this.javaName = javaName;
        this.returns = returns;
        this.params = params;
        this.statefulIndexed = statefulIndexed;
    }

    public static UniformFunction of(Method method, boolean constantFoldable, boolean statefulIndexed) {
        val returns = Type.of(method.getReturnType());
        val javaParams = method.getParameterTypes();
        val params = new ArrayList<Type>(javaParams.length);
        for (val javaParam : javaParams) {
            params.add(Type.of(javaParam));
        }
        return new UniformFunction(constantFoldable ? method : null,
                                   org.objectweb.asm.Type.getInternalName(method.getDeclaringClass()),
                                   method.getName(),
                                   returns,
                                   params,
                                   statefulIndexed);
    }

    public @Nullable Method constantFoldMethod() {
        return constantFoldMethod;
    }

    public String javaOwner() {
        return javaOwner;
    }

    public String javaName() {
        return javaName;
    }

    public Type returns() {
        return returns;
    }

    public List<Type> params() {
        return params;
    }

    public boolean statefulIndexed() {
        return statefulIndexed;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (UniformFunction) obj;
        return Objects.equals(this.constantFoldMethod, that.constantFoldMethod) &&
               Objects.equals(this.javaOwner, that.javaOwner) &&
               Objects.equals(this.javaName, that.javaName) &&
               Objects.equals(this.returns, that.returns) &&
               Objects.equals(this.params, that.params) &&
               this.statefulIndexed == that.statefulIndexed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(constantFoldMethod, javaOwner, javaName, returns, params, statefulIndexed);
    }

    @Override
    public String toString() {
        return "UniformFunction[" +
               "constantFoldMethod=" +
               constantFoldMethod +
               ", " +
               "javaOwner=" +
               javaOwner +
               ", " +
               "javaName=" +
               javaName +
               ", " +
               "returns=" +
               returns +
               ", " +
               "params=" +
               params +
               ", " +
               "statefulIndexed=" +
               statefulIndexed +
               ']';
    }

}
