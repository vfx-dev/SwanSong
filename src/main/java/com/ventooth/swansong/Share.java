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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Share {
    public static final String MC_VERSION = "1.7.10";
    public static final Logger log = LogManager.getLogger(Tags.MOD_NAME);

    // TODO: Some debug? Verbose? Logging? :idk:
    public static Logger getLogger() {
        var rootClassName = new Throwable().getStackTrace()[1].getClassName();
        val idx = rootClassName.lastIndexOf('.');
        if (idx != -1) {
            rootClassName = rootClassName.substring(idx + 1);
        }
        return getLogger(rootClassName);
    }

    public static Logger getLogger(String name) {
        return LogManager.getLogger(Tags.MOD_NAME + "|" + name);
    }
}
