/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client;

import com.ventooth.swansong.mixin.extensions.WorldRendererExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.WorldRenderer;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements WorldRendererExt {
    @Shadow
    public boolean isInFrustum;
    @Shadow
    private boolean isInitialized;
    @Unique
    private boolean swan$frustumBackup;
    @Unique
    private boolean swan$shadowFrustum;

    @Override
    public void swan$backupFrustum() {
        swan$frustumBackup = isInFrustum;
        isInFrustum = swan$shadowFrustum;
    }

    @Override
    public void swan$restoreFrustum() {
        swan$shadowFrustum = isInFrustum;
        isInFrustum = swan$frustumBackup;
    }

    @Override
    public boolean swan$initialized() {
        return isInitialized;
    }
}
