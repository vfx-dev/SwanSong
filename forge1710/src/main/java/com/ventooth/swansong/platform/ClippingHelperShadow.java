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

import com.ventooth.swansong.shader.ShadowFrustum;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.util.AxisAlignedBB;

public class ClippingHelperShadow extends ClippingHelper {
    public final ShadowFrustum frustum = new ShadowFrustum();

    public void addShadowReceiver(WorldRenderer wr) {
        frustum.addShadowReceiver(wr.posX, wr.posY, wr.posZ, wr.posX + 16, wr.posY + 16, wr.posZ + 16);
    }

    public void addShadowReceiver(AxisAlignedBB aabb) {
        frustum.addShadowReceiver((float) aabb.minX,
                                  (float) aabb.minY,
                                  (float) aabb.minZ,
                                  (float) aabb.maxX,
                                  (float) aabb.maxY,
                                  (float) aabb.maxZ);
    }

    @Override
    public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return frustum.testAab((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ);
    }
}
