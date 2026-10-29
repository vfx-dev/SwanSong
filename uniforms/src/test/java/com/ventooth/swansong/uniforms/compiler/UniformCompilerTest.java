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
import com.ventooth.swansong.uniforms.Type;
import com.ventooth.swansong.uniforms.UniformDef;
import com.ventooth.swansong.uniforms.UniformFunctionLookup;
import com.ventooth.swansong.uniforms.UniformFunctionProvider;
import com.ventooth.swansong.uniforms.UniformFunctionRegistry;
import com.ventooth.swansong.uniforms.compiler.backend.BytecodeOptimizer;
import com.ventooth.swansong.uniforms.compiler.backend.CodeGenerator;
import com.ventooth.swansong.uniforms.compiler.frontend.Optimizer;
import com.ventooth.swansong.uniforms.compiler.frontend.TypeResolver;
import lombok.SneakyThrows;
import lombok.val;
import org.joml.Vector3d;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

class UniformCompilerTest {
    @Test
    void simpleTest() {
        val lookup = new UniformFunctionLookup.Multi();
        lookup.add(Builtins.LOOKUP);
        val flags = new UniformCompiler.Flags(new TypeResolver.Flags(true),
                                              new Optimizer.Flags(true, true, true),
                                              new CodeGenerator.Flags(false, false),
                                              new BytecodeOptimizer.Flags(false));
        val compiler = new UniformCompiler(flags, lookup);
        val method = compiler.compile(Type.Vec3, "ceil(vec3(1.3) * pi)", UniformCompilerTest::createEmptyMethod);
        val outClass = new ClassNode();
        outClass.version = Opcodes.V1_8;
        outClass.superName = "java/lang/Object";
        outClass.name = "Funny";
        outClass.access = Opcodes.ACC_PUBLIC;
        outClass.methods.add(method);

        val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        outClass.accept(writer);
        Assertions.assertTrue(writer.toByteArray().length > 0);
    }

    @Test
    void codegenEndToEndTest() {
        val provider = new TestProvider();
        val codegen = new UniformCodegen();
        codegen.register(provider);

        val defs = List.of(
                new UniformDef("baseVar", Type.Float, "counterVal * 2.0", false),
                new UniformDef("outFloat", Type.Float, "baseVar + smooth(baseVar, 0.0, 0.0)", true),
                new UniformDef("outExplicit", Type.Float, "smooth(3, baseVar, 0.0, 0.0)", true),
                new UniformDef("outRand", Type.Int, "randomInt(10, 20)", true),
                new UniformDef("outVec", Type.Vec3, "ceil(vec3(1.3) * 2.0)", true),
                new UniformDef("brokenUniform", Type.Float, "unknownFunc(123)", true)
        );

        val result = codegen.generate(defs);
        Assertions.assertEquals(1, result.errors.size());
        Assertions.assertFalse(result.uniforms.containsKey("baseVar"));
        Assertions.assertTrue(result.uniforms.containsKey("outFloat"));
        Assertions.assertTrue(result.uniforms.containsKey("outExplicit"));
        Assertions.assertTrue(result.uniforms.containsKey("outRand"));
        Assertions.assertTrue(result.uniforms.containsKey("outVec"));

        provider.counter = 5.0;
        result.carrier.update();
        Assertions.assertEquals(1, provider.updates);
        Assertions.assertEquals(20.0, ((CompiledUniform.Float) result.uniforms.get("outFloat")).value(), 1e-6);
        Assertions.assertEquals(10.0, ((CompiledUniform.Float) result.uniforms.get("outExplicit")).value(), 1e-6);
        val randVal = ((CompiledUniform.Int) result.uniforms.get("outRand")).value();
        Assertions.assertTrue(randVal >= 10 && randVal < 20);
        Assertions.assertEquals(new Vector3d(3.0, 3.0, 3.0),
                                ((CompiledUniform.Vec3) result.uniforms.get("outVec")).value());

        result.carrier.reset();
        Assertions.assertEquals(1, provider.resets);
    }

    @Test
    void variablesShadowProviderFunctions() {
        val codegen = new UniformCodegen();
        codegen.register(new TestProvider());

        val defs = List.of(new UniformDef("counterVal", Type.Float, "7.0", false),
                           new UniformDef("out", Type.Float, "counterVal", true));

        val result = codegen.generate(defs);
        Assertions.assertTrue(result.errors.isEmpty());
        result.carrier.update();
        Assertions.assertEquals(7.0, ((CompiledUniform.Float) result.uniforms.get("out")).value(), 1e-6);
    }

    static MethodNode createEmptyMethod(String desc) {
        return new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "funy", desc, null, null);
    }

    public static class TestProvider implements UniformFunctionProvider {
        public double counter = 0.0;
        public int resets = 0;
        public int updates = 0;

        @Override
        public String name() {
            return "test_provider";
        }

        @Override
        @SneakyThrows
        public void init(UniformFunctionRegistry cfg) {
            cfg.impure(TestProvider.class.getDeclaredMethod("counterVal"));
        }

        @Override
        public void reset() {
            resets++;
        }

        @Override
        public void update() {
            updates++;
        }

        public double counterVal() {
            return counter;
        }
    }
}
