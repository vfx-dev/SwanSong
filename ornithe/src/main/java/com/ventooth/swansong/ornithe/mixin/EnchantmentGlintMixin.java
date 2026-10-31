package com.ventooth.swansong.ornithe.mixin;

import com.ventooth.swansong.ornithe.OrnitheRender;
import com.ventooth.swansong.shader.StateGraph.Stack;
import net.minecraft.client.render.entity.ItemRenderer;
import net.minecraft.client.render.entity.layer.AbstractArmorLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemRenderer.class, AbstractArmorLayer.class})
public abstract class EnchantmentGlintMixin {
    @Inject(method = "renderEnchantmentGlint",
            at = @At("HEAD"),
            cancellable = true)
    private void swansong$pushGlint(CallbackInfo ci) {
        if (OrnitheRender.isManaged()) {
            if (OrnitheRender.isShadowPass()) {
                ci.cancel();
                return;
            }
            OrnitheRender.push(Stack.ArmorGlint);
        }
    }

    @Inject(method = "renderEnchantmentGlint",
            at = @At("RETURN"))
    private void swansong$popGlint(CallbackInfo ci) {
        OrnitheRender.pop(Stack.ArmorGlint);
    }
}
