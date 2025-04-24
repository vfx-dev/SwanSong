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

import com.ventooth.swansong.mathparser.AbstractParser;
import com.ventooth.swansong.mathparser.Lexer;
import com.ventooth.swansong.mathparser.ParserException;
import com.ventooth.swansong.uniforms.Builtins;
import com.ventooth.swansong.uniforms.Type;
import com.ventooth.swansong.uniforms.UniformFunctionRegistry;
import com.ventooth.swansong.uniforms.compiler.ast.TypedNode;
import com.ventooth.swansong.uniforms.compiler.ast.UntypedNode;
import com.ventooth.swansong.uniforms.compiler.ast.typed.TypedCastNode;
import com.ventooth.swansong.uniforms.compiler.backend.BytecodeOptimizer;
import com.ventooth.swansong.uniforms.compiler.backend.CodeGenerator;
import com.ventooth.swansong.uniforms.compiler.frontend.Optimizer;
import com.ventooth.swansong.uniforms.compiler.frontend.TypeResolver;
import com.ventooth.swansong.uniforms.compiler.frontend.UntypedParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.file.Files;
import java.nio.file.Paths;

public class UniformCompiler {

    @RequiredArgsConstructor
    public static final class Flags {
        public final TypeResolver.Flags typeResolver;
        public final Optimizer.Flags optimizer;
        public final CodeGenerator.Flags codegen;
        public final BytecodeOptimizer.Flags bytecode;
    }

    private final Flags flags;
    private final TypeResolver typeResolver;
    private final Optimizer optimizer;

    public UniformCompiler(Flags flags, UniformFunctionRegistry registry) {
        this.flags = flags;
        this.typeResolver = new TypeResolver(flags.typeResolver, registry);
        this.optimizer = new Optimizer(flags.optimizer);
    }

    public MethodNode compile(Type returnType, String expressionSource, MethodBuilder builder) {
        val untypedExpr = parse(expressionSource);
        val typedExpr = resolveTypes(returnType, untypedExpr);
        val optimizedExpr = optimizer.transform(typedExpr);
        val method = codegen(optimizedExpr, builder);
        new BytecodeOptimizer(flags.bytecode).optimize(method.instructions);
        return method;
    }

    public void compile(Type returnType, String expressionSource, InsnList instructions, boolean isStatic) {
        val untypedExpr = parse(expressionSource);
        val typedExpr = resolveTypes(returnType, untypedExpr);
        val optimizedExpr = optimizer.transform(typedExpr);
        codegen(optimizedExpr, instructions, isStatic);
        new BytecodeOptimizer(flags.bytecode).optimize(instructions);
    }

    private UntypedNode parse(String expressionSource) {
        val lexer = new Lexer(expressionSource);
        val parser = new UntypedParser(lexer);
        try {
            return parser.parse();
        } catch (Lexer.UnexpectedCharException e) {
            val b = new StringBuilder("Unexpected character in uniform!\n");
            b.append(expressionSource)
             .append('\n');
            for (int i = 0; i < e.at; i++) {
                b.append(' ');
            }
            b.append("^ here\nExpected: ")
             .append(e.expected)
             .append("\nGot: ")
             .append(e.got)
             .append('\n');
            throw new RuntimeException(b.toString(), e);
        } catch (AbstractParser.UnexpectedTokenException e) {
            val b = new StringBuilder("Unexpected token in uniform!\n");
            b.append(expressionSource)
             .append('\n');
            val tok = e.got;
            val off = tok.offset();
            val til = tok.until();
            if (off >= 5) {
                for (int i = 0; i < off - 5; i++) {
                    b.append(' ');
                }
                b.append("here ");
            } else {
                for (int i = 0; i < off; i++) {
                    b.append(' ');
                }
            }
            for (int i = off; i < til; i++) {
                b.append('^');
            }
            if (off < 5) {
                b.append(" here");
            }
            b.append("\nExpected one of: ")
             .append(e.expected)
             .append("\nGot: ")
             .append(e.got.type())
             .append('\n');
            throw new RuntimeException(b.toString(), e);
        } catch (ParserException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private TypedNode resolveTypes(Type returnType, UntypedNode untyped) {
        val typed = typeResolver.transform(untyped);

        if (typed.outputType() != returnType) {
            return new TypedCastNode(returnType, typed);
        }
        return typed;
    }

    private MethodNode codegen(TypedNode expr, MethodBuilder builder) {
        val ot = expr.outputType();
        String desc = "()" + ot.descriptor();
        val ret = new InsnNode(ot.returnOpcode());
        val method = builder.createEmptyMethod(desc);

        codegen(expr, method.instructions, (method.access & Opcodes.ACC_STATIC) != 0);

        method.instructions.add(ret);

        return method;
    }

    private void codegen(TypedNode expr, InsnList instructions, boolean isStatic) {
        val codeGenerator = new CodeGenerator(flags.codegen, isStatic);
        codeGenerator.genExpr(expr, instructions);
    }

    private static MethodNode createEmptyMethod(String desc) {
        return new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "funy", desc, null, null);
    }

    @SneakyThrows
    public static void main(String[] args) {
        val registry = new UniformFunctionRegistry.Multi();
        registry.add(Builtins.REGISTRY);
        val flags = new Flags(new TypeResolver.Flags(true),
                              new Optimizer.Flags(true, true, true),
                              new CodeGenerator.Flags(false, false),
                              new BytecodeOptimizer.Flags(false));
        val compiler = new UniformCompiler(flags, registry);
        val method = compiler.compile(Type.Vec3, "ceil(vec3(1.3) * pi)", UniformCompiler::createEmptyMethod);
        val outClass = new ClassNode();
        outClass.version = Opcodes.V1_8;
        outClass.superName = "java/lang/Object";
        outClass.name = "Funny";
        outClass.access = Opcodes.ACC_PUBLIC;
        outClass.methods.add(method);
        val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        outClass.accept(writer);
        Files.write(Paths.get("Funny.class"), writer.toByteArray());
    }

    @FunctionalInterface
    public interface MethodBuilder {
        MethodNode createEmptyMethod(String descriptor);
    }
}
