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


import java.util.Objects;

//TODO convert to record
public final class TaggedLine {
    private final int file;
    private final int line;
    private final String text;
    private final boolean lineBreak;
    private final Tag tag;

    public TaggedLine(int file, int line, String text, boolean lineBreak, Tag tag) {
        this.file = file;
        this.line = line;
        this.text = text;
        this.lineBreak = lineBreak;
        this.tag = tag;
    }

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

    public int file() {
        return file;
    }

    public int line() {
        return line;
    }

    public String text() {
        return text;
    }

    public boolean lineBreak() {
        return lineBreak;
    }

    public Tag tag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (TaggedLine) obj;
        return this.file == that.file &&
               this.line == that.line &&
               Objects.equals(this.text, that.text) &&
               this.lineBreak == that.lineBreak &&
               Objects.equals(this.tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, line, text, lineBreak, tag);
    }

    @Override
    public String toString() {
        return "TaggedLine[" +
               "file=" +
               file +
               ", " +
               "line=" +
               line +
               ", " +
               "text=" +
               text +
               ", " +
               "lineBreak=" +
               lineBreak +
               ", " +
               "tag=" +
               tag +
               ']';
    }


    public enum Tag {
        Standard,
        MultilineComment,
        Macro,
    }
}
