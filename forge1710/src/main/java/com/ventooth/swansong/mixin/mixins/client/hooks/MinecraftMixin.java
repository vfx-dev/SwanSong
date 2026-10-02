/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.hooks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ventooth.swansong.platform.ArchaicShaderEngine;
import com.ventooth.swansong.shader.ShaderStateOld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import cpw.mods.fml.common.FMLCommonHandler;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @WrapOperation(method = "runGameLoop",
                   at = @At(value = "INVOKE",
                            target = "Lcpw/mods/fml/common/FMLCommonHandler;onRenderTickStart(F)V",
                            remap = false),
                   require = 1)
    private void hook_BeginFrame(FMLCommonHandler instance, float subTick, Operation<Void> original) {
        ArchaicShaderEngine.get().beginRenderAllPre();
        if (ArchaicShaderEngine.isInitialized()) {
            ArchaicShaderEngine.get().state().updateSubTick(subTick);
            ArchaicShaderEngine.get().beginRenderAll();
        }
        original.call(instance, subTick);
    }

    @WrapOperation(method = "runGameLoop",
                   at = @At(value = "INVOKE",
                            target = "Lcpw/mods/fml/common/FMLCommonHandler;onRenderTickEnd(F)V",
                            remap = false),
                   require = 1)
    private void hook_EndFrame(FMLCommonHandler instance, float subTick, Operation<Void> original) {
        original.call(instance, subTick);
        if (ArchaicShaderEngine.isInitialized()) {
            ArchaicShaderEngine.get().endRenderAll();
        }
    }

    @Redirect(method = "refreshResources",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/RenderGlobal;loadRenderers()V"),
              require = 1)
    private void hook_ReloadShaderPack(RenderGlobal rg) {
        // We will call loadRenderers() ourselves later when the shader reloads
        ArchaicShaderEngine.get().scheduleShaderPackReload();
    }

    @Inject(method = "updateFramebufferSize",
            at = @At("RETURN"),
            require = 1)
    private void hook_ResizeWindow(CallbackInfo ci) {
        if (ArchaicShaderEngine.isInitialized()) {
            ArchaicShaderEngine.get().scheduleFramebufferResize();
        }
    }
}
