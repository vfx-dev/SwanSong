/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.loader.config;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.shader.config.ConfigEntry;
import com.ventooth.swansong.shader.preprocessor.Option;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.Locale;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

public class ConfigRootScreen extends ConfigScreen implements ConfigEntry.RootScreen {
    private final ObjectList<@NotNull Option> allOptions;
    private final IntList initialValues;

    public ConfigRootScreen(Locale locale,
                            @NotNull ObjectList<@Nullable ConfigEntry> content,
                            @NotNull ObjectList<@NotNull Option> allOptions) {
        super(locale, null, content);
        this.allOptions = allOptions;
        initialValues = new IntArrayList(allOptions.size());
        for (val opt : allOptions) {
            initialValues.add(opt.getValueIndex());
        }
    }

    @Override
    public void cancel() {
        for (int i = 0; i < allOptions.size(); i++) {
            allOptions.get(i)
                      .setValueIndex(initialValues.getInt(i));
        }
    }

    @Override
    public boolean isModified() {
        for (int i = 0; i < allOptions.size(); i++) {
            if (allOptions.get(i)
                          .getValueIndex() != initialValues.getInt(i)) {
                return true;
            }
        }
        return false;
    }

    boolean isModified(int index) {
        return allOptions.get(index)
                         .getValueIndex() != initialValues.getInt(index);
    }

    @Override
    public void save() {
        val configFile = ShaderPackManager.resolvePath(ShaderPackManager.currentShaderPackName + ".txt");
        PrintWriter writer = null;
        try {
            for (int i = 0; i < allOptions.size(); i++) {
                var entry = allOptions.get(i);
                initialValues.set(i, entry.getValueIndex());
                if (!entry.isDefaultValue()) {
                    if (writer == null) {
                        writer = new PrintWriter(Files.newBufferedWriter(configFile));
                    }
                    writer.println(entry.toProps());
                }
            }
        } catch (IOException e) {
            Share.log.error("Failed to save shader configuration", e);
        } finally {
            if (writer != null) {
                writer.close();
            } else {
                try {
                    Files.deleteIfExists(configFile);
                } catch (Throwable t) {
                    Share.log.error("Failed to remove empty shader configuration file", t);
                }
            }
        }
    }

    @Override
    public void fullReset() {
        reset();
        for (val entry : allOptions) {
            entry.setToDefault();
        }
    }
}
