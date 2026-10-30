/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */
package com.ventooth.swansong.ornithe.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ventooth.swansong.ornithe.SwanSongOrnithe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.world.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @WrapOperation(method = "runGame",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/render/GameRenderer;render(FJ)V"),
                   require = 1)
    private void swansong$frame(GameRenderer instance, float tickDelta, long nanoTime, Operation<Void> original) {
        var engine = SwanSongOrnithe.ENGINE;
        engine.beginRenderAllPre();
        if (engine.isInitializedImpl()) {
            engine.state()
                  .updateSubTick(tickDelta);
            engine.beginRenderAll();
        }
        original.call(instance, tickDelta, nanoTime);
        if (engine.isInitializedImpl()) {
            engine.endRenderAll();
        }
    }

    @Redirect(method = "reloadResources",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/render/world/WorldRenderer;reload()V"),
              require = 1)
    private void swansong$reloadShaderPack(WorldRenderer worldRenderer) {
        SwanSongOrnithe.ENGINE.scheduleShaderPackReload();
    }

    @Inject(method = "onResolutionChanged",
            at = @At("RETURN"),
            require = 1)
    private void swansong$resize(CallbackInfo ci) {
        if (SwanSongOrnithe.ENGINE.isInitializedImpl()) {
            SwanSongOrnithe.ENGINE.scheduleFramebufferResize();
        }
    }
}
