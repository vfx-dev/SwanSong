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

import com.ventooth.swansong.shader.ShaderEntityData;
import com.ventooth.swansong.shader.ShaderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

@Mixin(value = RenderBlocks.class,
       priority = 1010)
public abstract class MixinRenderBlocks {
    @Shadow
    public IBlockAccess blockAccess;

    @Unique
    private final ShaderEntityData swansong$entityData = ShaderEntityData.get();

    @Inject(method = "renderBlockByRenderType",
            at = @At("HEAD"),
            require = 1)
    private void state_pushEntityBlock(Block block, int posX, int posY, int posZ, CallbackInfoReturnable<Boolean> cir) {
        swansong$entityData.pushEntity(block, blockAccess.getBlockMetadata(posX, posY, posZ));
    }

    @Inject(method = "renderBlockByRenderType",
            at = @At("RETURN"),
            require = 1)
    private void state_popEntityBlock(CallbackInfoReturnable<Boolean> cir) {
        swansong$entityData.popEntity();
    }

    @Inject(method = "renderBlockFlowerpot",
            at = @At("HEAD"),
            require = 1)
    private void state_pushEntityBlockFlowerpot0(BlockFlowerPot block,
                                                 int posX,
                                                 int posY,
                                                 int posZ,
                                                 CallbackInfoReturnable<Boolean> cir) {
        swansong$entityData.pushEntity(block, blockAccess.getBlockMetadata(posX, posY, posZ));
    }

    @Inject(method = "renderBlockFlowerpot",
            at = @At("RETURN"),
            require = 1)
    private void state_popEntityBlockFlowerpot0(CallbackInfoReturnable<Boolean> cir) {
        swansong$entityData.popEntity();
    }

    @Redirect(method = "renderBlockFlowerpot",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/block/Block;getRenderType()I"),
              require = 1)
    private int state_pushEntityBlockFlowerpot1(Block block) {
        swansong$entityData.pushEntity(block);
        return block.getRenderType();
    }

    @Inject(method = "renderBlockFlowerpot",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/Tessellator;addTranslation(FFF)V",
                     shift = At.Shift.AFTER,
                     ordinal = 1),
            require = 1)
    private void state_popEntityBlockFlowerpot1(CallbackInfoReturnable<Boolean> cir) {
        swansong$entityData.popEntity();
    }

    // region Block Light Level

    @ModifyConstant(method = {"renderBlockBed(Lnet/minecraft/block/Block;III)Z",
                              "renderBlockDoor(Lnet/minecraft/block/Block;III)Z",
                              "renderBlockLiquid(Lnet/minecraft/block/Block;III)Z",
                              "renderBlockCactusImpl(Lnet/minecraft/block/Block;IIIFFF)Z",
                              "renderStandardBlockWithColorMultiplier(Lnet/minecraft/block/Block;IIIFFF)Z",
                              "renderBlockSandFalling(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIII)V"},
                    constant = @Constant(floatValue = 0.5F),
                    require = 6)
    public float state_blockSingleLightLevel05(float constant) {
        return ShaderState.blockLightLevel(constant);
    }

    @ModifyConstant(method = {"renderBlockBed(Lnet/minecraft/block/Block;III)Z",
                              "renderBlockLiquid(Lnet/minecraft/block/Block;III)Z",
                              "renderBlockDoor(Lnet/minecraft/block/Block;III)Z",
                              "renderBlockCactusImpl(Lnet/minecraft/block/Block;IIIFFF)Z",
                              "renderStandardBlockWithColorMultiplier(Lnet/minecraft/block/Block;IIIFFF)Z",
                              "renderBlockSandFalling(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIII)V"},
                    constant = @Constant(floatValue = 0.6F),
                    expect = 6)
    public float state_blockSingleLightLevel06(float constant) {
        return ShaderState.blockLightLevel(constant);
    }

    @ModifyConstant(method = {"renderBlockBed(Lnet/minecraft/block/Block;III)Z",
                              "renderBlockLiquid(Lnet/minecraft/block/Block;III)Z",
                              "renderBlockDoor(Lnet/minecraft/block/Block;III)Z",
                              "renderBlockCactusImpl(Lnet/minecraft/block/Block;IIIFFF)Z",
                              "renderStandardBlockWithColorMultiplier(Lnet/minecraft/block/Block;IIIFFF)Z",
                              "renderBlockSandFalling(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIII)V"},
                    constant = @Constant(floatValue = 0.8F),
                    expect = 6)
    public float state_blockSingleLightLevel08(float constant) {
        return ShaderState.blockLightLevel(constant);
    }

    @ModifyConstant(method = "renderPistonExtension(Lnet/minecraft/block/Block;IIIZ)Z",
                    constant = @Constant(floatValue = 0.5F),
                    slice = @Slice(from = @At(value = "FIELD",
                                              target = "Lnet/minecraft/client/renderer/RenderBlocks;uvRotateEast:I")),
                    expect = 4)
    public float state_pistonBlockLightLevel05(float constant) {
        return ShaderState.blockLightLevel(constant);
    }

    @ModifyConstant(method = "renderPistonExtension(Lnet/minecraft/block/Block;IIIZ)Z",
                    constant = @Constant(floatValue = 0.6F),
                    expect = 12)
    public float state_pistonBlockLightLevel06(float constant) {
        return ShaderState.blockLightLevel(constant);
    }

    @ModifyConstant(method = "renderPistonExtension(Lnet/minecraft/block/Block;IIIZ)Z",
                    constant = @Constant(floatValue = 0.8F),
                    expect = 4)
    public float state_pistonBlockLightLevel08(float constant) {
        return ShaderState.blockLightLevel(constant);
    }

    @ModifyConstant(method = {"renderStandardBlockWithAmbientOcclusionPartial(Lnet/minecraft/block/Block;IIIFFF)Z",
                              "renderStandardBlockWithAmbientOcclusion(Lnet/minecraft/block/Block;IIIFFF)Z"},
                    constant = @Constant(floatValue = 0.5F),
                    expect = 12)
    public float state_multipleBlockLightLevel05(float constant) {
        return ShaderState.blockLightLevel(constant);
    }

    @ModifyConstant(method = {"renderStandardBlockWithAmbientOcclusionPartial(Lnet/minecraft/block/Block;IIIFFF)Z",
                              "renderStandardBlockWithAmbientOcclusion(Lnet/minecraft/block/Block;IIIFFF)Z"},
                    constant = @Constant(floatValue = 0.6F),
                    expect = 24)
    public float state_multipleBlockLightLevel06(float constant) {
        return ShaderState.blockLightLevel(constant);
    }

    @ModifyConstant(method = {"renderStandardBlockWithAmbientOcclusionPartial(Lnet/minecraft/block/Block;IIIFFF)Z",
                              "renderStandardBlockWithAmbientOcclusion(Lnet/minecraft/block/Block;IIIFFF)Z"},
                    constant = @Constant(floatValue = 0.8F),
                    expect = 24)
    public float state_multipleBlockLightLevel08(float constant) {
        return ShaderState.blockLightLevel(constant);
    }
    // endregion
}
