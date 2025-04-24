/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.asm;

import com.falsepattern.lib.turboasm.MergeableTurboTransformer;
import com.ventooth.swansong.asm.transformers.SkyRendererTransformer;
import com.ventooth.swansong.asm.transformers.TessellatorShaderTransformer;

import java.util.Arrays;

public class RootTransformer extends MergeableTurboTransformer {
    public RootTransformer() {
        super(Arrays.asList(new TessellatorShaderTransformer(), new SkyRendererTransformer()));
    }
}
