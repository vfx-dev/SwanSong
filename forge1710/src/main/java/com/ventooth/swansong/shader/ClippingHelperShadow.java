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

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.util.AxisAlignedBB;

public class ClippingHelperShadow extends ClippingHelper {

    private float minCamX, minCamY, minCamZ;
    private float maxCamX, maxCamY, maxCamZ;
    private float minX, minY, minZ;
    private float maxX, maxY, maxZ;
    private boolean hasReceiver = false;
    private final Vector3f scratch = new Vector3f();
    public final Matrix4f shadowModelViewMatrix = new Matrix4f();

    public void begin() {
        minCamX = Float.POSITIVE_INFINITY;
        minCamY = Float.POSITIVE_INFINITY;
        minCamZ = Float.POSITIVE_INFINITY;
        maxCamX = Float.NEGATIVE_INFINITY;
        maxCamY = Float.NEGATIVE_INFINITY;
        maxCamZ = Float.NEGATIVE_INFINITY;
        minX = Float.POSITIVE_INFINITY;
        minY = Float.POSITIVE_INFINITY;
        minZ = Float.POSITIVE_INFINITY;
        maxX = Float.NEGATIVE_INFINITY;
        maxY = Float.NEGATIVE_INFINITY;
        maxZ = Float.NEGATIVE_INFINITY;
        hasReceiver = false;
    }

    public void addShadowReceiver(WorldRenderer wr) {
        addShadowReceiver(wr.posX, wr.posY, wr.posZ, wr.posX + 16, wr.posY + 16, wr.posZ + 16);
    }

    public void addShadowReceiver(AxisAlignedBB aabb) {
        addShadowReceiver((float) aabb.minX,
                          (float) aabb.minY,
                          (float) aabb.minZ,
                          (float) aabb.maxX,
                          (float) aabb.maxY,
                          (float) aabb.maxZ);
    }

    public void addShadowReceiver(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        hasReceiver = true;
        this.minX = Math.min(this.minX, minX);
        this.minY = Math.min(this.minY, minY);
        this.minZ = Math.min(this.minZ, minZ);
        this.maxX = Math.max(this.maxX, maxX);
        this.maxY = Math.max(this.maxY, maxY);
        this.maxZ = Math.max(this.maxZ, maxZ);
    }

    public void end() {
        minCamX = Float.POSITIVE_INFINITY;
        minCamY = Float.POSITIVE_INFINITY;
        minCamZ = Float.POSITIVE_INFINITY;
        maxCamX = Float.NEGATIVE_INFINITY;
        maxCamY = Float.NEGATIVE_INFINITY;
        maxCamZ = Float.NEGATIVE_INFINITY;
        if (!hasReceiver) {
            return;
        }
        for (int i = 0; i < 8; i++) {
            float x = (i & 1) == 0 ? minX : maxX;
            float y = (i & 2) == 0 ? minY : maxY;
            float z = (i & 4) == 0 ? minZ : maxZ;
            shadowModelViewMatrix.transformPosition(x, y, z, scratch);
            x = scratch.x;
            y = scratch.y;
            z = scratch.z;
            minCamX = Math.min(minCamX, x);
            minCamY = Math.min(minCamY, y);
            minCamZ = Math.min(minCamZ, z);
            maxCamX = Math.max(maxCamX, x);
            maxCamY = Math.max(maxCamY, y);
            maxCamZ = Math.max(maxCamZ, z);
        }
        if (Float.isNaN(minCamX)) {
            minCamX = Float.NEGATIVE_INFINITY;
        }
        if (Float.isNaN(minCamY)) {
            minCamY = Float.NEGATIVE_INFINITY;
        }
        if (Float.isNaN(minCamZ)) {
            minCamZ = Float.NEGATIVE_INFINITY;
        }
        if (Float.isNaN(maxCamX)) {
            maxCamX = Float.POSITIVE_INFINITY;
        }
        if (Float.isNaN(maxCamY)) {
            maxCamY = Float.POSITIVE_INFINITY;
        }
        if (Float.isNaN(maxCamZ)) {
            maxCamZ = Float.POSITIVE_INFINITY;
        }
    }

    @Override
    public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (!hasReceiver) {
            return false;
        }
        return isShadowVisible((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ);
    }


    private boolean isShadowVisible(float mX, float mY, float mZ, float MX, float MY, float MZ) {
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 8; i++) {
            float x = ((i & 1) == 0 ? mX : MX);
            float y = ((i & 2) == 0 ? mY : MY);
            float z = ((i & 4) == 0 ? mZ : MZ);
            shadowModelViewMatrix.transformPosition(x, y, z, scratch);
            x = scratch.x;
            y = scratch.y;
            z = scratch.z;
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        return maxX > minCamX && minX < maxCamX && maxY > minCamY && minY < maxCamY && maxZ > minCamZ && minZ < maxCamZ;
    }
}
