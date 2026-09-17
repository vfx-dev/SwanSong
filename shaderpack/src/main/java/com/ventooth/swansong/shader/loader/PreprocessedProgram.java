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

import com.ventooth.swansong.shader.ShaderId;
import com.ventooth.swansong.shader.preprocessor.ShaderPreprocessor;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import org.jetbrains.annotations.Nullable;

public record PreprocessedProgram(ShaderId loc,
                                  ShaderId actualLoc,
                                  String path,
                                  ShaderPreprocessor.PreprocessorStage2Suspend vert,
                                  @Nullable ShaderPreprocessor.PreprocessorStage2Suspend geom,
                                  ShaderPreprocessor.PreprocessorStage2Suspend frag,
                                  ObjectList<String> mipmapEnabled,
                                  IntList renderTargets) {}
