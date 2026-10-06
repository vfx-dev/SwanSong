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

import com.ventooth.swansong.uniforms.BuiltinsOld;
import com.ventooth.swansong.uniforms.Type;
import com.ventooth.swansong.uniforms.UniformFunctionRegistryOld;
import com.ventooth.swansong.uniforms.compiler.backend.BytecodeOptimizer;
import com.ventooth.swansong.uniforms.compiler.backend.CodeGenerator;
import com.ventooth.swansong.uniforms.compiler.frontend.Optimizer;
import com.ventooth.swansong.uniforms.compiler.frontend.TypeResolver;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.file.Files;
import java.nio.file.Paths;

class UniformCompilerTest {
    @Test
    void simpleTest() {
        val registry = new UniformFunctionRegistryOld.Multi();
        registry.add(BuiltinsOld.REGISTRY);
        val flags = new UniformCompiler.Flags(new TypeResolver.Flags(true),
                                              new Optimizer.Flags(true, true, true),
                                              new CodeGenerator.Flags(false, false),
                                              new BytecodeOptimizer.Flags(false));
        val compiler = new UniformCompiler(flags, registry);
        val method = compiler.compile(Type.Vec3, "ceil(vec3(1.3) * pi)", UniformCompilerTest::createEmptyMethod);
        val outClass = new ClassNode();
        outClass.version = Opcodes.V1_8;
        outClass.superName = "java/lang/Object";
        outClass.name = "Funny";
        outClass.access = Opcodes.ACC_PUBLIC;
        outClass.methods.add(method);

        val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        outClass.accept(writer);
        Assertions.assertDoesNotThrow(() -> Files.write(Paths.get("Funny.class"), writer.toByteArray()));
    }

    static MethodNode createEmptyMethod(String desc) {
        return new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "funy", desc, null, null);
    }
}
