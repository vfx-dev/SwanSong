/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.debug;

import lombok.val;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class DebugCommandServer extends CommandBase {
    private final String name;
    private final List<String> aliases;
    private final String usage;
    private final int permission;

    private final Map<String, Consumer<ICommandSender>> optionMap;
    private final List<String> optionList;

    public DebugCommandServer() {
        this.name = "swan2";
        this.aliases = Collections.singletonList(this.name);
        this.usage = MessageFormat.format("/{0} <option>", this.name);
        this.permission = 0;

        this.optionMap = new LinkedHashMap<>();
        this.optionMap.put("smite", sender -> {
            if (!(sender instanceof EntityPlayerMP player)) {
                return;
            }
            // Got Player
            val hit = player.rayTrace(100F, 0F);
            if (hit == null || hit.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
                return;
            }
            // Got Hit

            val world = player.worldObj;

            val posX = hit.blockX;
            val posY = hit.blockY;
            val posZ = hit.blockZ;

            val bolt = new EntityLightningBolt(world, posX, posY, posZ);

            world.addWeatherEffect(bolt);

            sender.addChatMessage(new ChatComponentText("ZAP!"));
        });

        this.optionList = new ArrayList<>(optionMap.keySet());
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args == null || args.length != 1) {
            return;
        }
        val arg = args[0];

        for (val option : optionMap.entrySet()) {
            if (arg.equals(option.getKey())) {
                option.getValue()
                      .accept(sender);
                return;
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args == null || args.length != 1) {
            return null;
        }
        val arg = args[0];
        val completionOptions = new ArrayList<String>();
        optionList.stream()
                  .filter(opt -> opt.startsWith(arg))
                  .forEach(completionOptions::add);
        return completionOptions.isEmpty() ? null : completionOptions;
    }

    // region Not Important
    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return usage;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return permission;
    }

    @Override
    public List<String> getCommandAliases() {
        return aliases;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
    // endregion
}
