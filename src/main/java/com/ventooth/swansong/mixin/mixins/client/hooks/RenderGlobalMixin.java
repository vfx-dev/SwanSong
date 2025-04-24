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
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderState;
import com.ventooth.swansong.shader.StateGraph;
import com.ventooth.swansong.shader.WorldProviderRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IRenderHandler;

import java.util.Collections;
import java.util.List;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {
    /**
     * @author FalsePattern
     * @reason Nobody used this, so we don't handle it. The overwrite makes it break if someone actually uses it (to make it diagnosable)
     */
    @Overwrite
    public void rebuildDisplayListEntities() {

    }

    @Redirect(method = "renderEntities",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderGlobal;displayListEntitiesDirty:Z"),
              require = 1)
    private boolean dontBakeDisplayListEntities(RenderGlobal instance) {
        return false;
    }

    @Redirect(method = "renderEntities",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V"),
              require = 1)
    private void dontCallDisplayListEntities(int list) {

    }

    @Inject(method = "drawOutlinedBoundingBox",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void hook_BeginAABBOutline(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            if (ShaderEngine.graph.isShadowPass()) {
                // We don't allow any rendering of bounding boxes in the shadow pass.
                ci.cancel();
                return;
            }

            // Note, we're NOT globally resetting the light map or texture
            // Because some mod might actually want to use it when rendering a bounding box
            // Doubtful, but possible none the less.
            ShaderEngine.graph.push(StateGraph.Stack.AABBOutline);
        }
    }

    @Inject(method = "drawOutlinedBoundingBox",
            at = @At(value = "RETURN"),
            require = 1)
    private static void hook_EndAABBOutline(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.pop(StateGraph.Stack.AABBOutline);
        }
    }

    @Inject(method = "drawSelectionBox",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderGlobal;drawOutlinedBoundingBox(Lnet/minecraft/util/AxisAlignedBB;I)V"),
            require = 1)
    private static void fix_TexLightSelectionBox(CallbackInfo ci) {
        // Needed to ensure no texture or lightmap being present
        RenderUtil.bindEmptyTexture();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
    }

    @WrapOperation(method = "renderSky(F)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/WorldProvider;getSkyRenderer()Lnet/minecraftforge/client/IRenderHandler;",
                            remap = false),
                   require = 1)
    private IRenderHandler hook_ShaderRenderSky(WorldProvider worldProvider, Operation<IRenderHandler> original) {
        return WorldProviderRenderer.wrapSkyRenderer(worldProvider, original.call(worldProvider));
    }

    @Inject(method = "drawBlockDamageTexture(Lnet/minecraft/client/renderer/Tessellator;Lnet/minecraft/entity/EntityLivingBase;F)V",
            at = @At("HEAD"),
            remap = false,
            require = 1)
    private void hook_BeginBlockDestroyProgress(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.push(StateGraph.Stack.BlockDestroyProgress);
        }
    }

    @Inject(method = "drawBlockDamageTexture(Lnet/minecraft/client/renderer/Tessellator;Lnet/minecraft/entity/EntityLivingBase;F)V",
            at = @At("RETURN"),
            remap = false,
            require = 1)
    private void hook_EndBlockDestroyProgress(CallbackInfo ci) {
        if (ShaderEngine.graph.isManaged()) {
            ShaderEngine.graph.pop(StateGraph.Stack.BlockDestroyProgress);
        }
    }

    @WrapOperation(method = "renderEntities",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraftforge/client/MinecraftForgeClient;getRenderPass()I"),
                   require = 1)
    private int captureEntityRenderPass(Operation<Integer> original, @Share("render_pass") LocalIntRef renderPass) {
        renderPass.set(original.call());
        return renderPass.get();
    }

    @Inject(method = "renderEntities",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                     args = "ldc=global"),
            require = 1)
    private void beginWeatherEntities(CallbackInfo ci, @Share("render_pass") LocalIntRef renderPass) {
        // Reset Render Pass
        ForgeHooksClient.setRenderPass(renderPass.get());

        if (ShaderEngine.graph.isShadowPass()) {
            // We don't draw em
        } else {
            ShaderEngine.graph.moveToEither(StateGraph.Node.RenderWeatherEntities0, StateGraph.Node.RenderWeatherEntities1);
        }
    }

    @Redirect(method = "renderEntities",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/multiplayer/WorldClient;weatherEffects:Ljava/util/List;"),
              require = 1)
    private List<?> noWeatherEffectsInShadowPass(WorldClient instance) {
        if (ShaderEngine.graph.isShadowPass()) {
            return Collections.emptyList();
        } else {
            return instance.weatherEffects;
        }
    }

    @Inject(method = "renderEntities(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                     args = "ldc=entities",
                     shift = At.Shift.AFTER),
            require = 1)
    private void hook_BeginEntities(CallbackInfo ci, @Share("render_pass") LocalIntRef renderPass) {
        // Reset Render Pass
        ForgeHooksClient.setRenderPass(renderPass.get());

        if (ShaderEngine.graph.isShadowPass()) {
            ShaderEngine.graph.moveToEither(StateGraph.Node.ShadowEntities0, StateGraph.Node.ShadowEntities1);
        } else {
            ShaderEngine.graph.moveToEither(StateGraph.Node.RenderEntities0, StateGraph.Node.RenderEntities1);
        }
    }

    @WrapWithCondition(method = "renderEntities(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntitySimple(Lnet/minecraft/entity/Entity;F)Z"),
                       require = 2)
    private boolean hook_NextEntity(RenderManager instance,
                                    Entity entity,
                                    float subTick,
                                    @Share("render_pass") LocalIntRef renderPass) {
        // Reset Render Pass
        ForgeHooksClient.setRenderPass(renderPass.get());

        ShaderState.nextEntity(entity);
        return true;
    }

    @Inject(method = "renderEntities(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderHelper;enableStandardItemLighting()V"),
            require = 1)
    private void hook_BeginBlockEntities(CallbackInfo ci, @Share("render_pass") LocalIntRef renderPass) {
        // Reset Render Pass
        ForgeHooksClient.setRenderPass(renderPass.get());

        if (ShaderEngine.graph.isShadowPass()) {
            ShaderEngine.graph.moveToEither(StateGraph.Node.ShadowBlockEntities0, StateGraph.Node.ShadowBlockEntities1);
        } else {
            ShaderEngine.graph.moveToEither(StateGraph.Node.RenderBlockEntities0, StateGraph.Node.RenderBlockEntities1);
        }
    }

    @WrapWithCondition(method = "renderEntities(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;renderTileEntity(Lnet/minecraft/tileentity/TileEntity;F)V"),
                       require = 1)
    private boolean hook_NextBlockEntity(TileEntityRendererDispatcher instance, TileEntity tileEntity, float subTick) {
        ShaderState.nextBlockEntity(tileEntity);
        RenderUtil.bindEmptyTexture();
        return true;
    }
}
