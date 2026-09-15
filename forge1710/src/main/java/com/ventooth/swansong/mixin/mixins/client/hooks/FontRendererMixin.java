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

import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.FontRenderer;

@Mixin(value = FontRenderer.class,
       priority = 1100)// Applies after other font render stuff
public abstract class FontRendererMixin {
    @Inject(method = {"drawStringWithShadow(Ljava/lang/String;III)I",
                      "drawString(Ljava/lang/String;III)I",
                      "drawString(Ljava/lang/String;IIIZ)I"},
            at = @At("HEAD"),
            require = 3)
    private void graph_PushText(CallbackInfoReturnable<Integer> cir) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.push(StateGraph.Stack.Text);
        }
    }

    @Inject(method = {"drawStringWithShadow(Ljava/lang/String;III)I",
                      "drawString(Ljava/lang/String;III)I",
                      "drawString(Ljava/lang/String;IIIZ)I"},
            at = @At("RETURN"),
            require = 3)
    private void graph_PopText(CallbackInfoReturnable<Integer> cir) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.pop(StateGraph.Stack.Text);
        }
    }
}
