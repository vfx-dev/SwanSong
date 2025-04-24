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

import com.ventooth.swansong.debug.GLDebugGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;

/**
 * @implNote Lower priority, as we want the debug stuff to latch on early.
 */
@Mixin(value = Minecraft.class,
       priority = 500)
public abstract class MinecraftMixin {
    @Inject(method = "runGameLoop",
            at = @At(value = "HEAD"),
            require = 1)
    private void debug_group$pushGameLoop(CallbackInfo ci) {
        GLDebugGroups.GAME_LOOP.push();
    }

    @Inject(method = "runGameLoop",
            at = @At(value = "RETURN"),
            require = 1)
    private void debug_group$popGameLoop(CallbackInfo ci) {
        GLDebugGroups.GAME_LOOP.pop();
    }
}
