/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.resources.pack;

import com.ventooth.swansong.Share;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModJarContainer {
    public static final Logger log = Share.getLogger();

    private static JarFile jar;

    public static InputStream getInputStream(@NonNull ZipEntry entry) throws IOException {
        return ModJarContainer.get()
                              .getInputStream(entry);
    }

    public static @Nullable ZipEntry getEntry(@NonNull String name) {
        return ModJarContainer.get()
                              .getEntry(name);
    }

    public static JarFile get() {
        if (jar == null) {
            throw new IllegalStateException("Not Initialized!");
        }
        return jar;
    }

    // region Init
    public static void init() {
        if (jar != null) {
            log.debug("Mod jar already loaded?? Yeah, it's here: {}", jar.getName());
            return;
        }
        log.debug("Looking for the uh, mod jar?");

        String jarPath;
        try {
            val jarUrl = InternalShaderPack.class.getProtectionDomain()
                                                 .getCodeSource()
                                                 .getLocation();
            // noinspection CharsetObjectCanBeUsed (Jabel woes..)
            jarPath = URLDecoder.decode(jarUrl.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("Unreachable");
        }
        log.debug("Base path: {}", jarPath);

        val exclIndex = jarPath.indexOf('!');
        if (exclIndex >= 0) {
            jarPath = jarPath.substring(0, exclIndex);
        }
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring("file:".length());
        }
        log.debug("Trimmed path: {}", jarPath);

        try {
            jar = new JarFile(jarPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Found own mod jar: {}", jarPath);
    }
    // endregion
}
