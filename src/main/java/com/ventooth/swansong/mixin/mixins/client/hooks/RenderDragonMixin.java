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

import com.falsepattern.lib.util.RenderUtil;
import com.ventooth.swansong.shader.ShaderEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.entity.boss.EntityDragon;

@Mixin(RenderDragon.class)
public abstract class RenderDragonMixin {
    @Inject(method = "renderEquippedItems(Lnet/minecraft/entity/boss/EntityDragon;F)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderHelper;disableStandardItemLighting()V"),
            require = 1)
    protected void fix_EnderDragonDeathBeams(EntityDragon p_77029_1_, float p_77029_2_, CallbackInfo ci) {
        if (ShaderEngine.isInitialized()) {
            RenderUtil.bindEmptyTexture();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        }
    }
}
