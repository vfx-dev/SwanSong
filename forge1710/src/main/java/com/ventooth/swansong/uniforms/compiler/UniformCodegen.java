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
import com.ventooth.swansong.uniforms.UniformDef;
import com.ventooth.swansong.uniforms.UniformFunction;
import com.ventooth.swansong.uniforms.UniformFunctionRegistry;
import com.ventooth.swansong.uniforms.compiler.backend.BytecodeOptimizer;
import com.ventooth.swansong.uniforms.compiler.backend.CodeGenerator;
import com.ventooth.swansong.uniforms.compiler.frontend.Optimizer;
import com.ventooth.swansong.uniforms.compiler.frontend.TypeResolver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UniformCodegen {
    private static final Logger log = LogManager.getLogger("SwanSong|UniformCodegen");

    public static Supplier<@Nullable Path> dumpDirSupplier = () -> null;

    private static final String GENERATED_PACKAGE = "com/ventooth/swansong/uniforms/compiled/";

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final Map<Type, String> uniformTypeInternalNameMap = new EnumMap<>(Type.class);
    private static final String internalInterfaceName = org.objectweb.asm.Type.getInternalName(UniformCarrier.class);

    static {
        uniformTypeInternalNameMap.put(Type.Float, org.objectweb.asm.Type.getInternalName(CompiledUniform.Float.class));
        uniformTypeInternalNameMap.put(Type.Int, org.objectweb.asm.Type.getInternalName(CompiledUniform.Int.class));
        uniformTypeInternalNameMap.put(Type.Bool, org.objectweb.asm.Type.getInternalName(CompiledUniform.Bool.class));
        uniformTypeInternalNameMap.put(Type.Vec2, org.objectweb.asm.Type.getInternalName(CompiledUniform.Vec2.class));
        uniformTypeInternalNameMap.put(Type.Vec3, org.objectweb.asm.Type.getInternalName(CompiledUniform.Vec3.class));
        uniformTypeInternalNameMap.put(Type.Vec4, org.objectweb.asm.Type.getInternalName(CompiledUniform.Vec4.class));
    }

    public static Result generate(UniformFunctionRegistry hostUniforms, List<UniformDef> defs) {
        val varRegistry = new UniformFunctionRegistry.Single();
        val registry = new UniformFunctionRegistry.Multi();
        registry.add(Builtins.REGISTRY);
        registry.add(StatefulBuiltins.REGISTRY);
        registry.add(varRegistry);
        registry.add(hostUniforms);

        val carrier = new ClassNode();
        carrier.version = Opcodes.V1_8;
        carrier.superName = "java/lang/Object";
        carrier.interfaces.add(internalInterfaceName);
        carrier.name = GENERATED_PACKAGE + "__COMP_CARRIER_" + counter.incrementAndGet();
        carrier.access = Opcodes.ACC_PUBLIC;
        addEmptyConstructor(carrier);

        val updateMethod = new MethodNode(Opcodes.ACC_PUBLIC, "update", "()V", null, null);
        carrier.methods.add(updateMethod);

        val accessors = new HashMap<String, ClassNode>();
        val compiler = new UniformCompiler(new UniformCompiler.Flags(new TypeResolver.Flags(true),
                                                                     new Optimizer.Flags(true, true, true),
                                                                     new CodeGenerator.Flags(true, true),
                                                                     new BytecodeOptimizer.Flags(true)), registry);
        for (val def : defs) {
            ClassNode accessor;
            try {
                accessor = compile(compiler, def, carrier, updateMethod.instructions);
            } catch (Exception e) {
                // TODO: Logging here should go to debug+trace, and the shortform error appended to the report.
                log.error("Failed to compile custom shader uniform {} with code: {}",
                          def.name(),
                          def.expression()
                             .replace('\n', ' ')
                             .replace('\r', ' '));
                log.trace("Stacktrace:", e);
                continue;
            }
            if (accessor != null) {
                accessors.put(def.name(), accessor);
            }
            varRegistry.addWithNames(new UniformFunction(null,
                                                         carrier.name,
                                                         def.name() + "$get",
                                                         def.type(),
                                                         Collections.emptyList(),
                                                         false), def.name());
        }

        updateMethod.instructions.add(new InsnNode(Opcodes.RETURN));

        val loader = new UniformClassLoader(UniformCodegen.class.getClassLoader(), dumpDirSupplier.get());
        val carrierClass = loader.define(carrier);
        val accessorClasses = new HashMap<String, Class<?>>();
        for (val accessor : accessors.entrySet()) {
            accessorClasses.put(accessor.getKey(), loader.define(accessor.getValue()));
        }

        final UniformCarrier carrierInstance;
        final Map<String, CompiledUniform> accessorInstances;
        try {
            carrierInstance = (UniformCarrier) carrierClass.getConstructor()
                                                           .newInstance();
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
        return new Result(accessorInstances, carrierInstance);
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
                                               InsnList updateMethod) {
        val type = def.type();
        val name = def.name();
        val expr = def.expression();
        val fieldDesc = type.descriptor();
        val methodDesc = "()" + fieldDesc;
        val retOpcode = type.returnOpcode();

        val name$state = name + "$state";
        val name$get = name + "$get";
        val name$update = name + "$update";
        {
            val staticUpdate = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name$update, "()V", null, null);
            val insn = staticUpdate.instructions;
            //This can throw an exception, propagate upward without touching any other state
            comp.compile(type, expr, insn, true);
            insn.add(new FieldInsnNode(Opcodes.PUTSTATIC, carrier.name, name$state, fieldDesc));
            insn.add(new InsnNode(Opcodes.RETURN));
            carrier.methods.add(staticUpdate);
        }

        carrier.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name$state, fieldDesc, null, null));
        {
            val staticGet = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name$get, methodDesc, null, null);
            carrier.methods.add(staticGet);
            val insn = staticGet.instructions;
            insn.add(new FieldInsnNode(Opcodes.GETSTATIC, carrier.name, name$state, fieldDesc));
            insn.add(new InsnNode(retOpcode));
        }
        updateMethod.add(new MethodInsnNode(Opcodes.INVOKESTATIC, carrier.name, name$update, "()V", false));
        if (!def.exposed()) {
            return null;
        }

        //Instance accessor
        val cn = new ClassNode();
        cn.version = Opcodes.V1_8;
        cn.superName = "java/lang/Object";
        cn.interfaces.add(uniformTypeInternalNameMap.get(type));
        cn.name = GENERATED_PACKAGE + "__COMP_UNI_" + counter.incrementAndGet() + "_" + name;
        cn.access = Opcodes.ACC_PUBLIC;
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

    @RequiredArgsConstructor
    public static final class Result {
        public final Map<String, CompiledUniform> uniforms;
        public final UniformCarrier carrier;
    }

    /**
     * Internal use only, use {@link Result#carrier}!
     * Public because generated classes need to see it.
     */
    public interface UniformCarrier {
        void update();
    }

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
