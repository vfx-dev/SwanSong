/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.uniform;

import com.falsepattern.lib.util.FileUtil;
import com.ventooth.swansong.Share;
import com.ventooth.swansong.config.DebugConfig;
import com.ventooth.swansong.shader.info.ShaderVar;
import com.ventooth.swansong.uniforms.Builtins;
import com.ventooth.swansong.uniforms.StatefulBuiltins;
import com.ventooth.swansong.uniforms.Type;
import com.ventooth.swansong.uniforms.UniformFunction;
import com.ventooth.swansong.uniforms.UniformFunctionRegistry;
import com.ventooth.swansong.uniforms.compiler.UniformCompiler;
import com.ventooth.swansong.uniforms.compiler.backend.BytecodeOptimizer;
import com.ventooth.swansong.uniforms.compiler.backend.CodeGenerator;
import com.ventooth.swansong.uniforms.compiler.frontend.Optimizer;
import com.ventooth.swansong.uniforms.compiler.frontend.TypeResolver;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.FileUtils;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class CompiledUniforms {
    private final Map<String, CompiledUniform> uniforms;
    private final InternalCompiledUniform carrier;

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final Map<Type, String> uniformTypeInternalNameMap = new EnumMap<>(com.ventooth.swansong.uniforms.Type.class);
    private static final String internalInterfaceName = org.objectweb.asm.Type.getInternalName(InternalCompiledUniform.class);

    static {
        uniformTypeInternalNameMap.put(Type.Float, org.objectweb.asm.Type.getInternalName(CompiledUniform.Float.class));
        uniformTypeInternalNameMap.put(Type.Int, org.objectweb.asm.Type.getInternalName(CompiledUniform.Int.class));
        uniformTypeInternalNameMap.put(Type.Bool, org.objectweb.asm.Type.getInternalName(CompiledUniform.Bool.class));
        uniformTypeInternalNameMap.put(Type.Vec2, org.objectweb.asm.Type.getInternalName(CompiledUniform.Vec2.class));
        uniformTypeInternalNameMap.put(Type.Vec3, org.objectweb.asm.Type.getInternalName(CompiledUniform.Vec3.class));
        uniformTypeInternalNameMap.put(Type.Vec4, org.objectweb.asm.Type.getInternalName(CompiledUniform.Vec4.class));
    }

    public List<Uniform<?>> wrapUniforms() {
        val list = new ArrayList<Uniform<?>>();

        for (val entry : uniforms.entrySet()) {
            val name = entry.getKey();
            val rawGetter = entry.getValue();

            if (rawGetter instanceof CompiledUniform.Float getter) {
                list.add(new Uniform.OfDouble(name, getter::value, Uniform::set));
            } else if (rawGetter instanceof CompiledUniform.Int getter) {
                list.add(new Uniform.OfInt(name, getter::value, Uniform::set));
            } else if (rawGetter instanceof CompiledUniform.Bool getter) {
                list.add(new Uniform.OfBoolean(name, getter::value, Uniform::set));
            } else if (rawGetter instanceof CompiledUniform.Vec2 getter) {
                list.add(new Uniform.Of<>(name, getter::value, Uniform::set));
            } else if (rawGetter instanceof CompiledUniform.Vec3 getter) {
                list.add(new Uniform.Of<>(name, getter::value, Uniform::set));
            } else if (rawGetter instanceof CompiledUniform.Vec4 getter) {
                list.add(new Uniform.Of<>(name, getter::value, Uniform::set));
            }
        }

        return Collections.unmodifiableList(list);
    }

    public void update() {
        carrier.update();
        StatefulBuiltins.update();
    }

    public static CompiledUniforms createCompiledUniforms(UniformFunctionRegistry mcUniforms,
                                                          List<ShaderVar> shaderVars) {
        val varRegistry = new UniformFunctionRegistry.Single();
        val registry = new UniformFunctionRegistry.Multi();
        registry.add(Builtins.REGISTRY);
        registry.add(StatefulBuiltins.REGISTRY);
        registry.add(varRegistry);
        registry.add(mcUniforms);
        val carrier = new ClassNode();
        carrier.version = Opcodes.V1_8;
        carrier.superName = "java/lang/Object";
        carrier.interfaces.add(internalInterfaceName);
        carrier.name = "com/ventooth/swansong/shader/uniform/compiled/__COMP_CARRIER_" + counter.incrementAndGet();
        carrier.access = Opcodes.ACC_PUBLIC;
        addEmptyConstructor(carrier);
        val updateMethod = new MethodNode(Opcodes.ACC_PUBLIC, "update", "()V", null, null);
        carrier.methods.add(updateMethod);
        val accessors = new HashMap<String, ClassNode>();
        val compiler = new UniformCompiler(new UniformCompiler.Flags(new TypeResolver.Flags(true),
                                                                     new Optimizer.Flags(true, true, true),
                                                                     new CodeGenerator.Flags(true, true),
                                                                     new BytecodeOptimizer.Flags(true)), registry);
        for (val shaderVar : shaderVars) {
            ClassNode accessor;
            try {
                accessor = compile(compiler, shaderVar, carrier, updateMethod.instructions);
            } catch (Exception e) {
                // TODO: Logging here should go to debug+trace, and the shortform error appended to the report.
                Share.log.error("Failed to compile custom shader uniform {} with code: {}",
                                shaderVar.name(),
                                shaderVar.expression()
                                         .replace('\n', ' ')
                                         .replace('\r', ' '));
                Share.log.trace("Stacktrace:", e);
                continue;
            }
            if (accessor != null) {
                accessors.put(shaderVar.name(), accessor);
            }
            varRegistry.addWithNames(new UniformFunction(null,
                                                         carrier.name,
                                                         shaderVar.name() + "$get",
                                                         shaderVar.type(),
                                                         Collections.emptyList(),
                                                         false), shaderVar.name());
        }

        updateMethod.instructions.add(new InsnNode(Opcodes.RETURN));
        val loader = new UniformClassLoader(CompiledUniforms.class.getClassLoader());
        val carrierClass = loader.define(carrier);
        val accessorClasses = new HashMap<String, Class<?>>();
        for (val accessor : accessors.entrySet()) {
            accessorClasses.put(accessor.getKey(), loader.define(accessor.getValue()));
        }
        final InternalCompiledUniform carrierInstance;
        final Map<String, CompiledUniform> accessorInstances;
        try {
            carrierInstance = (InternalCompiledUniform) carrierClass.getConstructor()
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
        return new CompiledUniforms(accessorInstances, carrierInstance);
    }

    private static void addEmptyConstructor(ClassNode cn) {
        val init = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
        init.instructions.add(new InsnNode(Opcodes.RETURN));
        cn.methods.add(init);
    }

    private static @Nullable ClassNode compile(UniformCompiler comp,
                                               ShaderVar var,
                                               ClassNode carrier,
                                               InsnList updateMethod) {
        val variant = var.variant();
        val type = var.type();
        val name = var.name();
        val expr = var.expression();
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
        if (variant != ShaderVar.Variant.Uniform) {
            return null;
        }
        //Instance accessor
        val cn = new ClassNode();
        cn.version = Opcodes.V1_8;
        cn.superName = "java/lang/Object";
        cn.interfaces.add(uniformTypeInternalNameMap.get(type));
        cn.name = "com/ventooth/swansong/shader/uniform/compiled/__COMP_UNI_" + counter.incrementAndGet() + "_" + name;
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

    /**
     * Internal use only, use {@link #update()}!
     * Public because generated classes need to see it.
     */
    public interface InternalCompiledUniform {
        void update();
    }

    private static class UniformClassLoader extends ClassLoader {
        UniformClassLoader(ClassLoader parent) {
            super(parent);
            if (DebugConfig.DumpCompiledUniforms) {
                if (Files.exists(debugDir)) {
                    try {
                        FileUtils.deleteDirectory(debugDir.toFile());
                    } catch (IOException ignored) {
                    }
                }
                try {
                    Files.createDirectories(debugDir);
                } catch (IOException ignored) {
                }
            }
        }

        private final Map<String, Class<?>> knownClasses = new HashMap<>();

        private static final Path debugDir = FileUtil.getMinecraftHomePath()
                                                     .resolve("swansong_uniform_compiler");

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
            if (DebugConfig.DumpCompiledUniforms) {
                val dumpFile = debugDir.resolve(cn.name + ".class");
                try {
                    Files.createDirectories(dumpFile.getParent());
                    Files.write(dumpFile, bytes);
                } catch (IOException ignored) {
                }
            }
            val klass = defineClass(name, bytes, 0, bytes.length);
            knownClasses.put(name, klass);
            return klass;
        }
    }
}
