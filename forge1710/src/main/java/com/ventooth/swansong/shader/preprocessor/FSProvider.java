/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.preprocessor;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public interface FSProvider {
    default String absolutize(String source, String path) {
        if (path == null) {
            return null;
        }
        if (path.charAt(0) == '/') {
            return path;
        }
        if (source == null) {
            return "/" + path;
        }
        val currentPath = new ArrayList<>(Arrays.asList(source.substring(1)
                                                              .split("/")));
        //Last element is always the current file, browse to parent dir
        currentPath.remove(currentPath.size() - 1);
        val relPath = new ArrayList<>(Arrays.asList(path.split("/")));
        while (!relPath.isEmpty()) {
            val elem = relPath.remove(0);
            switch (elem) {
                case "." -> {
                }
                case ".." -> {
                    if (!currentPath.isEmpty()) {
                        currentPath.remove(currentPath.size() - 1);
                    }
                }
                default -> currentPath.add(elem);
            }
        }
        return "/" + String.join("/", currentPath);
    }

    @NotNull InputStream get(String path) throws IOException;

    boolean has(String path) throws IOException;
}
