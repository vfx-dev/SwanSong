/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * Keyed state-tracker helper
 */
public class StackStateTracker<T> {
    private final ObjectList<T> storage = new ObjectArrayList<>();
    private final @Nullable ObjectList<Throwable> traces;
    private final ObjectList<Key> keys = new ObjectArrayList<>();

    public StackStateTracker(boolean saveStackTraceOnPush) {
        if (saveStackTraceOnPush) {
            traces = new ObjectArrayList<>();
        } else {
            traces = null;
        }
    }

    /**
     * Push with a reusable key. No object spam.
     */
    public void push(Key key, T state) {
        storage.add(state);
        keys.add(key);
        if (traces != null) {
            traces.add(new Throwable());
        }
    }

    /**
     * Creates a new key on each push. Yes object spam.
     *
     * @return A unique key. Compare by identity.
     */
    public Key push(String hintText, T state) {
        var key = new Key(hintText);
        push(key, state);
        return key;
    }

    /**
     * Pops the topmost state. Validates that the key matches, throws on failure or empty stack.
     */
    public T pop(Key key) throws IllegalStateException {
        if (keys.isEmpty()) {
            throw new IllegalStateException("Tried to pop with key " + key + " on an empty stack!");
        }
        var index = keys.size() - 1;
        var topKey = keys.get(index);
        if (topKey != key) {
            var builder = new StringBuilder("Tried to pop with key ").append(key)
                                                                     .append(" when stack had ")
                                                                     .append(topKey)
                                                                     .append(" on top!");
            addStackState(builder);
            var exc = new IllegalStateException(builder.toString());
            if (traces != null) {
                var pushTrace = traces.get(index);
                exc.initCause(pushTrace);
            }
            throw exc;
        }
        keys.remove(index);
        if (traces != null) {
            traces.remove(index);
        }
        return storage.remove(index);
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public void addStackState(StringBuilder b) {
        b.append("\nStack contents (from top to bottom):\n");
        for (int i = keys.size() - 1; i >= 0; i--) {
            var key = keys.get(i);
            b.append('\t')
             .append(key.toString())
             .append('\n');
        }
    }

    @RequiredArgsConstructor
    public static final class Key {
        public final String hintName;

        @Override
        public String toString() {
            return hintName;
        }
    }
}
