/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package stubpackage.org.lwjgl.system;

import java.nio.Buffer;

public class MemoryStack implements AutoCloseable {

    public static MemoryStack stackPush() {
        return null;
    }

    public long npointer(Buffer value) {
        return 0L;
    }

    @Override
    public void close() {

    }
}
