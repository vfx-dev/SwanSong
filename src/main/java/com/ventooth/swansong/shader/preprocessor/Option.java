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

import com.falsepattern.lib.util.MathUtil;
import com.ventooth.swansong.MicroCache;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.AllArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleConsumer;
import java.util.regex.Pattern;

@AllArgsConstructor
public abstract class Option {
    public static final List<Value.Bool> TOGGLE_VALUES = Collections.unmodifiableList(Arrays.asList(Value.Bool.False,
                                                                                                    Value.Bool.True));
    public final String name;
    protected final State state;
    protected final List<? extends Value> legalValues;
    protected final int defaultValue;
    protected int currentValue;

    public static void purgeCaches() {
        Const.cacheC.purge();
        Const.cacheNC.purge();
        Define.cache.purge();
    }

    public Value getCurrentValue() {
        return legalValues.get(currentValue);
    }

    public void setCurrentValue(Value value) {
        if (isReadonly()) {
            return;
        }
        currentValue = valueIndexOf(value, legalValues);
    }

    public void nextValue() {
        if (isReadonly()) {
            return;
        }
        currentValue = (currentValue + 1) % legalValues.size();
    }

    public void prevValue() {
        if (isReadonly()) {
            return;
        }
        val s = legalValues.size();
        currentValue = (currentValue - 1 + s) % s;
    }

    public Value getDefaultValue() {
        return legalValues.get(defaultValue);
    }

    public boolean isDefaultValue() {
        return defaultValue == currentValue;
    }

    public void setToDefault() {
        if (isReadonly()) {
            return;
        }
        currentValue = defaultValue;
    }

    public int getValueCount() {
        return legalValues.size();
    }

    public int getValueIndex() {
        return currentValue;
    }

    public void setValueIndex(@Range(from = 0,
                                     to = Integer.MAX_VALUE) int index) {
        if (index < 0 || index >= legalValues.size()) {
            throw new IndexOutOfBoundsException();
        }
        if (isReadonly()) {
            return;
        }
        currentValue = index;
    }

    public Value getValueByIndex(@Range(from = 0,
                                        to = Integer.MAX_VALUE) int index) {
        return legalValues.get(index);
    }

    public boolean isToggle() {
        return legalValues == TOGGLE_VALUES;
    }

    public boolean isReadonly() {
        return state != State.Mutable;
    }

    public boolean isConfigurable() {
        return state != State.Unconfigurable;
    }

    public boolean isEnabled() {
        if (!isToggle()) {
            throw new IllegalStateException();
        }
        val tgl = (Value.Bool) getCurrentValue();
        return switch (tgl) {
            case True -> true;
            case False -> false;
        };
    }

    public ObjectList<String> valueStrings() {
        val result = new ObjectArrayList<String>();
        for (val value : legalValues) {
            result.add(value.toString());
        }
        return ObjectLists.unmodifiable(result);
    }

    public String toProps() {
        return name + '=' + getCurrentValue().toString();
    }

    public abstract Option copy(boolean readonly);

    public abstract String toCode();

    public abstract String uniqueName();

    //language=regexp
    private static final String REGEX_NAME = "(\\w+)";
    //language=regexp
    private static final String REGEX_VALUE = "([\\w.-]+)";
    //language=regexp
    private static final String REGEX_ALLOWED = "\\s*(?://(?:.*?\\[(.*?)])?.*)?$";

    public static class Const extends Option {
        private static final int GROUP_TYPE = 1;
        private static final int GROUP_NAME = 2;
        private static final int GROUP_VALUE = 3;
        private static final int GROUP_ALLOWED = 4;
        private static final Pattern CONST_REGEX = Pattern.compile("^\\s*const\\s+" +
                                                                   REGEX_NAME +
                                                                   "\\s+" +
                                                                   REGEX_NAME +
                                                                   "\\s*=\\s*" +
                                                                   REGEX_VALUE +
                                                                   "\\s*;" +
                                                                   REGEX_ALLOWED);

        public final String type;
        public final boolean definedInComment;

        private Const(String type,
                      String name,
                      State state,
                      List<? extends Value> legalValues,
                      boolean definedInComment,
                      int defaultValue,
                      int currentValue) {
            super(name, state, legalValues, defaultValue, currentValue);
            this.type = type;
            this.definedInComment = definedInComment;
        }

