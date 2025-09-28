package com.ventooth.swansong.mixin.extensions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RendererLivingEntityExt {
    /**
     * Explicitly extracted from the rest of the code to allow for better compat/detection in the future.
     *
     * @param render    this renderer
     * @param entity    the current entity
     * @param modelBase the model reference
     * @param pass      the entity render pass from 0-3
     *
     * @return {@code true} if this render call should be treated as spider eyes
     */
    @Unique
    public static boolean isSpiderEyes(RendererLivingEntity render, Entity entity, ModelBase modelBase, int pass) {
        // Currently only Spider/Enderman/Dragon RENDERERS on pass 0
        if (pass == 0) {
            return render instanceof RenderSpider || render instanceof RenderEnderman || render instanceof RenderDragon;
        } else {
            return false;
        }
    }
}
