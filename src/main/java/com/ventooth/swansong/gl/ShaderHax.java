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

import com.falsepattern.lib.dependencies.DependencyLoader;
import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.SemanticVersion;
import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.ventooth.swansong.Tags;
import com.ventooth.swansong.asm.CoreLoadingPlugin;
import lombok.SneakyThrows;
import lombok.val;
import org.lwjgl.BufferChecks;
import org.lwjgl.MemoryUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GLContext;
import stubpackage.me.eigenraven.lwjgl3ify.api.Lwjgl3Aware;
import stubpackage.org.lwjgl.opengl.GL20C;
import stubpackage.org.lwjgl.system.MemoryStack;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

//MESA driver segfault workaround
public class ShaderHax {
    private static final boolean lwjgl3;

    static {
        boolean isLwjgl3 = false;
        try {
            Class.forName("org.lwjgl.system.MemoryUtil", false, CoreLoadingPlugin.class.getClassLoader());
            isLwjgl3 = true;
        } catch (Throwable ignored) {
        }
        lwjgl3 = isLwjgl3;
        if (!lwjgl3) {
            DependencyLoader.addMavenRepo("https://repo1.maven.org/maven2/");
            DependencyLoader.loadLibraries(Library.builder()
                                                  .loadingModId(Tags.MOD_ID)
                                                  .groupId("net.java.dev.jna")
                                                  .artifactId("jna")
                                                  .minVersion(new SemanticVersion(5, 17, 0))
                                                  .preferredVersion(new SemanticVersion(5, 17, 0))
                                                  .build());
        }
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
        //java 8 doesn't have Reference.reachabilityFence, so we cheat
        public static Pointer jnaPointerReachability;
        public static PointerBuffer pointerReachability;

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
            val ptrs = PointerBuffer.allocateDirect(1);
            ptrs.put(0, MemoryUtil.getAddress(sourceNullTerminated));
            pointerReachability = ptrs;
            val jnaPointer = new Pointer(MemoryUtil.getAddress(ptrs));
            jnaPointerReachability = jnaPointer;
            fn.invoke(new Object[]{shader, 1, jnaPointer, null});
            jnaPointerReachability = null;
            pointerReachability = null;
        }
    }

    @Lwjgl3Aware
    private static class Lwjgl3 {
        private static void glShaderSource(int shader, ByteBuffer sourceNullTerminated) {
            try (val stack = MemoryStack.stackPush()) {
                val ptr = stack.npointer(sourceNullTerminated);
                GL20C.nglShaderSource(shader, 1, ptr, 0);
            }
        }
    }
}
