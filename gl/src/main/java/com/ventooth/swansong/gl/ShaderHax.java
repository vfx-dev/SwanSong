/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.gl;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import lombok.SneakyThrows;
import lombok.val;
import org.lwjgl.BufferChecks;
import org.lwjgl.MemoryUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GLContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

//MESA driver segfault workaround
public class ShaderHax {
    private static final boolean lwjgl3;

    static {
        boolean isLwjgl3 = false;
        try {
            Class.forName("org.lwjgl.system.MemoryUtil", false, ShaderHax.class.getClassLoader());
            isLwjgl3 = true;
        } catch (Throwable ignored) {
        }
        lwjgl3 = isLwjgl3;
    }

    public static boolean isLwjgl3() {
        return lwjgl3;
    }

    public static void glShaderSource(int shader, ByteBuffer sourceNullTerminated) {
        if (lwjgl3) {
            Lwjgl3.glShaderSource(shader, sourceNullTerminated);
        } else {
            Lwjgl2.glShaderSource(shader, sourceNullTerminated);
        }
    }

    private static class Lwjgl2 {
        private static final Field glShaderSourceCaps;
        private static final PointerBuffer pointers = PointerBuffer.allocateDirect(1);

        static {
            try {
                glShaderSourceCaps = Class.forName("org.lwjgl.opengl.ContextCapabilities")
                                          .getDeclaredField("glShaderSource");
                glShaderSourceCaps.setAccessible(true);
            } catch (ClassNotFoundException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        @SneakyThrows
        private static synchronized void glShaderSource(int shader, ByteBuffer sourceNullTerminated) {
            val caps = GLContext.getCapabilities();
            long function_pointer = glShaderSourceCaps.getLong(caps);
            val fn = Function.getFunction(new Pointer(function_pointer));
            BufferChecks.checkFunctionAddress(function_pointer);
            BufferChecks.checkDirect(sourceNullTerminated);
            pointers.put(0, MemoryUtil.getAddress(sourceNullTerminated));
            fn.invoke(new Object[]{shader, 1, new Pointer(MemoryUtil.getAddress(pointers)), null});
        }
    }

    private static class Lwjgl3 {
        private static final MethodHandle memAddress;
        private static final MethodHandle nglShaderSource;
        private static final ByteBuffer pointer = ByteBuffer.allocateDirect(Long.BYTES)
                                                            .order(ByteOrder.nativeOrder());

        static {
            try {
                val lookup = MethodHandles.publicLookup();
                val loader = ShaderHax.class.getClassLoader();
                memAddress = lookup.findStatic(Class.forName("org.lwjgl.system.MemoryUtil", true, loader),
                                               "memAddress",
                                               MethodType.methodType(long.class, ByteBuffer.class));
                nglShaderSource = lookup.findStatic(Class.forName("org.lwjgl.opengl.GL20C", true, loader),
                                                    "nglShaderSource",
                                                    MethodType.methodType(void.class,
                                                                          int.class,
                                                                          int.class,
                                                                          long.class,
                                                                          long.class));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        @SneakyThrows
        private static synchronized void glShaderSource(int shader, ByteBuffer sourceNullTerminated) {
            pointer.putLong(0, (long) memAddress.invokeExact(sourceNullTerminated));
            nglShaderSource.invokeExact(shader, 1, (long) memAddress.invokeExact(pointer), 0L);
        }
    }
}