        public static Int2ObjectMap<Option> find(List<TaggedLine> code, boolean readonly) {
            val output = new Int2ObjectRBTreeMap<Option>();
            val size = code.size();
            for (int i = 0; i < size; i++) {
                val line = code.get(i);
                val opt = switch (line.tag()) {
                    case Standard -> Const.get(line.text(), readonly, false);
                    case MultilineComment -> Const.get(line.text(), true, true);
                    case Macro -> null;
                };
                if (opt != null) {
                    output.put(i, opt);
                }
            }
            return output;
        }

        private static final MicroCache<String, Const> cacheC = new MicroCache<>();
        private static final MicroCache<String, Const> cacheNC = new MicroCache<>();

        private static Const get(String code, boolean readonly, boolean definedInComment) {
            val v = definedInComment ? cacheC.getCached(code, c -> getUncached(c, true))
                                     : cacheNC.getCached(code, c -> getUncached(c, false));
            return v == null ? null : v.copy(readonly);
        }

        private static Const getUncached(String code, boolean definedInComment) {
            val match = CONST_REGEX.matcher(code);
            if (!match.matches()) {
                return null;
            }
            val type = match.group(GROUP_TYPE);
            val name = match.group(GROUP_NAME);
            val value = Value.detect(match.group(GROUP_VALUE));
            val allowed = Option.tryParseAllowedGroup(value, match.group(GROUP_ALLOWED));
            if (allowed == null) {
                if (value instanceof Value.Bool) {
                    int idx = valueIndexOf(value, TOGGLE_VALUES);
                    return new Const(type, name, State.Unconfigurable, TOGGLE_VALUES, definedInComment, idx, idx);
                }
                return new Const(type,
                                 name,
                                 State.Unconfigurable,
                                 Collections.singletonList(value),
                                 definedInComment,
                                 0,
                                 0);
            } else {
                int idx = valueIndexOf(value, allowed);
                return new Const(type, name, State.Readonly, allowed, definedInComment, idx, idx);
            }
        }

        @Override
        public Const copy(boolean readonly) {
            if (state == State.Unconfigurable) {
                return this;
            }
            if (readonly && state == State.Readonly) {
                return this;
            }
            return new Const(type,
                             name,
                             readonly ? State.Readonly : State.Mutable,
                             legalValues,
                             definedInComment,
                             defaultValue,
                             currentValue);
        }

        @Override
        public String toCode() {
            return "const " + type + " " + name + " = " + getCurrentValue() + ";";
        }

        @Override
        public String uniqueName() {
            return "C\uffff" + name;
        }
    }

    public static class Define extends Option {
        private static final int GROUP_DISABLED = 1;
        private static final int GROUP_NAME = 2;
        private static final int GROUP_VALUE = 3;
        private static final int GROUP_ALLOWED = 4;
        private static final Pattern DEFINE_REGEX = Pattern.compile("^\\s*(//)?\\s*#define\\s+" +
                                                                    REGEX_NAME +
                                                                    "(?:\\s+" +
                                                                    REGEX_VALUE +
                                                                    ")?" +
                                                                    REGEX_ALLOWED);

        private Define(String name,
                       State state,
                       List<? extends Value> legalValues,
                       int defaultValue,
                       int currentValue) {
            super(name, state, legalValues, defaultValue, currentValue);
        }

        public static Int2ObjectMap<Option> find(List<TaggedLine> code, boolean readonly) {
            val output = new Int2ObjectRBTreeMap<Option>();
            val size = code.size();
            for (int i = 0; i < size; i++) {
                val line = code.get(i);
                val opt = switch (line.tag()) {
                    case Standard, Macro -> Define.get(line.text(), readonly);
                    case MultilineComment -> null;
                };
                if (opt != null) {
                    output.put(i, opt);
                }
            }
            return output;
        }

        private static final MicroCache<String, Define> cache = new MicroCache<>();

        private static Define get(String code, boolean readonly) {
            val v = cache.getCached(code, Define::getUncached);
            return v == null ? null : v.copy(readonly);
        }

