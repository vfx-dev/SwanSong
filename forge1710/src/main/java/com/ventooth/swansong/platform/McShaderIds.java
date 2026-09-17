/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.platform;

import com.ventooth.swansong.shader.ShaderEngine;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class McShaderIds {
    public static int blockId(Block block, int meta) {
        return ShaderEngine.remapBlockID(Block.getIdFromBlock(block), meta);
    }

    public static int blockEntityId(TileEntity tileEntity) {
        // TODO: Would we ever need to be NBT-Aware?
        return blockId(tileEntity.getBlockType(), tileEntity.getBlockMetadata());
    }

    public static int entityId(Entity entity) {
        // TODO: Are there any mapping tables for this?
        return EntityList.getEntityID(entity);
    }

    // TODO: remove
    public static int endPortalBlockId() {
        return blockId(Blocks.end_portal, 0);
    }

    public static boolean isItemTranslucent(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        }
        val item = stack.getItem();
        if (!(item instanceof ItemBlock itemBlock)) {
            return false;
        }
        val block = itemBlock.field_150939_a;
        return block != null && block.getRenderBlockPass() != 0;
    }
}
