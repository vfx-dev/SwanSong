package com.ventooth.swansong.mixin.mixins.client.compat.thaumcraft;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thaumcraft.client.renderers.entity.RenderCultist;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.MinecraftForgeClient;

@Mixin(RenderCultist.class)
public abstract class RenderCultistMixin {
    @WrapWithCondition(method = "doRender",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/renderer/entity/RenderBiped;doRender(Lnet/minecraft/entity/Entity;DDDFF)V"),
                       require = 1)
    private boolean fix_RenderBodyPass0(RenderBiped instance,
                                        Entity entity,
                                        double x,
                                        double y,
                                        double z,
                                        float entityYaw,
                                        float partialTicks) {
        return MinecraftForgeClient.getRenderPass() == 0;
    }

    @WrapOperation(method = "doRender",
                   at = @At(value = "INVOKE",
                                target = "Lthaumcraft/client/renderers/entity/RenderCultist;drawFloatyLine(DDDDDDFILjava/lang/String;FFF)V"),
                   require = 1)
    private void fix_RenderFloatyLinePass1(RenderCultist instance,
                                           double x,
                                           double y,
                                           double z,
                                           double x2,
                                           double y2,
                                           double z2,
                                           float partialTicks,
                                           int color,
                                           String texture,
                                           float speed,
                                           float distance,
                                           float width,
                                           Operation<Void> original) {
        // Only on translucent pass
        if (MinecraftForgeClient.getRenderPass() == 1) {
            if (ShaderEngine.graph.isManaged()) {
                // Only on render pass, not on shadow
                if (ShaderEngine.graph.isRender()) {

                    // Treat this part as a particle, as to avoid any benign PBR logic
                    ShaderEngine.graph.push(StateGraph.Stack.EntityParticle);
                    original.call(instance, x, y, z, x2, y2, z2, partialTicks, color, texture, speed, distance, width);
                    ShaderEngine.graph.pop(StateGraph.Stack.EntityParticle);
                }
            } else {
                original.call(instance, x, y, z, x2, y2, z2, partialTicks, color, texture, speed, distance, width);
            }
        }
    }
}
