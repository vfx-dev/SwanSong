/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.mappings;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.resources.pack.ShaderPack;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockIDRemapper {
    private static final String PROP_FILE_NAME = "block.properties";

    private final @NotNull List<List<MetaMapping>> patterns;

    public int remap(int id, int meta) {
        if (id < 0 || id >= patterns.size()) {
            return id;
        }
        val mappings = patterns.get(id);
        if (mappings == null) {
            return id;
        }
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, mappingsSize = mappings.size(); i < mappingsSize; i++) {
            var mapping = mappings.get(i);
            if (mapping.matches(meta)) {
                return mapping.outputID();
            }
        }
        return id;
    }

    public static @Nullable BlockIDRemapper createRemapper(@NotNull ShaderPack pack) {
        val path = pack.absolutize(null, PROP_FILE_NAME);
        try {
            if (!pack.has(path)) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        val properties = new OrderedProperties();
        try (val input = pack.get(path)) {
            properties.load(input);
        } catch (IOException e) {
            return null;
        }
        ArrayList<List<MetaMapping>> mappings = null;
        for (val prop : properties) {
            val key = prop.getKey();
            val value = prop.getValue();
            final int outputId;
            try {
                final String blockIdStr;
                if (key.startsWith("block.")) {
                    blockIdStr = key.substring("block.".length());
                } else {
                    blockIdStr = key;
                }
                outputId = Integer.parseInt(blockIdStr);
            } catch (NumberFormatException ignored) {
                continue;
            }
            if (mappings == null) {
                mappings = new ArrayList<>();
            }
            parsePatterns(outputId, value, mappings);
        }
        // TODO: Figure out some kind of logging? Append to report probably.
        return mappings == null ? null : new BlockIDRemapper(mappings);
    }

    private static void parsePatterns(int outputId, @NotNull String property, ArrayList<List<MetaMapping>> result) {
        val parts = StringUtils.split(property, null);
        for (val part : parts) {
            parsePattern(outputId, part, result);
        }
    }

    private static void parsePattern(int outputId, @NotNull String pattern, ArrayList<List<MetaMapping>> result) {
        val parts = StringUtils.split(pattern, ':');
        final String modId;
        final String block;
        final int metaStartOffset;
        if (parts.length >= 2 && isBlockName(parts[1])) {
            modId = parts[0];
            block = parts[1];
            metaStartOffset = 2;
        } else {
            modId = "minecraft";
            block = parts[0];
            metaStartOffset = 1;
        }
        if (block.isEmpty()) {
            //TODO log warning
            return;
        }
        val blocks = resolveBlocksIDs(modId, block);
        if (blocks == null) {
            //TODO log warning
            return;
        }

        for (int i = 0, blocksSize = blocks.size(); i < blocksSize; i++) {
            val blockId = blocks.getInt(i);
            final IntSet metas;
            if (parts.length <= metaStartOffset) {
                metas = null;
            } else {
                metas = resolveBlockMetas(parts[metaStartOffset]);
                if (metas == null) {
                    //TODO log warning
                    continue;
                }
            }
            result.ensureCapacity(blockId);
            while (blockId >= result.size()) {
                result.add(null);
            }
            var mappingsList = result.get(blockId);
            if (mappingsList == null) {
                result.set(blockId, mappingsList = new ArrayList<>());
            }
            if (MetaMapping.mergeInto(mappingsList, outputId, metas) == MetaMapping.Status.DuplicateWildcard) {
                Share.log.warn("Duplicate wildcard values for block ID: {}", blockId);
            }
        }
    }

    private static boolean isBlockName(@NotNull String str) {
        return !str.isEmpty() && !Character.isDigit(str.charAt(0)) && !str.contains("=");
    }

    private static @Nullable IntList resolveBlocksIDs(@NotNull String modId, @NotNull String blockName) {
        if (!Character.isDigit(blockName.charAt(0))) {
            val block = GameRegistry.findBlock(modId, blockName);
            if (block == null) {
                return null;
            } else {
                return IntList.of(Block.getIdFromBlock(block));
            }
        }

        return IntParsingUtils.parseUnsignedIntMulti(blockName);
    }

    private static @Nullable IntSet resolveBlockMetas(@NotNull String str) {
        if (str.isEmpty() || !Character.isDigit(str.charAt(0))) {
            //TODO log warning
            return null;
        } else {
            val theList = IntParsingUtils.parseUnsignedIntMulti(str);
            return theList == null ? null : theList.size() < 8 ? new IntArraySet(theList) : new IntOpenHashSet(theList);
        }
    }
}
