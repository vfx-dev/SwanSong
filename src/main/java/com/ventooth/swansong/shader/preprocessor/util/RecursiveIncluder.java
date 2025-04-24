/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.preprocessor.util;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.shader.preprocessor.FSProvider;
import com.ventooth.swansong.shader.preprocessor.TaggedLine;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class RecursiveIncluder {
    private final FSProvider fs;
    private final List<String> includeStack = new ArrayList<>();
    private List<String> sourceIndices = new ArrayList<>(Collections.singletonList("__INTERNAL__"));
    private List<TaggedLine> lines = new ArrayList<>();

    public boolean read(String path) {
        return read(null, path);
    }

    public void reset() {
        includeStack.clear();
        sourceIndices = new ArrayList<>();
        lines = new ArrayList<>();
        sourceIndices.add("__INTERNAL__");
    }

    public @UnmodifiableView List<String> sourceIndices() {
        return Collections.unmodifiableList(sourceIndices);
    }

    public @UnmodifiableView List<TaggedLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    private boolean isInclude(String line) {
        return line.contains("#include") &&
               line.trim()
                   .startsWith("#include");
    }

    private boolean parseInclude(String line, String file, int lineI, String absPath) {
        val strStart = line.indexOf('"');
        val strEnd = line.lastIndexOf('"');
        if (strStart == -1 || strEnd == -1 || strStart == strEnd) {
            Share.log.error("Invalid include statement at file {} line {}", file, lineI);
            return false;
        }
        return read(absPath, line.substring(strStart + 1, strEnd));
    }

    private boolean read(String source, String path) {
        val absPath = fs.absolutize(source, path);
        if (includeStack.contains(absPath)) {
            return false;
        }
        int fileIndex;
        if (!sourceIndices.contains(absPath)) {
            fileIndex = sourceIndices.size();
            sourceIndices.add(absPath);
        } else {
            fileIndex = sourceIndices.indexOf(absPath);
        }
        includeStack.add(absPath);
        try {
            @Cleanup val input = fs.get(absPath);
            val reader = new LineNumberReader(new InputStreamReader(input));
            String line;
            boolean prevInclude = false;
            while ((line = reader.readLine()) != null) {
                val lineI = reader.getLineNumber();
                if (isInclude(line)) {
                    if (!parseInclude(line, absPath, lineI, absPath)) {
                        return false;
                    }
                    prevInclude = true;
                    continue;
                } else if (prevInclude) {
                    prevInclude = false;

                }
                int hashIndex = line.indexOf('#');
                TaggedLine.Tag tag;
                if (hashIndex == -1) {
                    tag = TaggedLine.Tag.Standard;
                } else {
                    boolean isMacro = true;
                    for (int i = 0; i < hashIndex; i++) {
                        if (!Character.isWhitespace(line.charAt(i))) {
                            isMacro = false;
                            break;
                        }
                    }
                    tag = isMacro ? TaggedLine.Tag.Macro : TaggedLine.Tag.Standard;
                }
                lines.add(new TaggedLine(fileIndex, lineI, line, true, tag));
            }
            return true;
        } catch (FileNotFoundException e) {
            Share.log.trace("File not found: {}", e.getMessage());
            return false;
        } catch (IOException e) {
            Share.log.warn("Caught Exception when reading a file:", e);
            return false;
        } finally {
            if (!includeStack.isEmpty()) {
                includeStack.remove(includeStack.size() - 1);
            }
        }
    }
}
