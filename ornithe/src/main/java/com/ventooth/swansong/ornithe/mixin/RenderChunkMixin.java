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
import com.ventooth.swansong.ornithe.OrnitheVertexFormats;
import com.ventooth.swansong.ornithe.SwanSongOrnithe;
import com.ventooth.swansong.shader.ShaderEntityData;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.render.block.BlockRenderDispatcher;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.VertexFormat;
import net.minecraft.client.render.world.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderChunk.class)
public abstract class RenderChunkMixin {
    @ModifyArg(method = "<init>",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/render/vertex/VertexBuffer;<init>(Lnet/minecraft/client/render/vertex/VertexFormat;)V"))
    private VertexFormat swansong$bufferFormat(VertexFormat format) {
        return OrnitheVertexFormats.SHADER_BLOCK;
    }

    @ModifyArg(method = "beginLayer",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/render/vertex/BufferBuilder;begin(ILnet/minecraft/client/render/vertex/VertexFormat;)V"))
    private VertexFormat swansong$builderFormat(VertexFormat format) {
        return OrnitheVertexFormats.SHADER_BLOCK;
    }

    @WrapOperation(method = "compile",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/render/block/BlockRenderDispatcher;render(Lnet/minecraft/block/state/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/WorldView;Lnet/minecraft/client/render/vertex/BufferBuilder;)Z"))
    private boolean swansong$entityData(BlockRenderDispatcher dispatcher,
                                        BlockState state,
                                        BlockPos pos,
                                        WorldView world,
                                        BufferBuilder builder,
                                        Operation<Boolean> original) {
        var block = state.getBlock();
        var meta = block.getMetadataFromState(state);
        var data = ShaderEntityData.get();
        data.pushEntityBlock(SwanSongOrnithe.ENGINE.remapBlockID(Block.getId(block), meta), block.getRenderType(), meta);
        try {
            return original.call(dispatcher, state, pos, world, builder);
        } finally {
            data.popEntity();
        }
    }
}
