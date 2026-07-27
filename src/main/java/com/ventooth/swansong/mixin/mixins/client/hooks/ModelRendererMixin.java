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
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GLAllocation;

@Mixin(ModelRenderer.class)
public abstract class ModelRendererMixin {
    @Unique
    private boolean swansong$compiledWithShaderTess = false;

    @Shadow
    private boolean compiled;
    @Shadow
    private int displayList;

    @Inject(method = "render",
            at = @At("HEAD"),
            require = 1)
    private void hook_CheckDisplayListState(CallbackInfo ci) {
        if (!this.compiled) {
            return;
        }

        if (swansong$compiledWithShaderTess != ShaderEngine.isInitialized()) {
            if (this.displayList != 0) {
                GLAllocation.deleteDisplayLists(this.displayList);
                this.displayList = 0;
            }
            this.compiled = false;
        }

        GL11.glEnable(GL11.GL_NORMALIZE);
    }

    @Inject(method = "compileDisplayList",
            at = @At("RETURN"),
            require = 1)
    private void hook_MarkDisplayListState(float scale, CallbackInfo ci) {
        swansong$compiledWithShaderTess = ShaderEngine.isInitialized();
    }
}