        private static Define getUncached(String code) {
            val match = DEFINE_REGEX.matcher(code);
            if (!match.matches()) {
                return null;
            }
            val disabled = match.group(GROUP_DISABLED);
            val name = match.group(GROUP_NAME);
            val valueStr = match.group(GROUP_VALUE);
            if (valueStr != null) {
                if (disabled != null) {
                    return null;
                }
                val value = Value.detect(valueStr);
                val allowed = Option.tryParseAllowedGroup(value, match.group(GROUP_ALLOWED));
                if (allowed == null) {
                    return new Define(name, State.Unconfigurable, Collections.singletonList(value), 0, 0);
                } else {
                    val idx = valueIndexOf(value, allowed);
                    return new Define(name, State.Readonly, allowed, idx, idx);
                }
            } else {
                val idx = disabled == null ? 1 : 0;
                return new Define(name, State.Readonly, TOGGLE_VALUES, idx, idx);
            }
        }

        @Override
        public Define copy(boolean readonly) {
            if (state == State.Unconfigurable) {
                return this;
            }
            if (readonly && state == State.Readonly) {
                return this;
            }
            return new Define(name, readonly ? State.Readonly : State.Mutable, legalValues, defaultValue, currentValue);
        }

        @Override
        public String toCode() {
            val res = new StringBuilder();
            if (legalValues == TOGGLE_VALUES) {
                if (getCurrentValue() == Value.Bool.False) {
                    res.append("//");
                }
                res.append("#define ")
                   .append(name);
                return res.toString();
            }
            res.append("#define ")
               .append(name)
               .append(' ')
               .append(getCurrentValue());
            return res.toString();
        }

        @Override
        public String uniqueName() {
            return "D\uffff" + name;
        }
    }

    protected static @Nullable @Unmodifiable List<? extends Value> tryParseAllowedGroup(Value initialValue, String allowedGroup) {
        if (allowedGroup == null) {
            return null;
        } else {
            val allowedStr = allowedGroup.trim()
                                         .split("\\s+");
            if (allowedStr.length == 0) {
                return null;
            } else {
                boolean anyMatch = false;
                val allowed = new ArrayList<Value>();
                for (val str : allowedStr) {
                    val v = Value.detect(str);
                    if (!anyMatch) {
                        anyMatch = valueMatches(v, initialValue);
                    }
                    allowed.add(v);
                }
                if (allowed.size() == 2 && allowed.get(0) == Value.Bool.False && allowed.get(1) == Value.Bool.True) {
                    return TOGGLE_VALUES;
                }
                if (!anyMatch) {
                    allowed.add(0, initialValue);
                }
                return Collections.unmodifiableList(allowed);
            }
        }
    }

    private static int valueIndexOf(Value expect, List<? extends Value> possible) {
        val size = possible.size();
        for (int i = 0; i < size; i++) {
            if (valueMatches(expect, possible.get(i))) {
                return i;
            }
        }
        return 0;
    }

    public static boolean valueMatches(Value a, Value b) {
        return switch (a.type()) {
            case Toggle -> switch (b.type()) {
                case Toggle -> a == b;
                case Int -> (a == Value.Bool.True ? 1 : 0) == ((Value.Int) b).v;
                case Double -> (a == Value.Bool.True ? 1 : 0) == ((Value.Dbl) b).v;
                case Str -> false;
            };
            case Int -> switch (b.type()) {
                case Toggle -> ((Value.Int) a).v == (b == Value.Bool.True ? 1 : 0);
                case Int -> ((Value.Int) a).v == ((Value.Int) b).v;
                case Double -> ((Value.Int) a).v == ((Value.Dbl) b).v;
                case Str -> false;
            };
            case Double -> switch (b.type()) {
                case Toggle -> ((Value.Dbl) a).v == (b == Value.Bool.True ? 1 : 0);
                case Int -> ((Value.Dbl) a).v == ((Value.Int) b).v;
                case Double -> ((Value.Dbl) a).v == ((Value.Dbl) b).v;
                case Str -> false;
            };
            case Str -> switch (b.type()) {
                case Toggle, Int, Double -> false;
                case Str -> Objects.equals(((Value.Str) a).v, ((Value.Str) b).v);
            };
        };
    }

    public enum ValueType {
        Int,
        Double,
        Str,
        Toggle;
    }

    public enum State {
        Mutable,
        Readonly,
        Unconfigurable
    }

