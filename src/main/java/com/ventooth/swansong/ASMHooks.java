/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong;

import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;

@SuppressWarnings("unused") // Used from ASM
public class ASMHooks {
    public static void glEnable(int cap) {
        if (cap == GL11.GL_TEXTURE_2D && ShaderEngine.graph.isSky()) {
            ShaderEngine.graph.moveTo(StateGraph.Node.RenderSkyTextured);
        }
        GL11.glEnable(cap);
    }

    public static void glDisable(int cap) {
        if (cap == GL11.GL_TEXTURE_2D && ShaderEngine.graph.isSky()) {
            ShaderEngine.graph.moveTo(StateGraph.Node.RenderSkyBasic);
        }
        GL11.glDisable(cap);
    }

    public static void glCallList(int list) {
        if (ShaderEngine.isInitialized()) {
            val rg = Minecraft.getMinecraft().renderGlobal;
            if (list == rg.glSkyList) {
                ShaderEngine.preSkyList();
            }
        }
        GL11.glCallList(list);
    }
}
