/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.shaderobjects;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.gl.GLProgram;
import com.ventooth.swansong.shader.loader.CompiledProgram;
import com.ventooth.swansong.shader.uniform.Uniform;
import com.ventooth.swansong.shader.uniform.UniformGetterDanglingWires;
import com.ventooth.swansong.sufrace.Framebuffer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.lwjgl.opengl.GL20;

import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Objects;

@Accessors(fluent = true,
           chain = false)
public abstract class ManagedShader {
    protected static final Logger log = Share.getLogger();

    private static final IntList DEFAULT_RENDER_TARGETS = IntLists.singleton(0);

    @Getter
    protected final ResourceLocation loc;
    protected final ResourceLocation actualLoc;
    @Getter
    protected final String srcPath;

    protected GLProgram program;

    @Getter
    protected final IntList renderTargets;

    public @Nullable Framebuffer framebuffer;

    private @Unmodifiable Int2ObjectMap<@NotNull Uniform<?>> uniforms;
    private Int2ObjectArrayMap<@NotNull Uniform<?>> uniformsDirect;

    public ManagedShader(ResourceLocation loc, CompiledProgram prog) {
        this.loc = loc;
        this.actualLoc = prog.actualShaderType();
        this.srcPath = prog.path();
        this.program = prog.program();
        this.uniforms = Int2ObjectMaps.emptyMap();

        var tempRenderTargets = prog.renderTargets();
        if (tempRenderTargets == null) {
            tempRenderTargets = DEFAULT_RENDER_TARGETS;
        }
        this.renderTargets = tempRenderTargets;
    }

    public void init() {
        program.glUseProgram();

        val uniforms = new Int2ObjectArrayMap<Uniform<?>>();

        if (isFallback()) {
            log.debug("Initializing: {}, using fallback: {}", loc, actualLoc);
        } else {
            log.debug("Initializing: {}", loc);
        }

        val uniformCount = program.glGetProgramActiveUniforms();
        log.debug("Expecting {} uniforms", uniformCount);
        if (uniformCount != 0) {
            for (var i = 0; i < uniformCount; i++) {
                val uniformName = program.glGetActiveUniform(i);
                if (uniformName == null || uniformName.isEmpty()) {
                    continue;
                }

                if (uniformName.startsWith("gl_")) {
                    log.debug("Skipping builtin: {}", uniformName);
                    continue;
                }

                // This index is not going to be the same as `i`, as location and index aren't the same thing.
                val index = program.glGetUniformLocation(uniformName);
                found:
                {
                    // TODO: Needs a hashmap lookup
                    val relevantUniforms = relevantUniforms();
                    for (val relevantUniform : relevantUniforms) {
                        if (relevantUniform.name().equals(uniformName)) {
                            uniforms.put(index, relevantUniform);
                            log.debug("Binding relevant uniform: {} at index: {}", uniformName, index);
                            break found;
                        }
                    }

                    // TODO: Needs a hashmap lookup
                    val customUniforms = UniformGetterDanglingWires.customUniforms;
                    if (customUniforms != null) {
                        for (val customUniform : customUniforms) {
                            if (customUniform.name().equals(uniformName)) {
                                uniforms.put(index, customUniform);
                                log.debug("Binding custom uniform: {} at index: {}", uniformName, index);
                                break found;
                            }
                        }
                    }

                    // TODO: Append to report properly
                    log.debug("Unknown uniform: {} at index: {}", uniformName, index);
                }
            }

        } else {
            // TODO: Should this be an error, or somehow present in the report?
            log.debug("Shader {} initialized with no uniforms!", actualLoc);
        }

        this.uniformsDirect = uniforms;
        this.uniforms = Int2ObjectMaps.unmodifiable(uniforms);

        GL20.glUseProgram(0);
    }

    public Int2ObjectMap<Uniform<?>> getUniforms() {
        return this.uniforms;
    }

    public void deinit() {
        // TODO: Move this to the ShaderEngine
        if (framebuffer != null) {
            framebuffer.deinit();
            framebuffer = null;
        }

        program.glDeleteProgram();
        program = null;
        uniforms = null;
        uniformsDirect = null;
    }

    public boolean isFallback() {
        return !Objects.equals(actualLoc, loc);
    }

    /**
     * Will bind the current frame buffer (if one is present)
     * <p>
     * Alongside binding this program, and linking to appropriate uniforms for update tracking.
     */
    public void begin() {
        if (framebuffer != null) {
            framebuffer.bindDraw();
        }

        program.glUseProgram();

        val iter = uniformsDirect.int2ObjectEntrySet()
                                 .fastIterator();
        while (iter.hasNext()) {
            val uniform = iter.next();
            uniform.getValue()
                   .load(uniform.getIntKey());
        }
    }

    /**
     * Mostly here to unbind the uniform tracking. May also unbind the shader if we're at the end of the render pipeline.
     *
     * @param unbind {@code true} if we don't plan on binding another program after this.
     */
    public void end(boolean unbind) {
        for (val uniform : uniformsDirect.values()) {
            uniform.reset();
        }

        if (unbind) {
            GL20.glUseProgram(0);
        }
    }

    protected abstract List<Uniform<?>> relevantUniforms();
}
