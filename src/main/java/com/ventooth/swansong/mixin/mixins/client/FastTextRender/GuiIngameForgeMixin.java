/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.FastTextRender;

import com.llamalad7.mixinextras.sugar.Local;
import com.ventooth.swansong.FastTextRender.FastFontRenderer;
import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;

import java.util.ArrayList;

@Mixin(GuiIngameForge.class)
public abstract class GuiIngameForgeMixin {
    @Shadow
    private FontRenderer fontrenderer;

    @Shadow
    @Final
    private static int WHITE;

    @Redirect(method = "renderHUDText",
              at = @At(value = "INVOKE",
                       target = "Lcpw/mods/fml/common/eventhandler/EventBus;post(Lcpw/mods/fml/common/eventhandler/Event;)Z",
                       ordinal = 0),
              slice = @Slice(from = @At(value = "NEW",
                                        target = "(Lnet/minecraftforge/client/event/RenderGameOverlayEvent;Ljava/util/ArrayList;Ljava/util/ArrayList;)Lnet/minecraftforge/client/event/RenderGameOverlayEvent$Text;")),
              remap = false,
              require = 1)
    private boolean killLastEvent(EventBus instance,
                                  Event event,
                                  @Local(ordinal = 0,
                                         argsOnly = true) int width,
                                  @Local(ordinal = 0) ArrayList<String> left,
                                  @Local(ordinal = 1) ArrayList<String> right) {
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return true;
        }

        if (left.isEmpty() && right.isEmpty()) {
            return true;
        }
        val ffr = (FastFontRenderer) fontrenderer;
        val thisStart = ffr.swan$beginTessellating();
        for (int x = 0, n = left.size(); x < n; x++) {
            String msg = left.get(x);
            if (msg == null) {
                continue;
            }
            fontrenderer.drawStringWithShadow(msg, 2, 2 + x * 10, WHITE);
        }

        for (int x = 0, n = right.size(); x < n; x++) {
            String msg = right.get(x);
            if (msg == null) {
                continue;
            }
            int w = fontrenderer.getStringWidth(msg);
            fontrenderer.drawStringWithShadow(msg, width - w - 10, 2 + x * 10, WHITE);
        }
        ffr.swan$draw(thisStart);

        return true;
    }
}
