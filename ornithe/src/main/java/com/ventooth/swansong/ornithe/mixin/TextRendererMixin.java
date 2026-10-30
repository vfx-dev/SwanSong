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

import com.ventooth.swansong.ornithe.OrnitheRender;
import com.ventooth.swansong.shader.StateGraph.Stack;
import net.minecraft.client.render.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TextRenderer.class,
       priority = 1100)
public abstract class TextRendererMixin {
    @Inject(method = {"drawWithShadow(Ljava/lang/String;FFI)I",
                      "draw(Ljava/lang/String;III)I",
                      "draw(Ljava/lang/String;FFIZ)I"},
            at = @At("HEAD"),
            require = 3)
    private void swansong$pushText(CallbackInfoReturnable<Integer> cir) {
        OrnitheRender.push(Stack.Text);
    }

    @Inject(method = {"drawWithShadow(Ljava/lang/String;FFI)I",
                      "draw(Ljava/lang/String;III)I",
                      "draw(Ljava/lang/String;FFIZ)I"},
            at = @At("RETURN"),
            require = 3)
    private void swansong$popText(CallbackInfoReturnable<Integer> cir) {
        OrnitheRender.pop(Stack.Text);
    }
}
