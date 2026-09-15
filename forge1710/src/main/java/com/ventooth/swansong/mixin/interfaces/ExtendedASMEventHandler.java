/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.interfaces;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.IEventListener;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Method;

public interface ExtendedASMEventHandler extends IEventListener {
    ModContainer swan$owner();

    Object swan$target();

    Method swan$callback();

    SubscribeEvent swan$subInfo();
}
