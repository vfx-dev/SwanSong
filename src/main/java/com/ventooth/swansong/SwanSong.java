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

import com.ventooth.swansong.config.ModuleConfig;
import com.ventooth.swansong.debug.DebugCommandClient;
import com.ventooth.swansong.debug.DebugCommandServer;
import com.ventooth.swansong.image.ThreadedScreenshot;
import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.resources.pack.ModJarContainer;
import com.ventooth.swansong.shader.ShaderEngine;
import com.ventooth.swansong.shader.ShaderTypes;
import com.ventooth.swansong.zoom.FunkyZoom;
import lombok.NoArgsConstructor;
import lombok.val;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;

@Mod(modid = Tags.MOD_ID,
     name = Tags.MOD_NAME,
     version = Tags.MOD_VERSION,
     acceptedMinecraftVersions = "[1.7.10]",
     guiFactory = Tags.ROOT_PKG + ".config.ConfigGuiFactory")
@NoArgsConstructor
public final class SwanSong {
    @SidedProxy(clientSide = Tags.ROOT_PKG + ".SwanSong$ClientProxy",
                serverSide = Tags.ROOT_PKG + ".SwanSong$ServerProxy")
    public static SwanSong.Proxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent evt) {
        evt.registerServerCommand(new DebugCommandServer());
    }

    @NoArgsConstructor
    public static final class ClientProxy implements Proxy {
        @Override
        public void preInit(FMLPreInitializationEvent event) {
            ModJarContainer.init();
            EnvInfo.init();

            ShaderTypes.registerInternalFallbacks();
            MinecraftForge.EVENT_BUS.register(this);
            FMLCommonHandler.instance()
                            .bus()
                            .register(this);

            // TODO: Compat with SmoothFont?
            if (ModuleConfig.FastTextRender && Loader.isModLoaded("smoothfont")) {
                Share.log.warn(
                        "The FastTestRender module is not compatible with SmoothFont (yet), and has been disabled!");
            }
        }

        @Override
        public void init(FMLInitializationEvent event) {
            ClientCommandHandler.instance.registerCommand(new DebugCommandClient());
        }

        @Override
        public void postInit(FMLPostInitializationEvent event) {
            ShaderPackManager.init();

            if (ModuleConfig.ThreadedScreenshots) {
                ThreadedScreenshot.init();
            }
            if (ModuleConfig.FunkyZoom) {
                FunkyZoom.init();
            }
            ShaderTypes.validateRegistry();
            ShaderEngine.firstInit();
        }

        @SubscribeEvent
        public void onDebugGuiText(RenderGameOverlayEvent.Text text) {
            if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) {
                return;
            }
            text.right.add("§b" + Tags.MOD_NAME + " §9" + Tags.MOD_VERSION);
            text.right.add("§bPack §9" + ShaderPackManager.currentShaderPackName);
            text.right.add("§bShadows " + (ShaderEngine.shadowPassExists() ? "§aEnabled" : "§4Disabled"));
            text.right.add("§bShader switches: " + "§r" + ShaderEngine.prevFrameShaderSwitches);
            if (ShaderEngine.DO_GRAPH_LOG) {
                text.right.add("Graph log:");
                for (val node : ShaderEngine.graphLog) {
                    text.right.add(node.name());
                }
            }
        }

        @SubscribeEvent
        public void onDimensionChange(EntityJoinWorldEvent event) {
            if (ShaderEngine.isInitialized()) {
                if (event.world.isRemote && event.entity instanceof EntityPlayerSP) {
                    ShaderEngine.scheduleShaderPackReload();
                }
            }
        }

        @SubscribeEvent
        public void onKey(InputEvent.KeyInputEvent e) {
            if (ShaderEngine.isInitialized()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_R &&
                    Keyboard.isKeyDown(Keyboard.KEY_F3) &&
                    Keyboard.getEventKeyState() &&
                    !Keyboard.isRepeatEvent()) {
                    ShaderEngine.scheduleShaderPackReload();
                }
            }
        }
    }

    @NoArgsConstructor
    public static final class ServerProxy implements Proxy {
        @Override
        public void preInit(FMLPreInitializationEvent event) {
            Share.log.warn("This mod does NOT belong in your DEDICATED SERVER! (but prolly wont crash idk..)");
        }
    }

    public interface Proxy {
        default void preInit(FMLPreInitializationEvent event) {
        }

        default void init(FMLInitializationEvent event) {
        }

        default void postInit(FMLPostInitializationEvent event) {
        }
    }
}