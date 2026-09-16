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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class McWorldSampler {
    public static WorldSample sample() {
        val mc = Minecraft.getMinecraft();
        val world = mc.theWorld;
        if (world == null) {
            return WorldSample.EMPTY;
        }
        val partialTick = ShaderState.getSubTick();
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
        val camPos = ShaderState.camPosInt();

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

    public static int heldItemId() {
        val plr = Minecraft.getMinecraft().thePlayer;
        val stack = plr != null ? plr.getHeldItem() : null;
        val item = stack != null ? stack.getItem() : null;
        return item == null ? -1 : Item.itemRegistry.getIDForObject(item);
    }

    public static int heldBlockLightValue() {
        val itemId = heldItemId();
        if (itemId == -1) {
            return 0;
        }
        val block = (Block) Block.blockRegistry.getObjectById(itemId);
        return block != null ? block.getLightValue() : 0;
    }
}
