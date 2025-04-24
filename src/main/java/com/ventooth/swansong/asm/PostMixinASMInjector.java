/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.asm;

import lombok.SneakyThrows;
import lombok.val;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.List;

public class PostMixinASMInjector implements ITweaker {
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {

    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    @SneakyThrows
    public String[] getLaunchArguments() {
        val f = LaunchClassLoader.class.getDeclaredField("transformers");
        f.setAccessible(true);
        val transformers = (List<IClassTransformer>) f.get(Launch.classLoader);
        transformers.add(CoreLoadingPlugin.transformer);
        return new String[0];
    }
}
