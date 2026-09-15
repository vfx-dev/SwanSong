/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.debug;

import com.ventooth.swansong.Share;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.util.StringJoiner;

/// A simple OpenGL debug helper that uses {@link GL11#glGetError()}, useful if error callbacks are unsupported.
public final class GLSimpleDebug {
    private static final Logger log = Share.getLogger("GLSimpleDebug");

    private GLSimpleDebug() {
        throw new UnsupportedOperationException();
    }

    /// Silently flushes OpenGL Errors
    ///
    /// @apiNote Call before {@link #checkError()} to avoid catching other errors
    public static void flushError() {
        while (GL11.glGetError() != GL11.GL_NO_ERROR) {
            Thread.onSpinWait();
        }
    }

    /// Checks for any OpenGL errors, logging if any are found.
    ///
    /// @return Number of errors found
    ///
    /// @apiNote Call after doing something that might error
    public static int checkError() {
        var error = GL11.glGetError();
        if (error == GL11.GL_NO_ERROR) {
            return 0;
        }

        var numErrors = 0;
        val sj = new StringJoiner(", ");
        while (error != GL11.GL_NO_ERROR) {
            numErrors++;

            val errKind = switch (error) {
                case GL11.GL_INVALID_ENUM -> "GL_INVALID_ENUM";
                case GL11.GL_INVALID_VALUE -> "GL_INVALID_VALUE";
                case GL11.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION";
                case GL11.GL_STACK_OVERFLOW -> "GL_STACK_OVERFLOW";
                case GL11.GL_STACK_UNDERFLOW -> "GL_STACK_UNDERFLOW";
                case GL11.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY";
                default -> Integer.toHexString(error);
            };
            sj.add(errKind);

            error = GL11.glGetError();
        }

        log.error("Got {} Errors: {}", numErrors, sj.toString());
        log.error("Trace: ", new Throwable());

        return numErrors;
    }
}
