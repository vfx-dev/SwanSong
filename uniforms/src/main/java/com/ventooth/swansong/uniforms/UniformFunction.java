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

import lombok.Builder;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: Add Docs
public record UniformFunction(
        // Instance to call on if the method ain't static
        @Nullable Object instance,
        // Woe!!
        Method method,
        // return type
        Type retType,
        // parameter types
        @Unmodifiable List<Type> paramTypes,
        // Passed to ASM on creation
        String owner,
        String name,
        String desc,
        @Unmodifiable Set<Flag> flags) {

    @Builder
    public UniformFunction(@Nullable Object instance,
                           Method method,
                           boolean pure,
                           boolean indexedExplicit,
                           boolean indexedImplicit) {
        val methodParams = method.getParameterTypes();
        val paramTypes = new ArrayList<Type>(methodParams.length);
        for (val methodParam : methodParams) {
            paramTypes.add(Type.of(methodParam));
        }

        val flags = new HashSet<Flag>();
        val methodClass = method.getDeclaringClass();
        if (Modifier.isStatic(method.getModifiers())) {
            flags.add(Flag.STATIC);
            if (pure) {
                flags.add(Flag.PURE);
            }
        } else if (Modifier.isInterface(methodClass.getModifiers())) {
            flags.add(Flag.INTERFACE);
        }

        if (indexedExplicit && indexedImplicit) {
            throw new IllegalArgumentException();
        } else if (indexedExplicit) {
            flags.add(Flag.INDEXED);
        } else if (indexedImplicit) {
            flags.add(Flag.INDEXED);
            flags.add(Flag.INDEXED_IMPLICIT);
        }

        this(instance,
             method,
             Type.of(method.getReturnType()),
             paramTypes,
             org.objectweb.asm.Type.getInternalName(methodClass),
             method.getName(),
             Type.methodDescriptor(Type.of(method.getReturnType()), paramTypes.toArray(new Type[0])),
             flags);
    }

    public UniformFunction(@Nullable Object instance,
                           Method method,
                           Type retType,
                           @Unmodifiable List<Type> paramTypes,
                           String owner,
                           String name,
                           String desc,
                           @Unmodifiable Set<Flag> flags) {
        Flag.validate(flags);

        val methodClass = method.getDeclaringClass();

        if (!Modifier.isPublic(methodClass.getModifiers())) {
            throw new IllegalArgumentException();
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException();
        }

        val isStatic = Modifier.isStatic(method.getModifiers());
        if (isStatic != flags.contains(Flag.STATIC)) {
            throw new IllegalArgumentException();
        }
        val isInterface = Modifier.isInterface(methodClass.getModifiers());
        if (isInterface != flags.contains(Flag.INTERFACE)) {
            throw new IllegalArgumentException();
        }

        if (isStatic) {
            if (instance != null) {
                throw new IllegalArgumentException();
            }
            if (isInterface) {
                throw new IllegalArgumentException();
            }
        } else {
            if (instance == null) {
                throw new IllegalArgumentException();
            }
            if (!methodClass.isAssignableFrom(instance.getClass())) {
                throw new IllegalArgumentException();
            }
        }

        val isIndexed = flags.contains(Flag.INDEXED);
        if (isIndexed) {
            if (paramTypes.isEmpty()) {
                throw new IllegalArgumentException();
            }
            if (paramTypes.getFirst() != Type.Int) {
                throw new IllegalArgumentException();
            }
        }

        if (retType != Type.of(method.getReturnType())) {
            throw new IllegalArgumentException();
        }

        val numParams = paramTypes.size();
        val methodParams = method.getParameterTypes();
        if (numParams != methodParams.length) {
            throw new IllegalArgumentException();
        }
        for (var i = (isIndexed ? 1 : 0); i < numParams; i++) {
            if (paramTypes.get(i) != Type.of(methodParams[i])) {
                throw new IllegalArgumentException(Integer.toString(i));
            }
        }

        // Yes. These two ARE calling 'expensive' methods.
        // But this means that both constructors are validated!
        if (!owner.equals(org.objectweb.asm.Type.getInternalName(methodClass))) {
            throw new IllegalArgumentException();
        }
        if (!desc.equals(Type.methodDescriptor(retType, paramTypes.toArray(new Type[0])))) {
            throw new IllegalArgumentException();
        }

        this.instance = instance;
        this.method = method;
        this.retType = retType;
        this.paramTypes = List.copyOf(paramTypes);
        this.owner = owner;
        this.name = name;
        this.desc = desc;

        if (flags.isEmpty()) {
            this.flags = Collections.emptySet();
        } else {
            this.flags = Collections.unmodifiableSet(EnumSet.copyOf(flags));
        }
    }

    // TODO: Add Docs
    public enum Flag {
        ///
        STATIC,
        ///
        INTERFACE,
        ///
        PURE,
        ///
        INDEXED,
        ///
        INDEXED_IMPLICIT,
        ;

        public static void validate(Set<Flag> flags) {
            for (val flag : flags) {
                switch (flag) {
                    case STATIC -> {
                        if (flags.contains(INTERFACE)) {
                            throw new IllegalArgumentException();
                        }
                    }
                    case PURE -> {
                        if (!flags.contains(STATIC)) {
                            throw new IllegalArgumentException();
                        }
                    }
                    case INDEXED_IMPLICIT -> {
                        if (!flags.contains(INDEXED)) {
                            throw new IllegalArgumentException();
                        }
                    }
                }
            }
        }
    }
}
