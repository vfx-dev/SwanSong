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
import com.ventooth.swansong.ornithe.OrnitheSkyBox;
import com.ventooth.swansong.ornithe.SwanSongOrnithe;
import com.ventooth.swansong.shader.MCRenderStage;
import com.ventooth.swansong.shader.StateGraph.Node;
import com.ventooth.swansong.shader.StateGraph.Stack;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.Entities;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    private static boolean swansong$active() {
        return SwanSongOrnithe.ENGINE.isInitializedImpl();
    }

    private static void swansong$moveTo(Node node) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.graph()
                                  .moveTo(node);
        }
    }

    private static void swansong$moveTo(Node render, Node shadow) {
        if (swansong$active()) {
            var graph = SwanSongOrnithe.ENGINE.graph();
            graph.moveTo(graph.isShadowPass() ? shadow : render);
        }
    }

    private static void swansong$renderStage(MCRenderStage stage) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.state()
                                  .updateRenderStage(stage);
        }
    }

    @WrapOperation(method = "renderSky(FI)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/render/platform/GlStateManager;enableTexture()V"))
    private void swansong$skyTextured(Operation<Void> original) {
        if (swansong$active() && SwanSongOrnithe.ENGINE.graph()
                                                       .isSky()) {
            SwanSongOrnithe.ENGINE.graph()
                                  .moveTo(Node.RenderSkyTextured);
        }
        original.call();
    }

    @WrapOperation(method = "renderSky(FI)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/render/platform/GlStateManager;disableTexture()V"))
    private void swansong$skyBasic(Operation<Void> original) {
        if (swansong$active() && SwanSongOrnithe.ENGINE.graph()
                                                       .isSky()) {
            SwanSongOrnithe.ENGINE.graph()
                                  .moveTo(Node.RenderSkyBasic);
        }
        original.call();
    }

    @Inject(method = "renderSky(FI)V",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/render/world/WorldRenderer;useVbo:Z",
                     ordinal = 0),
            require = 1)
    private void swansong$preSkyList(CallbackInfo ci) {
        if (swansong$active()) {
            OrnitheSkyBox.preSkyList();
        }
    }

    @Inject(method = "renderSky(FI)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V",
                     ordinal = 4),
            require = 1)
    private void swansong$preCelestialRotate(CallbackInfo ci) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.state()
                                  .preCelestialRotate();
        }
    }

    @Inject(method = "renderSky(FI)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V",
                     ordinal = 4,
                     shift = At.Shift.AFTER),
            require = 1)
    private void swansong$postCelestialRotate(CallbackInfo ci) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.state()
                                  .postCelestialRotate();
        }
    }

    @Inject(method = "renderSky(FI)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/texture/TextureManager;bind(Lnet/minecraft/resource/Identifier;)V",
                     ordinal = 0),
            require = 1)
    private void swansong$sun(CallbackInfo ci) {
        swansong$renderStage(MCRenderStage.SUN);
    }

    @Inject(method = "renderSky(FI)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/texture/TextureManager;bind(Lnet/minecraft/resource/Identifier;)V",
                     ordinal = 1),
            require = 1)
    private void swansong$moon(CallbackInfo ci) {
        swansong$renderStage(MCRenderStage.MOON);
    }

    @Inject(method = "renderSky(FI)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/platform/GlStateManager;color4f(FFFF)V",
                     ordinal = 1),
            require = 1)
    private void swansong$stars(CallbackInfo ci) {
        swansong$renderStage(MCRenderStage.STARS);
    }

    @Inject(method = "renderEntities",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                     args = "ldc=global"),
            require = 1)
    private void swansong$weatherEntities(CallbackInfo ci) {
        swansong$moveTo(Node.RenderWeatherEntities0, Node.ShadowEntities0);
    }

    @Inject(method = "renderEntities",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                     args = "ldc=entities"),
            require = 1)
    private void swansong$entities(CallbackInfo ci) {
        swansong$moveTo(Node.RenderEntities0, Node.ShadowEntities0);
    }

    @Inject(method = "renderEntities",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                     args = "ldc=blockentities"),
            require = 1)
    private void swansong$blockEntities(CallbackInfo ci) {
        swansong$moveTo(Node.RenderBlockEntities0, Node.ShadowBlockEntities0);
    }

    @WrapOperation(method = "renderEntities",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;F)Z"))
    private boolean swansong$entityId(EntityRenderDispatcher instance, Entity entity, float tickDelta, Operation<Boolean> original) {
        if (swansong$active()) {
            SwanSongOrnithe.ENGINE.state()
                                  .nextEntity(Entities.getId(entity));
        }
        return original.call(instance, entity, tickDelta);
    }

    @WrapOperation(method = "renderEntities",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FI)V"))
    private void swansong$blockEntityId(BlockEntityRenderDispatcher instance,
                                        BlockEntity blockEntity,
                                        float tickDelta,
                                        int destroyProgress,
                                        Operation<Void> original) {
        if (swansong$active()) {
            var engine = SwanSongOrnithe.ENGINE;
            engine.state()
                  .nextBlockEntity(engine.remapBlockID(Block.getId(blockEntity.getBlock()),
                                                       blockEntity.getBlockMetadata()));
        }
        original.call(instance, blockEntity, tickDelta, destroyProgress);
    }

    @Inject(method = {"renderOutlineShape(Lnet/minecraft/util/math/Box;)V",
                      "renderOutlineShape(Lnet/minecraft/util/math/Box;IIII)V"},
            at = @At("HEAD"),
            cancellable = true,
            require = 2)
    private static void swansong$pushOutline(CallbackInfo ci) {
        if (OrnitheRender.isManaged()) {
            if (OrnitheRender.isShadowPass()) {
                ci.cancel();
                return;
            }
            OrnitheRender.push(Stack.AABBOutline);
        }
    }

    @Inject(method = {"renderOutlineShape(Lnet/minecraft/util/math/Box;)V",
                      "renderOutlineShape(Lnet/minecraft/util/math/Box;IIII)V"},
            at = @At("RETURN"),
            require = 2)
    private static void swansong$popOutline(CallbackInfo ci) {
        OrnitheRender.pop(Stack.AABBOutline);
    }

    @Inject(method = "renderBlockOutline",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/render/world/WorldRenderer;renderOutlineShape(Lnet/minecraft/util/math/Box;)V"))
    private void swansong$blockOutlineState(CallbackInfo ci) {
        if (swansong$active()) {
            OrnitheRender.bindWhiteTexture();
            OrnitheRender.fullBrightLightmap();
        }
    }
}
