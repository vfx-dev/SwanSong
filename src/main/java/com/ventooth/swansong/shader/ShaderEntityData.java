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

import java.util.Arrays;

/**
 * This class needs to be thread-safe, as the data is used within the Tessellator.
 * Which when used with FalseTweaks, is instanced and is threaded!
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderEntityData {
    public static final short SKY = -2;
    public static final short CLOUDS = -3;

    private static final ThreadLocal<ShaderEntityData> instances = ThreadLocal.withInitial(ShaderEntityData::new);

    public static ShaderEntityData get() {
        return instances.get();
    }

    private static final int CAPACITY = 16;
    private static final int DECREMENT_DELTA = CAPACITY - 1;

    private final long[] storage = new long[CAPACITY];
    private int entityDataIndex = 0;

    private static long pack(int entityData, int entityData2) {
        return (entityData & 0xFFFFFFFFL) | ((entityData2 & 0xFFFFFFFFL) << 32);
    }

    public static int unpackEntityData(long packed) {
        return (int)(packed & 0xFFFFFFFFL);
    }

    public static int unpackEntityData2(long packed) {
        return (int)((packed >>> 32));
    }

    private void pushRaw(int entityData, int entityData2) {
        int idx = (entityDataIndex + 1) % CAPACITY;
//        if (idx == 0) {
//            new Throwable("Index wrapped!").printStackTrace();
//        }
        entityDataIndex = idx;
        storage[idx] = pack(entityData, entityData2);
        storage[(idx + 1) % CAPACITY] = 0;
    }

    public void reset() {
        Arrays.fill(storage, 0L);
        entityDataIndex = 0;
    }

    @Deprecated
    public void pushEntity(int data0) {
        pushEntity((short)data0);
    }

    public void pushEntity(short data0) {
        pushEntity(data0, (short)0);
    }

    @Deprecated
    public void pushEntity(int data0, int data1) {
        pushEntity((short)data0, (short)data1);
    }

    public void pushEntity(short data0, short data1) {
        pushRaw((data0 & 0xFFFF) | ((data1 & 0xFFFF) << 16), 0);
    }

    public void pushEntity(Block block) {
        pushEntity(block, 0);
    }

    public void pushEntity(Block block, int meta) {
        pushRaw((ShaderEngine.getBlockID(block, meta) & 0xFFFF) | ((block.getRenderType() & 0xFFFF) << 16), meta);
    }

    public long getPackedEntityData() {
        return storage[entityDataIndex];
    }

    public void popEntity() {
        int idx = entityDataIndex;
        storage[idx] = 0;
        entityDataIndex = (idx + DECREMENT_DELTA) % CAPACITY;
    }
}
