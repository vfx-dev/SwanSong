/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.compat.mc.IncreasedMaxTextureSize;

import com.ventooth.swansong.Share;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @ModifyConstant(method = "getGLMaximumTextureSize",
                    constant = @Constant(intValue = 16384),
                    require = 1)
    private static int compat_ExtendMaxTextureSize(int constant) {
        // Note that what the function there does is find the maximum size for an RGBA texture,
        // While the provided max texture size from opengl is for any texture.
        // So the loop makes sense, as the value CAN be smaller.
        val max = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        Share.log.debug("Max (peak ever) texture size gonna be: {}x{}", max, max);
        return max;
    }
}
