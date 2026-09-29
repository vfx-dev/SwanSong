/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.interfaces;

/// Implemented on various renderers and other rendering classes to attach a context-specific [ShaderRenderGlobal]
///
/// @apiNote Intended for compat with multi-world renderers
public interface ShaderRenderGlobalHolder {
    /// @return the attached **Shader Render Global**
    ShaderRenderGlobal swan$shaderRenderGlobal();
}
