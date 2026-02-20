/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.api;

import org.jetbrains.annotations.ApiStatus;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Base class for all SwanSong events.
 * <p>
 * All events will be fired on the {@link MinecraftForge#EVENT_BUS}
 */
@ApiStatus.AvailableSince("1.3.0")
public abstract class SwanSongEvent extends Event {
    @Override
    public abstract boolean isCancelable();
}
