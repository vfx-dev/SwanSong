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

import com.ventooth.swansong.shader.HostWorld;
import com.ventooth.swansong.shader.ShaderState;
import lombok.val;

import net.minecraft.client.Minecraft;

public final class McHostWorld implements HostWorld {
    @Override
    public double cameraX() {
        val e = Minecraft.getMinecraft().renderViewEntity;
        val t = ShaderState.getSubTick();
        return e == null ? 0 : e.lastTickPosX + (e.posX - e.lastTickPosX) * t;
    }

    @Override
    public double cameraY() {
        val e = Minecraft.getMinecraft().renderViewEntity;
        val t = ShaderState.getSubTick();
        return e == null ? 0 : e.lastTickPosY + (e.posY - e.lastTickPosY) * t;
    }

    @Override
    public double cameraZ() {
        val e = Minecraft.getMinecraft().renderViewEntity;
        val t = ShaderState.getSubTick();
        return e == null ? 0 : e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * t;
    }

    @Override
    public double celestialAngle() {
        val world = Minecraft.getMinecraft().theWorld;
        return world == null ? 0 : world.getCelestialAngle(ShaderState.getSubTick());
    }

    @Override
    public double farPlane() {
        return Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16;
    }

    @Override
    public boolean isGuiHidden() {
        return Minecraft.getMinecraft().gameSettings.hideGUI;
    }

    @Override
    public double screenBrightness() {
        return Minecraft.getMinecraft().gameSettings.gammaSetting;
    }

    @Override
    public int displayWidth() {
        return Minecraft.getMinecraft().displayWidth;
    }

    @Override
    public int displayHeight() {
        return Minecraft.getMinecraft().displayHeight;
    }
}
