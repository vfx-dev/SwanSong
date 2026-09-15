/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.debug;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.profiler.Profiler;

/**
 * @implNote Lower priority, as we want the debug stuff to latch on early.
 */
@Mixin(value = Profiler.class,
       priority = 500)
public abstract class ProfilerMixin {
/*    @Inject(method = "startSection",
            at = @At(value = "HEAD"),
            require = 1)
    private void debugProfilerStart(String name, CallbackInfo ci) {
        if ("Client thread".equals(Thread.currentThread().getName())) {
            GLDebugGroups.push("PROFILER:" + name);
        }
    }

    @Inject(method = "endSection",
            at = @At(value = "HEAD"),
            require = 1)
    private void debugProfilerEnd(CallbackInfo ci) {
        if ("Client thread".equals(Thread.currentThread().getName())) {
            GLDebugGroups.pop();
        }
    }*/
}
