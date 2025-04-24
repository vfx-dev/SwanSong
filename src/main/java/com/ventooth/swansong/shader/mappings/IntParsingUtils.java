/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.mappings;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class IntParsingUtils {
    public static @Nullable IntList parseUnsignedIntMulti(@NotNull String s) {
        val parts = StringUtils.split(s, ',');
        IntList res = null;
        for (val part : parts) {
            if (part.indexOf('-') < 0) {
                val value = parseUnsignedInt(part);
                if (value >= 0) {
                    if (res == null) {
                        res = new IntArrayList();
                    }
                    res.add(value);
                } else {
                    //TODO log warning
                }
                continue;
            }

            val subParts = StringUtils.split(part, '-');
            if (subParts.length != 2) {
                //TODO log warning
                continue;
            }
            val min = parseUnsignedInt(subParts[0]);
            val max = parseUnsignedInt(subParts[1]);
            if (min < 0 || max < 0 || min > max) {
                //TODO log warning
                continue;
            }
            for (var i = min; i <= max; i++) {
                if (res == null) {
                    res = new IntArrayList();
                }
                res.add(i);
            }
        }
        return res;
    }

    @Range(from = -1,
           to = Integer.MAX_VALUE)
    public static int parseUnsignedInt(@NotNull String s) {
        return parseUnsignedInt(s, 10);
    }

    //Unsigned-optimized, non-throwing version of parseInt from the JDK
    @Range(from = -1,
           to = Integer.MAX_VALUE)
    public static int parseUnsignedInt(@NotNull String s, int radix) {
        var i = 0;
        val len = s.length() - 1;
        val limit = -Integer.MAX_VALUE;

        if (len == -1) {
            return -1;
        }
        char c;
        while (Character.isWhitespace(c = s.charAt(i))) {
            i++;
        }
        if (c < '0') { // Possible leading "+" or "-"
            //No negative numbers
            return -1;
        }
        val multmin = limit / radix;
        var result = 0;
        while (i < len) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            //Avoids trimming in parseUnsignedIntMulti
            if (Character.isWhitespace(c)) {
                break;
            }
            val digit = Character.digit(c, radix);
            if (digit < 0 || result < multmin) {
                return -1;
            }
            result *= radix;
            if (result < limit + digit) {
                return -1;
            }
            result -= digit;
            c = s.charAt(++i);
        }
        return -result;
    }
}
