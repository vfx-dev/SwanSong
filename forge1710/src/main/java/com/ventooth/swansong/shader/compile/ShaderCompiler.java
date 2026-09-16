/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.compile;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.gl.GLProgram;
import com.ventooth.swansong.gl.GLShader;
import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.shader.Report;
import com.ventooth.swansong.shader.ShaderException;
import com.ventooth.swansong.shader.loader.PreprocessedProgram;
import com.ventooth.swansong.shader.loader.ShaderLoader;
import com.ventooth.swansong.todo.tess.DanglingWiresTess.AttribMapping;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderCompiler {
    public static ShaderPool compile(ShaderLoader loader,
                                     List<AttribMapping> attribs,
                                     @Nullable Report report) {
        val pool = new ShaderPool();
        val disabled = loader.outDisabled;
        if (disabled != null && !disabled.isEmpty()) {
            pool.setDisabled(disabled);
        }
        for (val program : loader.outPrograms) {
            val compiled = compileProgram(program, attribs, report);
            if (compiled != null) {
                pool.insertShader(program.loc(), compiled);
            }
        }
        return pool;
    }

    public static ShaderPool lazyCompile(ShaderLoader loader, List<AttribMapping> attribs) {
        loader.lazyLoad(null);
        return compile(loader, attribs, null);
    }

    private static @Nullable CompiledProgram compileProgram(PreprocessedProgram program,
                                                            List<AttribMapping> attribs,
                                                            @Nullable Report report) {
        GLShader vert = null;
        GLShader geom = null;
        GLShader frag = null;

        try {
            vert = createShader(GL20.GL_VERTEX_SHADER,
                                program.path() + ".vsh",
                                program.vert()
                                       .getNativeBuffer(true));
            if (program.geom() != null) {
                geom = createShader(GL32.GL_GEOMETRY_SHADER,
                                    program.path() + ".gsh",
                                    program.geom()
                                           .getNativeBuffer(true));
            }
            frag = createShader(GL20.GL_FRAGMENT_SHADER,
                                program.path() + ".fsh",
                                program.frag()
                                       .getNativeBuffer(true));
            val prog = createProgram(program.path(), vert, geom, frag, attribs);
            return new CompiledProgram(program.path(),
                                       prog,
                                       program.mipmapEnabled(),
                                       program.renderTargets(),
                                       program.actualLoc());
        } catch (Exception e) {
            Share.log.error("Error while compiling shader {}", program.path());
            Share.log.error("Stacktrace:", e);
            if (report != null) {
                report.erroredShaders.add(program.path());
            }
            return null;
        } finally {
            if (vert != null) {
                vert.glDeleteShader();
            }
            if (geom != null) {
                geom.glDeleteShader();
            }
            if (frag != null) {
                frag.glDeleteShader();
            }
        }
    }

    /**
     * src MUST be null terminated!
     */
    public static @NotNull GLShader createShader(@MagicConstant(intValues = {GL20.GL_VERTEX_SHADER,
                                                                             GL32.GL_GEOMETRY_SHADER,
                                                                             GL20.GL_FRAGMENT_SHADER}) int type,
                                                 String name,
                                                 ByteBuffer src) throws ShaderException {
        ShaderPackManager.dumpShader(name, src);
        val shader = new GLShader();
        shader.glCreateShader(type);
        shader.glShaderSource(src);
        shader.glCompileShader();

        if (!shader.glGetShaderCompileStatus()) {
            var infoLog = shader.glGetShaderInfoLog();
            if (infoLog.isEmpty()) {
                infoLog = "Empty shader info log";
            }
            shader.glDeleteShader();

            throw new ShaderException("Failed to compile shader: " + name + '\n' + (infoLog) + '\n');
        }

        return shader;
    }

    public static @NotNull GLProgram createProgram(@NotNull String name,
                                                   @NotNull GLShader vertShader,
                                                   @Nullable GLShader geomShader,
                                                   @NotNull GLShader fragShader,
                                                   @NotNull List<AttribMapping> attribs) throws ShaderException {
        val program = new GLProgram();
        program.glCreateProgram();
        program.glAttachShader(vertShader);
        if (geomShader != null) {
            program.glAttachShader(geomShader);
        }
        program.glAttachShader(fragShader);

        for (val attrib : attribs) {
            program.glBindAttribLocation(attrib.index, attrib.name);
        }

        program.glLinkProgram();
        if (!program.glGetProgramLinkStatus()) {
            var infoLog = program.glGetProgramInfoLog();
            if (infoLog.isEmpty()) {
                infoLog = "Empty program info log";
            }
            program.glDeleteProgram();

            throw new ShaderException("Failed to link program: " + name + '\n' + (infoLog) + '\n');
        }

        return program;
    }

}
