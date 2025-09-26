/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.compat;

import com.falsepattern.lib.util.RenderUtil;
import com.ventooth.swansong.Share;
import com.ventooth.swansong.config.CompatConfig;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.StateGraph;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.OpenGlHelper;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.IEventListener;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Method;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventHijacker {
    public static @Nullable EventHijack hijack(EventHandlerInfo info) {
        if ("net.minecraftforge.client.event.RenderWorldLastEvent".equals(info.eventClassName())) {
            if (CompatConfig.NEI_OverlayFix &&
                "codechicken.nei.ClientHandler".equals(info.targetClassName()) &&
                "renderLastEvent".equals(info.methodName())) {
                Share.log.info("Hijacked NEI overlay for compat");
                return (original, event, info1) -> {
                    if (!ShaderEngine.graph.isManaged()) {
                        original.invoke(event);
                        return;
                    }

                    ShaderEngine.graph.push(StateGraph.Stack.NEIOverlay);

                    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                    try {
                        original.invoke(event);
                    } finally {
                        GL11.glPopAttrib();
                    }

                    ShaderEngine.graph.pop(StateGraph.Stack.NEIOverlay);
                };
            }
        }
        if ("net.minecraftforge.client.event.DrawBlockHighlightEvent".equals(info.eventClassName())) {
            Share.log.info("Hijacked handler: {}#{}(DrawBlockHighlightEvent)",
                           info.targetClassName(),
                           info.methodName());

            return (original, event, info1) -> {
                if (!ShaderEngine.graph.isManaged()) {
                    original.invoke(event);
                    return;
                }

                ShaderEngine.graph.push(StateGraph.Stack.AABBOutline);

                // Needed to ensure no texture or lightmap being present
                RenderUtil.bindEmptyTexture();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

                // Same logic as vanilla block highlight
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                GL11.glColor4f(0F, 0F, 0F, 0.4F);
                GL11.glLineWidth(2F);
                GL11.glDepthMask(false);

                try {
                    original.invoke(event);
                } finally {
                    GL11.glPopAttrib();
                }

                ShaderEngine.graph.pop(StateGraph.Stack.AABBOutline);
            };
        }
        // TODO: Config?
        if ("Reika.DragonAPI.Instantiable.Event.Client.EntityRenderingLoopEvent".equals(info.eventClassName())) {
            Share.log.info("Hijacked handler (DragonAPI): {}#{}(EntityRenderingLoopEvent)",
                           info.targetClassName(),
                           info.methodName());

            // This stuff happens right after entities render on both pass 0 and pass 1
            return (original, event, info1) -> {
                assert ShaderEngine.graph.isManaged() : "Unexpected unmanaged state?";

                ShaderEngine.graph.push(StateGraph.Stack.DragonAPI);

                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                try {
                    original.invoke(event);
                } finally {
                    GL11.glPopAttrib();
                }

                ShaderEngine.graph.pop(StateGraph.Stack.DragonAPI);
            };
        }

        return null;
    }


    @Getter
    @AllArgsConstructor
    @Accessors(fluent = true,
               chain = false)
    //TODO convert to record
    public static final class EventHandlerInfo {
        public final ModContainer owner;
        public final Object target;
        public final Method method;
        public final SubscribeEvent subInfo;

        public String modId() {
            return owner.getModId();
        }

        public String targetClassName() {
            return target.getClass()
                         .getName();
        }

        public String methodName() {
            return method.getName();
        }

        public String eventClassName() {
            return method.getParameterTypes()[0].getName();
        }
    }

    @FunctionalInterface
    public interface EventHijack {
        void invoke(IEventListener original, Event event, EventHandlerInfo info);
    }
}
