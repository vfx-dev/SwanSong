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

import com.ventooth.swansong.shader.ShaderState;
import com.ventooth.swansong.shader.WorldSample;
import lombok.val;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;

public class ArchaicShaderState extends ShaderState {
    @Override
    public double cameraX() {
        val e = Minecraft.getMinecraft().renderViewEntity;
        return e == null ? 0 : e.lastTickPosX + (e.posX - e.lastTickPosX) * getSubTick();
    }

    @Override
    public double cameraY() {
        val e = Minecraft.getMinecraft().renderViewEntity;
        return e == null ? 0 : e.lastTickPosY + (e.posY - e.lastTickPosY) * getSubTick();
    }

    @Override
    public double cameraZ() {
        val e = Minecraft.getMinecraft().renderViewEntity;
        return e == null ? 0 : e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * getSubTick();
    }

    @Override
    public double celestialAngle() {
        val world = Minecraft.getMinecraft().theWorld;
        return world == null ? 0 : world.getCelestialAngle(getSubTick());
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

    @Override
    public int heldItemId() {
        val plr = Minecraft.getMinecraft().thePlayer;
        val stack = plr != null ? plr.getHeldItem() : null;
        val item = stack != null ? stack.getItem() : null;
        return item == null ? -1 : Item.itemRegistry.getIDForObject(item);
    }

    @Override
    public int heldBlockLightValue() {
        val itemId = heldItemId();
        if (itemId == -1) {
            return 0;
        }
        val block = (Block) Block.blockRegistry.getObjectById(itemId);
        return block != null ? block.getLightValue() : 0;
    }

    @Override
    public WorldSample sampleWorld() {
        val mc = Minecraft.getMinecraft();
        val world = mc.theWorld;
        if (world == null) {
            return WorldSample.EMPTY;
        }
        val partialTick = getSubTick();
        val viewEntity = mc.renderViewEntity;
        val playerEntity = mc.thePlayer;

        var eyeInWater = 0;
        if (mc.gameSettings.thirdPersonView == 0 && playerEntity != null && !playerEntity.isPlayerSleeping()) {
            if (viewEntity.isInsideOfMaterial(Material.water)) {
                eyeInWater = 1;
            } else if (viewEntity.isInsideOfMaterial(Material.lava)) {
                eyeInWater = 2;
            }
        }

        var nightVision = 0D;
        var blindnessTicks = 0;
        if (playerEntity != null) {
            if (playerEntity.isPotionActive(Potion.nightVision)) {
                nightVision = mc.entityRenderer.getNightVisionBrightness(playerEntity, partialTick);
            }
            if (playerEntity.isPotionActive(Potion.blindness)) {
                blindnessTicks = playerEntity.getActivePotionEffect(Potion.blindness)
                                             .getDuration();
            }
        }

        val skyColor = world.getSkyColor(viewEntity, partialTick);
        val camPos = camPosInt();

        return new WorldSample(eyeInWater,
                               nightVision,
                               blindnessTicks,
                               viewEntity.getBrightnessForRender(partialTick),
                               world.getRainStrength(partialTick),
                               world.getWorldTime(),
                               world.getTotalWorldTime(),
                               world.getMoonPhase(),
                               skyColor.xCoord,
                               skyColor.yCoord,
                               skyColor.zCoord,
                               world.getBiomeGenForCoords(camPos.x(), camPos.z())
                                    .biomeID);
    }
}
