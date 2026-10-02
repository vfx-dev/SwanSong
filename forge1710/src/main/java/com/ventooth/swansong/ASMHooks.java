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

import com.ventooth.swansong.platform.ArchaicShaderEngine;
import com.ventooth.swansong.platform.SkyBoxRenderer;
import com.ventooth.swansong.shader.OldShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;

@SuppressWarnings("unused") // Used from ASM
public class ASMHooks {
    public static void glEnable(int cap) {
        val engine = ArchaicShaderEngine.getNullable();
        if (engine != null) {
            if (cap == GL11.GL_TEXTURE_2D && engine.graph().isSky()) {
                engine.graph().moveTo(StateGraph.Node.RenderSkyTextured);
            }
        }
        GL11.glEnable(cap);
    }

    public static void glDisable(int cap) {
        val engine = ArchaicShaderEngine.getNullable();
        if (engine != null) {
            if (cap == GL11.GL_TEXTURE_2D && engine.graph().isSky()) {
                engine.graph().moveTo(StateGraph.Node.RenderSkyBasic);
            }
        }
        GL11.glDisable(cap);
    }

    public static void glCallList(int list) {
        if (ArchaicShaderEngine.isInitialized()) {
            val rg = Minecraft.getMinecraft().renderGlobal;
            if (list == rg.glSkyList) {
                SkyBoxRenderer.preSkyList();
            }
        }
        GL11.glCallList(list);
    }
}
