/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.loader;

import com.ventooth.swansong.shader.Report;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class MultiShaderPool implements IShaderPool {
    private final IShaderPool primary;
    private final LinkedList<Supplier<IShaderPool>> unInitialized;
    private final List<IShaderPool> initialized = new ArrayList<>();
    private final @Nullable Report report;

    public MultiShaderPool(IShaderPool primary, Iterable<ShaderLoader> loaders, @Nullable Report report) {
        this.primary = primary;
        unInitialized = new LinkedList<>();
        for (val loader : loaders) {
            unInitialized.add(() -> {
                loader.lazyLoad(null);
                return loader.borrowOutShaderPool();
            });
        }
        this.report = report;
    }

    @Override
    public CompiledProgram borrowShader(ResourceLocation loc, boolean essential) {
        {
            val prog = primary.borrowShader(loc, essential);
            if (prog != null) {
                return prog;
            }
        }
        if (!essential) {
            return null;
        }
        for (val pool : initialized) {
            val prog = pool.borrowShader(loc, true);
            if (prog != null) {
                if (report != null) {
                    report.shadersFallbackInternal.add(prog.path());
                }
                return prog;
            }
        }
        while (!unInitialized.isEmpty()) {
            val pool = unInitialized.removeFirst()
                                    .get();
            initialized.add(pool);
            val prog = pool.borrowShader(loc, true);
            if (prog != null) {
                if (report != null) {
                    report.shadersFallbackInternal.add(prog.path());
                }
                return prog;
            }
        }
        return null;
    }

    @Override
    public void close() {
        primary.close();
        initialized.forEach(IShaderPool::close);
        initialized.clear();
    }
}
