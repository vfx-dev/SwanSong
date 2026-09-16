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

import lombok.ToString;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@ToString
public final class EnvInfo {
    public static final Logger log = Share.getLogger("EnvInfo");

    private static EnvInfo instance;

    public final String mcVersion;
    public final String swansongVersion;

    public final String osName;

    public final String glVendorStr;
    public final String glRendererStr;
    public final String glVersionStr;

    @ToString.Include(rank = -1)
    public final @Unmodifiable List<String> glExtList;
    @ToString.Exclude
    public final @Unmodifiable Set<String> glExtSet;

    public final String glslVersionStr;

    public final int maxTextureSize;
    public final int maxTextureUnits;

    public final OS osPlatform;
    public final GLVendor glVendor;
    public final GLRenderer glRenderer;

    public EnvInfo(String mcVersion,
                   String swansongVersion,
                   String osName,
                   String glVendorStr,
                   String glRendererStr,
                   String glVersionStr,
                   List<String> glExtList,
                   String glslVersionStr,
                   int maxTextureSize,
                   int maxTextureUnits) {
        this.mcVersion = mcVersion;
        this.swansongVersion = swansongVersion;

        this.osName = osName;

        this.glVendorStr = glVendorStr;
        this.glRendererStr = glRendererStr;
        this.glVersionStr = glVersionStr;

        this.glExtList = Collections.unmodifiableList(new ArrayList<>(glExtList));
        this.glExtSet = Collections.unmodifiableSet(new LinkedHashSet<>(this.glExtList));

        this.glslVersionStr = glslVersionStr;

        this.maxTextureSize = maxTextureSize;
        this.maxTextureUnits = maxTextureUnits;

        this.osPlatform = OS.of(this.osName);
        this.glVendor = GLVendor.of(this.glVendorStr);
        this.glRenderer = GLRenderer.of(this.glRendererStr);
    }

    public static void init(EnvInfo info) {
        if (instance != null) {
            log.error("", new IllegalStateException("Already initialized"));
        }
        instance = info;

        log.info("Initialized:\n{}", instance);
    }

    public static EnvInfo get() {
        if (instance == null) {
            throw new IllegalStateException("Not Initialized");
        }
        return instance;
    }

    // TODO: Do we really want this in here?
    public static boolean isMacOS() {
        return get().osPlatform == OS.OSX;
    }

    public enum OS {
        LINUX,
        WINDOWS,
        OSX,
        OTHER;

        private static OS of(String osName) {
            osName = osName.toLowerCase(Locale.ROOT);
            if (osName.contains("linux") || osName.contains("unix")) {
                return LINUX;
            } else if (osName.contains("win")) {
                return WINDOWS;
            } else if (osName.contains("mac")) {
                return OSX;
            } else {
                return OTHER;
            }
        }
    }

    public enum GLVendor {
        ATI,
        INTEL,
        NVIDIA,
        XORG,
        AMD,
        OTHER;

        private static GLVendor of(String glVendor) {
            glVendor = glVendor.toLowerCase(Locale.ROOT);
            if (glVendor.startsWith("ati")) {
                return ATI;
            } else if (glVendor.startsWith("intel")) {
                return INTEL;
            } else if (glVendor.startsWith("nvidia")) {
                return NVIDIA;
            } else if (glVendor.startsWith("amd")) {
                return AMD;
            } else if (glVendor.startsWith("x.org")) {
                return XORG;
            } else {
                return OTHER;
            }
        }
    }

    public enum GLRenderer {
        RADEON,
        GALLIUM,
        INTEL,
        GEFORCE,
        QUADRO,
        MESA,
        OTHER;

        private static GLRenderer of(String glRenderer) {
            glRenderer = glRenderer.toLowerCase(Locale.ROOT);
            if ((glRenderer.startsWith("amd") || glRenderer.startsWith("ati")) || glRenderer.startsWith("radeon")) {
                return RADEON;
            } else if (glRenderer.startsWith("gallium")) {
                return GALLIUM;
            } else if (glRenderer.startsWith("intel")) {
                return INTEL;
            } else if (glRenderer.startsWith("geforce") || glRenderer.startsWith("nvidia")) {
                return GEFORCE;
            } else if (glRenderer.startsWith("quadro") || glRenderer.startsWith("nvs")) {
                return QUADRO;
            } else if (glRenderer.startsWith("mesa")) {
                return MESA;
            }
            return OTHER;
        }
    }
}
