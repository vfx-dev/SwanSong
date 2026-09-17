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

public record TaggedLine(int file,
                         int line,
                         String text,
                         boolean lineBreak,
                         Tag tag) {

    public TaggedLine withTag(Tag tag) {
        return new TaggedLine(file, line, text, lineBreak, tag);
    }

    public TaggedLine withText(String text) {
        return new TaggedLine(file, line, text, lineBreak, tag);
    }

    public TaggedLine withText(String text, boolean lineBreak) {
        return new TaggedLine(file, line, text, lineBreak, tag);
    }

    public TaggedLine withTextAndTag(String text, Tag tag) {
        return new TaggedLine(file, line, text, lineBreak, tag);
    }

    public TaggedLine withTextAndTag(String text, boolean lineBreak, Tag tag) {
        return new TaggedLine(file, line, text, lineBreak, tag);
    }

    public enum Tag {
        Standard,
        MultilineComment,
        Macro,
    }
}
