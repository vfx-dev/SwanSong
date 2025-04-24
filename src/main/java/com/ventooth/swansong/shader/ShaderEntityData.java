/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.minecraft.block.Block;

/**
 * This class needs to be thread-safe, as the data is used within the Tessellator.
 * Which when used with FalseTweaks, is instanced and is threaded!
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderEntityData {
    public static final int SKY = -2;
    public static final int CLOUDS = -3;

    private static final ThreadLocal<ShaderEntityData> instances = ThreadLocal.withInitial(ShaderEntityData::new);

    public static ShaderEntityData get() {
        return instances.get();
    }

    private int[] entityData = new int[32];
    private int entityDataIndex = 0;

    public void reset() {
        entityData = new int[32];
        entityDataIndex = 0;
    }

    public void pushEntity(int data0) {
        pushEntity(data0, 0);
    }

    public void pushEntity(int data0, int data1) {
        entityDataIndex++;
        entityData[entityDataIndex * 2] = (data0 & 0xFFFF) | (data1 << 16);
        entityData[entityDataIndex * 2 + 1] = 0;
    }

    public void pushEntity(Block block) {
        pushEntity(block, 0);
    }

    public void pushEntity(Block block, int meta) {
        entityDataIndex++;
        entityData[entityDataIndex * 2] = (ShaderEngine.getBlockID(block, meta) & 0xFFFF) |
                                          (block.getRenderType() << 16);
        entityData[entityDataIndex * 2 + 1] = meta;
    }

    public int getEntityData() {
        return entityData[entityDataIndex * 2];
    }

    public int getEntityData2() {
        return entityData[entityDataIndex * 2 + 1];
    }

    public void popEntity() {
        entityData[entityDataIndex * 2] = 0;
        entityData[entityDataIndex * 2 + 1] = 0;
        entityDataIndex--;
    }
}
