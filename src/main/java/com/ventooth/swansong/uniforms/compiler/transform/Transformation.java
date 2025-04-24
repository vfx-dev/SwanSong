/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms.compiler.transform;

import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface Transformation<Input, Output> {
    Output transform(Input input);

    default List<Output> transform(List<Input> inputs) {
        val result = new ArrayList<Output>(inputs.size());
        for (val input : inputs) {
            result.add(transform(input));
        }
        return Collections.unmodifiableList(result);
    }
}
