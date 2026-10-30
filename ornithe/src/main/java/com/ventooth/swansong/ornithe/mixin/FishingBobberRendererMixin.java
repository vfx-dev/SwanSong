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
import com.ventooth.swansong.ornithe.OrnitheRender;
import com.ventooth.swansong.shader.StateGraph.Stack;
import net.minecraft.client.render.entity.FishingBobberRenderer;
import net.minecraft.client.render.vertex.Tesselator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingBobberRenderer.class)
public abstract class FishingBobberRendererMixin {
    @WrapOperation(method = "render(Lnet/minecraft/entity/FishingBobberEntity;DDDFF)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/render/vertex/Tesselator;end()V",
                            ordinal = 1))
    private void swansong$wrapLine(Tesselator tesselator, Operation<Void> original) {
        if (OrnitheRender.isManaged()) {
            OrnitheRender.bindWhiteTexture();
            OrnitheRender.push(Stack.Leash);
            original.call(tesselator);
            OrnitheRender.pop(Stack.Leash);
        } else {
            original.call(tesselator);
        }
    }
}
