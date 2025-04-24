/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.EventHijacking;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.ventooth.swansong.compat.EventHijacker;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.ASMEventHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.IEventListener;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Method;

@Mixin(value = ASMEventHandler.class,
       remap = false)
public abstract class ASMEventHandlerMixin {
    @Final
    @Shadow
    private SubscribeEvent subInfo;

    @Unique
    private @Nullable EventHijacker.EventHandlerInfo swan$info = null;
    @Unique
    private @Nullable EventHijacker.EventHijack swan$hijack = null;

    @Inject(method = "<init>",
            at = @At("RETURN"),
            require = 1)
    private void grabInfo(Object target, Method method, ModContainer owner, CallbackInfo ci) {
        if (method.getParameterCount() == 1) {
            swan$info = new EventHijacker.EventHandlerInfo(owner, target, method, this.subInfo);
            swan$hijack = EventHijacker.hijack(swan$info);

            // Save memory
            if (swan$hijack == null) {
                swan$info = null;
            }
        }
    }

    @WrapWithCondition(method = "invoke",
                       at = @At(value = "INVOKE",
                                target = "Lcpw/mods/fml/common/eventhandler/IEventListener;invoke(Lcpw/mods/fml/common/eventhandler/Event;)V"),
                       require = 1)
    private boolean hijack(IEventListener original, Event event) {
        if (swan$hijack == null) {
            return true;
        } else {
            swan$hijack.invoke(original, event, swan$info);
            return false;
        }
    }
}
