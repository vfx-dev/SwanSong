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

import com.ventooth.swansong.shader.info.ShaderVar;
import com.ventooth.swansong.uniforms.CompiledUniform;
import com.ventooth.swansong.uniforms.StatefulBuiltins;
import com.ventooth.swansong.uniforms.UniformDef;
import com.ventooth.swansong.uniforms.UniformFunctionRegistry;
import com.ventooth.swansong.uniforms.compiler.UniformCodegen;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class CompiledUniforms {
    private final UniformCodegen.Result compiled;

    public List<Uniform<?>> wrapUniforms() {
        val list = new ArrayList<Uniform<?>>();

        for (val entry : compiled.uniforms.entrySet()) {
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
        compiled.carrier.update();
        StatefulBuiltins.update();
    }

    public static CompiledUniforms createCompiledUniforms(UniformFunctionRegistry mcUniforms,
                                                          List<ShaderVar> shaderVars) {
        val defs = new ArrayList<UniformDef>(shaderVars.size());
        for (val shaderVar : shaderVars) {
            defs.add(new UniformDef(shaderVar.name(),
                                    shaderVar.type(),
                                    shaderVar.expression(),
                                    shaderVar.variant() == ShaderVar.Variant.Uniform));
        }
        return new CompiledUniforms(UniformCodegen.generate(mcUniforms, defs));
    }
}
