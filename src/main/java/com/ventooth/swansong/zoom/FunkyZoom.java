/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.zoom;

import com.ventooth.swansong.config.ZoomConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunkyZoom {
    private static KeyBinding zoomKey;

    private static boolean isActive;
    private static SmoothCameraState smoothCameraState;

    public static void init() {
        zoomKey = new KeyBinding("key.swansong.zoom", Keyboard.KEY_Z, "key.swansong.categories.general");
        isActive = false;
        smoothCameraState = SmoothCameraState.UNKNOWN;

        ClientRegistry.registerKeyBinding(zoomKey);
        FMLCommonHandler.instance()
                        .bus()
                        .register(new FunkyZoom());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START) {
            val keyState = zoomKey.getIsKeyPressed();
            if (keyState != isActive) {
                isActive = keyState;

                if (isActive) {
                    smoothCameraState = SmoothCameraState.ACTIVE;
                } else {
                    smoothCameraState = SmoothCameraState.RESETTING;
                }

                if (ZoomConfig.Sound) {
                    final ResourceLocation soundLoc;
                    if (isActive) {
                        soundLoc = new ResourceLocation("swansong:zoom.begin");
                    } else {
                        soundLoc = new ResourceLocation("swansong:zoom.end");
                    }
                    val soundHandler = Minecraft.getMinecraft()
                                                .getSoundHandler();
                    val sound = PositionedSoundRecord.func_147674_a(soundLoc, 1F);
                    soundHandler.playSound(sound);
                }
            }
        }
    }

    /**
     * @return {@code true} if {@link #adjustZoom(int)} should be called
     *
     * @implNote In the top level, hijacks the hotbar scrolling when zoom is active.
     */
    public static boolean shouldAdjustZoom() {
        return true;
    }

    /**
     * @param zoomDir {@code zoomDir > 0} zoom in, {@code zoomDir < 0} zoom out
     */
    public static void adjustZoom(int zoomDir) {
        // TODO: Handle zooming in/out?

        //        if (zoomDir > 0) {
        //            System.out.println("Zoom In!");
        //        } else if (zoomDir < 0) {
        //            System.out.println("Zoom Out!");
        //        }
    }

    public static boolean isActive() {
        return isActive;
    }

    // TODO: Custom `MouseFilter` ?
    public static boolean doSmoothCamera() {
        return smoothCameraState == SmoothCameraState.ACTIVE;
    }

    public static boolean shouldResetSmoothCamera() {
        if (smoothCameraState == SmoothCameraState.RESETTING) {
            smoothCameraState = SmoothCameraState.UNKNOWN;
            return true;
        }
        return false;
    }

    public static float tweakFov(float baseFov) {
        return baseFov / 4; //TODO: This is the same modifier as used by OptiFine zoom
    }

    private enum SmoothCameraState {
        UNKNOWN,
        ACTIVE,
        RESETTING
    }
}
