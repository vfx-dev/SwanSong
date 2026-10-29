/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms.compiler;

import com.ventooth.swansong.uniforms.Builtins;
import com.ventooth.swansong.uniforms.CompiledUniform;
import com.ventooth.swansong.uniforms.StatefulBuiltins;
import com.ventooth.swansong.uniforms.Type;
import com.ventooth.swansong.uniforms.UniformCarrier;
import com.ventooth.swansong.uniforms.UniformDef;
import com.ventooth.swansong.uniforms.UniformFunction;
import com.ventooth.swansong.uniforms.UniformFunctionLookup;
import com.ventooth.swansong.uniforms.UniformFunctionProvider;
import com.ventooth.swansong.uniforms.UniformFunctionRegistry;
import com.ventooth.swansong.uniforms.compiler.backend.BytecodeOptimizer;
import com.ventooth.swansong.uniforms.compiler.backend.CodeGenerator;
import com.ventooth.swansong.uniforms.compiler.frontend.Optimizer;
import com.ventooth.swansong.uniforms.compiler.frontend.TypeResolver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class UniformCodegen {
    private static final Logger log = LogManager.getLogger("SwanSong|UniformCodegen");

    public static Supplier<@Nullable Path> dumpDirSupplier = () -> null;

    private static final String GENERATED_PACKAGE = "com/ventooth/swansong/uniforms/compiled/";

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final String INTERNAL_CARRIER_NAME = org.objectweb.asm.Type.getInternalName(UniformCarrier.class);
    private static final String INTERNAL_PROVIDER_NAME = org.objectweb.asm.Type.getInternalName(UniformFunctionProvider.class);

    protected final Map<String, UniformFunctionProvider> valueProviders = new LinkedHashMap<>();
    protected final UniformFunctionLookup.Multi builtinLookups = new UniformFunctionLookup.Multi();
    protected final UniformFunctionLookup.Multi lookups = new UniformFunctionLookup.Multi();

    public UniformCodegen() {
        builtinLookups.add(Builtins.LOOKUP);
        builtinLookups.add(initProvider(new StatefulBuiltins()));
    }

    public void register(UniformFunctionLookup lookup) {
        lookups.add(Objects.requireNonNull(lookup));
    }

    public void register(UniformFunctionProvider provider) {
        lookups.add(initProvider(provider));
    }

    private UniformFunctionLookup initProvider(UniformFunctionProvider provider) {
        val name = provider.name();
        if (valueProviders.containsKey(name)) {
            throw new IllegalStateException("Duplicate UniformFunctionProvider: " + name);
        }

        val lookup = new UniformFunctionLookup.Single();
        provider.init(new ContextRegistry(provider, lookup));
        valueProviders.put(name, provider);
        return lookup;
    }

    public Result generate(List<UniformDef> definitions) {
        val varLookup = new UniformFunctionLookup.Single();
        val combinedLookup = new UniformFunctionLookup.Multi();
        combinedLookup.add(builtinLookups);
        combinedLookup.add(varLookup);
        combinedLookup.add(lookups);

        val carrier = new ClassNode();
        carrier.version = Opcodes.V1_8;
        carrier.superName = "java/lang/Object";
        carrier.interfaces.add(INTERNAL_CARRIER_NAME);
        carrier.name = GENERATED_PACKAGE + "__COMP_CARRIER_" + counter.incrementAndGet();
        carrier.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL;

        val carrierFields = new ArrayList<CarrierField>();
        for (val provider : valueProviders.values()) {
            ensureCarrierField(carrierFields, provider, UniformFunctionProvider.class);
        }

        val updateMethod = new MethodNode(Opcodes.ACC_PUBLIC, "update", "()V", null, null);
        val accessors = new HashMap<String, ClassNode>();
        val errors = new ArrayList<String>();

        val compiler = new UniformCompiler(new UniformCompiler.Flags(new TypeResolver.Flags(true),
                                                                     new Optimizer.Flags(true, true, true),
                                                                     new CodeGenerator.Flags(true, true),
                                                                     new BytecodeOptimizer.Flags(true)),
                                           combinedLookup);

        final CodeGenerator.InstanceLoader instanceLoader = (fn, insnList) -> {
            val inst = Objects.requireNonNull(fn.instance());
            val declaringClass = Objects.requireNonNull(fn.method())
                                        .getDeclaringClass();
            val field = ensureCarrierField(carrierFields, inst, declaringClass);
            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
            insnList.add(new FieldInsnNode(Opcodes.GETFIELD, carrier.name, field.name, field.desc));
        };

        for (val def : definitions) {
            ClassNode accessor;
            try {
                accessor = compile(compiler, def, carrier, updateMethod.instructions, instanceLoader);
            } catch (Exception e) {
                val cleanExpr = def.expression()
                                   .replace('\n', ' ')
                                   .replace('\r', ' ');
                val errMsg = "Failed to compile custom shader uniform " + def.name() + " with code: " + cleanExpr;
                log.error("Failed to compile custom shader uniform {} with code: {}", def.name(), cleanExpr);
                log.trace("Stacktrace:", e);
                errors.add(errMsg);
                continue;
            }
            if (accessor != null) {
                accessors.put(def.name(), accessor);
            }
            // TODO: Uniform definitions are *forced* to be ordered because of this.
            //  It is also the mechanism that implicitly prevents recursion.
            //  Iris allows for shaders to be defined out-of-order and still works.
            //  They seem to do explicit recursion checks.
            //  NOTE: If we *did* implement this, then we should catch `StackOverflowError` on updates!
            varLookup.add(new UniformFunction(null,
                                              null,
                                              def.type(),
                                              Collections.emptyList(),
                                              carrier.name,
                                              def.name() + "$get",
                                              Type.methodDescriptor(def.type()),
                                              EnumSet.of(UniformFunction.Flag.STATIC)),
                          def.name());
        }

        val resetMethod = new MethodNode(Opcodes.ACC_PUBLIC, "reset", "()V", null, null);
        for (val provider : valueProviders.values()) {
            val field = ensureCarrierField(carrierFields, provider, UniformFunctionProvider.class);
            resetMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            resetMethod.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, carrier.name, field.name, field.desc));
            resetMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,
                                                            INTERNAL_PROVIDER_NAME,
                                                            "reset",
                                                            "()V",
                                                            true));
            updateMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            updateMethod.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, carrier.name, field.name, field.desc));
            updateMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,
                                                             INTERNAL_PROVIDER_NAME,
                                                             "update",
                                                             "()V",
                                                             true));
        }
        resetMethod.instructions.add(new InsnNode(Opcodes.RETURN));
        updateMethod.instructions.add(new InsnNode(Opcodes.RETURN));

        addCarrierConstructorAndFields(carrier, carrierFields);
        carrier.methods.add(resetMethod);
        carrier.methods.add(updateMethod);

        val loader = new UniformClassLoader(UniformCodegen.class.getClassLoader(), dumpDirSupplier.get());
        val carrierClass = loader.define(carrier);
        val accessorClasses = new HashMap<String, Class<?>>();
        for (val accessor : accessors.entrySet()) {
            accessorClasses.put(accessor.getKey(), loader.define(accessor.getValue()));
        }

        final UniformCarrier carrierInstance;
        final Map<String, CompiledUniform> accessorInstances;
        try {
            val ctorTypes = new Class<?>[carrierFields.size()];
            val ctorArgs = new Object[carrierFields.size()];
            for (var i = 0; i < carrierFields.size(); i++) {
                val field = carrierFields.get(i);
                ctorTypes[i] = field.type;
                ctorArgs[i] = field.instance;
            }
            carrierInstance = (UniformCarrier) carrierClass.getConstructor(ctorTypes)
                                                           .newInstance(ctorArgs);
            accessorInstances = new HashMap<>();
            for (val accessorClass : accessorClasses.entrySet()) {
                accessorInstances.put(accessorClass.getKey(),
                                      (CompiledUniform) accessorClass.getValue()
                                                                     .getConstructor()
                                                                     .newInstance());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return new Result(Collections.unmodifiableMap(accessorInstances),
                          carrierInstance,
                          Collections.unmodifiableList(errors));
    }

    private static CarrierField ensureCarrierField(List<CarrierField> fields, Object instance, Class<?> requiredType) {
        for (val existing : fields) {
            if (existing.instance == instance && requiredType.isAssignableFrom(existing.type)) {
                return existing;
            }
        }
        val instClass = instance.getClass();
        val fieldType = Modifier.isPublic(instClass.getModifiers()) && requiredType.isAssignableFrom(instClass)
                        ? instClass
                        : requiredType;
        val field = new CarrierField("inst$" + fields.size(),
                                     fieldType,
                                     org.objectweb.asm.Type.getDescriptor(fieldType),
                                     instance);
        fields.add(field);
        return field;
    }

    private static void addCarrierConstructorAndFields(ClassNode carrier, List<CarrierField> fields) {
        val descBuilder = new StringBuilder("(");
        for (val field : fields) {
            carrier.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                                             field.name,
                                             field.desc,
                                             null,
                                             null));
            descBuilder.append(field.desc);
        }
        descBuilder.append(")V");

        val init = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", descBuilder.toString(), null, null);
        init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
        for (var i = 0; i < fields.size(); i++) {
            val field = fields.get(i);
            init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            init.instructions.add(new VarInsnNode(Opcodes.ALOAD, i + 1));
            init.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, carrier.name, field.name, field.desc));
        }
        init.instructions.add(new InsnNode(Opcodes.RETURN));
        carrier.methods.add(init);
    }

    private static void addEmptyConstructor(ClassNode cn) {
        val init = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
        init.instructions.add(new InsnNode(Opcodes.RETURN));
        cn.methods.add(init);
    }

    private static @Nullable ClassNode compile(UniformCompiler comp,
                                               UniformDef def,
                                               ClassNode carrier,
                                               InsnList updateMethod,
                                               CodeGenerator.InstanceLoader instanceLoader) {
        val type = def.type();
        val name = def.name();
        val expr = def.expression();
        val fieldDesc = type.descriptor();
        val methodDesc = "()" + fieldDesc;
        val retOpcode = type.returnOpcode();

        val name$state = name + "$state";
        val name$get = name + "$get";
        val name$update = name + "$update";

        val exprInsns = comp.compile(type, expr, false, instanceLoader);
        {
            val instanceUpdate = new MethodNode(Opcodes.ACC_PRIVATE, name$update, "()V", null, null);
            val insn = instanceUpdate.instructions;
            insn.add(exprInsns);
            insn.add(new FieldInsnNode(Opcodes.PUTSTATIC, carrier.name, name$state, fieldDesc));
            insn.add(new InsnNode(Opcodes.RETURN));
            carrier.methods.add(instanceUpdate);
        }

        carrier.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name$state, fieldDesc, null, null));
        {
            val staticGet = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name$get, methodDesc, null, null);
            carrier.methods.add(staticGet);
            val insn = staticGet.instructions;
            insn.add(new FieldInsnNode(Opcodes.GETSTATIC, carrier.name, name$state, fieldDesc));
            insn.add(new InsnNode(retOpcode));
        }
        updateMethod.add(new VarInsnNode(Opcodes.ALOAD, 0));
        updateMethod.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, carrier.name, name$update, "()V", false));
        if (!def.exposed()) {
            return null;
        }

        val cn = new ClassNode();
        cn.version = Opcodes.V1_8;
        cn.superName = "java/lang/Object";
        cn.interfaces.add(type.internalTypeName());
        cn.name = GENERATED_PACKAGE + "__COMP_UNI_" + counter.incrementAndGet() + "_" + name;
        cn.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL;
        addEmptyConstructor(cn);
        {
            val dynamicGet = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "value", methodDesc, null, null);
            val insn = dynamicGet.instructions;
            insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, carrier.name, name$get, methodDesc, false));
            insn.add(new InsnNode(retOpcode));
            cn.methods.add(dynamicGet);
        }
        return cn;
    }

    private record CarrierField(String name, Class<?> type, String desc, Object instance) {
    }

    @RequiredArgsConstructor
    private static final class ContextRegistry implements UniformFunctionRegistry {
        private final Object defaultInstance;
        private final UniformFunctionLookup.Single delegate;

        @Override
        public UniformFunction pure(Method method) {
            return delegate.pure(method);
        }

        @Override
        public UniformFunction pure(Method method, String... names) {
            return delegate.pure(method, names);
        }

        @Override
        public UniformFunction impure(Method method) {
            return Modifier.isStatic(method.getModifiers())
                   ? delegate.impure(method)
                   : delegate.impure(defaultInstance, method);
        }

        @Override
        public UniformFunction impure(Method method, String... names) {
            return Modifier.isStatic(method.getModifiers())
                   ? delegate.impure(method, names)
                   : delegate.impure(defaultInstance, method, names);
        }

        @Override
        public UniformFunction impure(Object instance, Method method) {
            return delegate.impure(instance, method);
        }

        @Override
        public UniformFunction impure(Object instance, Method method, String... names) {
            return delegate.impure(instance, method, names);
        }

        @Override
        public UniformFunction idxExplicit(Method method) {
            return Modifier.isStatic(method.getModifiers())
                   ? delegate.idxExplicit(method)
                   : delegate.idxExplicit(defaultInstance, method);
        }

        @Override
        public UniformFunction idxExplicit(Method method, String... names) {
            return Modifier.isStatic(method.getModifiers())
                   ? delegate.idxExplicit(method, names)
                   : delegate.idxExplicit(defaultInstance, method, names);
        }

        @Override
        public UniformFunction idxExplicit(Object instance, Method method) {
            return delegate.idxExplicit(instance, method);
        }

        @Override
        public UniformFunction idxExplicit(Object instance, Method method, String... names) {
            return delegate.idxExplicit(instance, method, names);
        }

        @Override
        public UniformFunction idxImplicit(Method method) {
            return Modifier.isStatic(method.getModifiers())
                   ? delegate.idxImplicit(method)
                   : delegate.idxImplicit(defaultInstance, method);
        }

        @Override
        public UniformFunction idxImplicit(Method method, String... names) {
            return Modifier.isStatic(method.getModifiers())
                   ? delegate.idxImplicit(method, names)
                   : delegate.idxImplicit(defaultInstance, method, names);
        }

        @Override
        public UniformFunction idxImplicit(Object instance, Method method) {
            return delegate.idxImplicit(instance, method);
        }

        @Override
        public UniformFunction idxImplicit(Object instance, Method method, String... names) {
            return delegate.idxImplicit(instance, method, names);
        }

        @Override
        public UniformFunction add(UniformFunction fn, String... names) {
            return delegate.add(fn, names);
        }

        @Override
        public UniformFunction add(UniformFunction fn, String name) {
            return delegate.add(fn, name);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Result {
        public final Map<String, CompiledUniform> uniforms;
        public final UniformCarrier carrier;
        public final List<String> errors;
    }

    // TODO: Debug tracking on if this class *actually* gets unloaded?
    private static class UniformClassLoader extends ClassLoader {
        private final @Nullable Path dumpDir;

        UniformClassLoader(ClassLoader parent, @Nullable Path dumpDir) {
            super(parent);
            this.dumpDir = dumpDir;
        }

        private final Map<String, Class<?>> knownClasses = new HashMap<>();

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            val cls = knownClasses.get(name);
            if (cls == null) {
                throw new ClassNotFoundException(name);
            }
            return cls;
        }

        private Class<?> define(ClassNode cn) {
            val name = cn.name.replace('/', '.');
            val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(writer);
            val bytes = writer.toByteArray();
            dump(cn.name, bytes);
            val klass = defineClass(name, bytes, 0, bytes.length);
            knownClasses.put(name, klass);
            return klass;
        }

        private void dump(String internalName, byte[] bytes) {
            val dir = dumpDir;
            if (dir == null) {
                return;
            }
            val dumpFile = dir.resolve(internalName + ".class");
            try {
                Files.createDirectories(dumpFile.getParent());
                Files.write(dumpFile, bytes);
            } catch (IOException ignored) {
            }
        }
    }
}
