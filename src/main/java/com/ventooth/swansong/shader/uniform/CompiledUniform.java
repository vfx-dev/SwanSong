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

import org.joml.Vector2dc;
import org.joml.Vector3dc;
import org.joml.Vector4dc;

public /*sealed*/ interface CompiledUniform {
    @FunctionalInterface
    interface Float extends CompiledUniform {
        double value();
    }

    @FunctionalInterface
    interface Int extends CompiledUniform {
        int value();
    }

    @FunctionalInterface
    interface Bool extends CompiledUniform {
        boolean value();
    }

    @FunctionalInterface
    interface Vec2 extends CompiledUniform {
        Vector2dc value();
    }

    @FunctionalInterface
    interface Vec3 extends CompiledUniform {
        Vector3dc value();
    }

    @FunctionalInterface
    interface Vec4 extends CompiledUniform {
        Vector4dc value();
    }
}
