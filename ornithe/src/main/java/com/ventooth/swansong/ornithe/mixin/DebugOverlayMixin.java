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

import com.ventooth.swansong.ornithe.SwanSongOrnithe;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.overlay.DebugOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugOverlay.class)
public abstract class DebugOverlayMixin {
    @Inject(method = "getSystemInfo",
            at = @At("RETURN"))
    private void swansong$info(CallbackInfoReturnable<List<String>> cir) {
        var lines = cir.getReturnValue();
        var version = FabricLoader.getInstance()
                                  .getModContainer("swansong")
                                  .map(mod -> mod.getMetadata()
                                                 .getVersion()
                                                 .getFriendlyString())
                                  .orElse("?");
        lines.add("");
        lines.add("§bSwanSong §9" + version);
        var engine = SwanSongOrnithe.ENGINE;
        if (!engine.isInitializedImpl()) {
            lines.add("§4Shaders disabled");
            return;
        }
        lines.add("§bPack §9" + engine.shaderPacks()
                                      .getCurrentShaderPackName());
        lines.add("§bShadows " + (engine.shadowPassExists() ? "§aEnabled" : "§4Disabled"));
        lines.add("§bShader switches: §r" + engine.prevFrameShaderSwitches());
        if (engine.doGraphLog()) {
            lines.add("Graph log:");
            for (var node : engine.graphLog()) {
                lines.add(node.name());
            }
        }
    }
}