    public interface Value {
        static Value detect(String value) {
            if ("true".equalsIgnoreCase(value)) {
                return Bool.True;
            } else if ("false".equalsIgnoreCase(value)) {
                return Bool.False;
            }
            if (value.indexOf('.') < 0) {
                try {
                    return new Value.Int(Integer.parseInt(value));
                } catch (NumberFormatException ignored) {
                }
            }
            try {
                return new Value.Dbl(java.lang.Double.parseDouble(value));
            } catch (NumberFormatException ignored) {
            }
            return new Value.Str(value);
        }

        ValueType type();

        @Nullable Boolean boolValue();

        @Nullable Integer intValue();

        @Nullable Double doubleValue();

        default void safeInt(IntConsumer out) {
            val v = intValue();
            if (v == null) {
                return;
            }
            out.accept((int) v);
        }

        default void safeDouble(DoubleConsumer out) {
            val v = doubleValue();
            if (v == null) {
                return;
            }
            out.accept(v);
        }

        default void safeDouble(double min, double max, DoubleConsumer out) {
            val v = doubleValue();
            if (v == null) {
                return;
            }
            val clamped = MathUtil.clamp(v, min, max);
            out.accept(clamped);
        }

        default void boolFused(Runnable out) {
            val v = boolValue();
            if (v == null || !v) {
                return;
            }
            out.run();
        }

        default void boolFused(BooleanConsumer out) {
            boolFused(() -> out.accept(true));
        }

        default void boolFused(BooleanConsumer out1, BooleanConsumer out2) {
            boolFused(() -> {
                out1.accept(true);
                out2.accept(true);
            });
        }

        //TODO convert to record
        static final class Int implements Value {
            private final int v;

            public Int(int v) {
                this.v = v;
            }

            @Override
            public String toString() {
                return Integer.toString(v);
            }

            @Override
            public ValueType type() {
                return ValueType.Int;
            }

            @Override
            public Boolean boolValue() {
                return null;
            }

            @Override
            public Integer intValue() {
                return v;
            }

            @Override
            public Double doubleValue() {
                return (double) v;
            }

            public int v() {
                return v;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Int) obj;
                return this.v == that.v;
            }

            @Override
            public int hashCode() {
                return Objects.hash(v);
            }

        }

        //TODO convert to record
        static final class Dbl implements Value {
            private final double v;

            public Dbl(double v) {
                this.v = v;
            }

            @Override
            public String toString() {
                return Double.toString(v);
            }

            @Override
            public ValueType type() {
                return ValueType.Double;
            }

            @Override
            public Boolean boolValue() {
                return null;
            }

            @Override
            public Integer intValue() {
                return null;
            }

            @Override
            public Double doubleValue() {
                return v;
            }

            public double v() {
                return v;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Dbl) obj;
                return Double.doubleToLongBits(this.v) == Double.doubleToLongBits(that.v);
            }

            @Override
            public int hashCode() {
                return Objects.hash(v);
            }

        }

        //TODO convert to record
        static final class Str implements Value {
            private final String v;

            public Str(String v) {
                this.v = v;
            }

            @Override
            public String toString() {
                return v;
            }

            @Override
            public ValueType type() {
                return ValueType.Str;
            }

            @Override
            public @Nullable Boolean boolValue() {
                return null;
            }

            @Override
            public Integer intValue() {
                return null;
            }

            @Override
            public Double doubleValue() {
                return null;
            }

            public String v() {
                return v;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Str) obj;
                return Objects.equals(this.v, that.v);
            }

            @Override
            public int hashCode() {
                return Objects.hash(v);
            }

        }

        enum Bool implements Value {
            False,
            True;

            @Override
            public String toString() {
                return switch (this) {
                    case True -> "true";
                    case False -> "false";
                };
            }

            @Override
            public ValueType type() {
                return ValueType.Toggle;
            }

            public Bool toggle() {
                return switch (this) {
                    case True -> False;
                    case False -> True;
                };
            }

            public static Bool of(boolean value) {
                return value ? True : False;
            }

            @Override
            public Boolean boolValue() {
                return switch (this) {
                    case True -> true;
                    case False -> false;
                };
            }

            @Override
            public Integer intValue() {
                return null;
            }

            @Override
            public Double doubleValue() {
                return null;
            }
        }
    }
}
