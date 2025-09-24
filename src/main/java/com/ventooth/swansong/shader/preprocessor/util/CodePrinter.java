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

import com.ventooth.swansong.shader.preprocessor.Option;
import com.ventooth.swansong.shader.preprocessor.TaggedLine;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CodePrinter {
    private final List<String> prelude;
    private final List<TaggedLine> code;
    private final Int2ObjectMap<Option> opts;
    private final PrintWriter f;

    private int file = -1;
    private int line = -1;
    private int lastContent = -1;
    private final List<String> fragments = new ArrayList<>(16);

    private static final boolean compact = true;

    public static void printCode(List<String> prelude,
                                 List<TaggedLine> code,
                                 Int2ObjectMap<Option> opts,
                                 PrintWriter writer) {
        new CodePrinter(prelude, code, opts, writer).sourceToOutputStream();
    }


    private void sourceToOutputStream() {
        for (val pStr : prelude) {
            f.println(pStr);
        }
        val size = code.size();
        for (int i = 0; i < size; i++) {
            val tagged = code.get(i);
            val tag = tagged.tag();
            val lb = tagged.lineBreak();
            val opt = opts.get(i);
            String txt;
            if (opt != null) {
                if (opt.isToggle() && !opt.isEnabled()) {
                    continue;
                }
                txt = null;
            } else {
                txt = tagged.text();
                if (processWhitespace(lb, txt)) {
                    continue;
                }
            }
            fixLineNumbers(tagged);
            if (tag != TaggedLine.Tag.MultilineComment) {
                lastContent = fragments.size();
            }
            if (opt != null) {
                fragments.add(opt.toCode());
            } else {
                fragments.add(txt);
            }
            if (lb) {
                flushLine();
            }
        }
    }

    private boolean processWhitespace(boolean lb, String txt) {
        if (!compact) {
            return false;
        }
        val len = txt.length();
        val fnws = StringUtils.firstNonWhitespace(txt, 0, len);
        if (fnws == -1) {
            if (!lb) {
                fragments.add(txt);
            } else {
                flushLine();
            }
            return true;
        }
        if (len - fnws >= 2 && txt.charAt(fnws) == '/' && txt.charAt(fnws + 1) == '/') {
            return true;
        }
        return false;
    }

    private void fixLineNumbers(TaggedLine tagged) {
        val tFile = tagged.file();
        val tLine = tagged.line();
        if (tFile != file) {
            flushLine();
            f.print("#line ");
            f.print(tLine);
            f.print(" ");
            f.println(tFile);
            line = tLine;
            file = tFile;
        } else if (tLine != line) {
            flushLine();
            int delta = tLine - line;
            if (delta < 7) {
                for (int j = 0; j < delta; j++) {
                    f.print('\n');
                }
            } else {
                f.print("#line ");
                f.println(tLine);
            }
            line = tLine;
        }
    }

    private void flushLine() {
        if (lastContent < 0) {
            fragments.clear();
            return;
        }
        val max = Math.min(fragments.size() - 1, lastContent);
        lastContent = -1;
        line++;

        for (int i = 0; i <= max; i++) {
            var fragment = fragments.get(i);
            if (i == max) {
                int lnws = StringUtils.lastNonWhitespace(fragment);
                if (lnws == -1) {
                    continue;
                }
                f.print(fragment.substring(0, lnws + 1));
            } else {
                f.print(fragment);
            }
        }
        fragments.clear();
        f.print('\n');
    }
}
