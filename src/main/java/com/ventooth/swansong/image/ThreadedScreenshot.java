/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.image;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ScreenShotHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ThreadedScreenshot {
    private static final int STATE_IDLE = 0;
    private static final int STATE_CAPTURING = 1;
    private static final int STATE_POST_CAPTURE = 2;

    private static final AtomicInteger state = new AtomicInteger(STATE_IDLE);

    private static volatile boolean resultSuccess = false;
    private static volatile File resultFile = null;
    private static volatile Exception resultException = null;

    public static void init() {
        FMLCommonHandler.instance()
                        .bus()
                        .register(new ThreadedScreenshot());
    }

    public static IChatComponent captureScreenshot(File gameDirectory, Framebuffer frameBuffer) {
        if (state.get() != STATE_IDLE) {
            return new ChatComponentTranslation("screenshot.swansong.nospam");
        }

        final File imageFile;
        final RawImage rawImage;
        try {
            imageFile = ScreenShotHelper.getTimestampedPNGFileForDirectory(new File(gameDirectory, "screenshots"));
            rawImage = ImageUtils.downloadGLTextureAsBGRA(frameBuffer);
            if (rawImage == null) {
                throw new IllegalStateException("Failed to copy from GPU");
            }
        } catch (Exception e) {
            return new ChatComponentTranslation("screenshot.failure", e.getMessage());
        }

        state.set(STATE_CAPTURING);
        val thread = new Thread(() -> {
            try {
                imageFile.getParentFile()
                         .mkdirs();
                ImageIO.write(rawImage.asBufImg(false, true), "png", imageFile);
                resultSuccess = true;
                resultFile = imageFile;
            } catch (IOException e) {
                resultSuccess = false;
                resultException = e;
            }
            state.set(STATE_POST_CAPTURE);
        });
        thread.setName("Screenshot Thread");
        thread.setDaemon(true);
        thread.start();

        return new ChatComponentTranslation("screenshot.swansong.threaded");
    }

    @SubscribeEvent
    public void postClientTick(TickEvent.ClientTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) {
            return;
        }

        if (state.get() != STATE_POST_CAPTURE) {
            return;
        }

        final IChatComponent chatMessage;
        if (resultSuccess) {
            val file = resultFile;

            val clickableFile = new ChatComponentText(file.getName());
            clickableFile.getChatStyle()
                         .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath()))
                         .setUnderlined(true);
            chatMessage = new ChatComponentTranslation("screenshot.success", clickableFile);
        } else {
            chatMessage = new ChatComponentTranslation("screenshot.failure", resultException.getMessage());
        }
        Minecraft.getMinecraft().ingameGUI.getChatGUI()
                                          .printChatMessage(chatMessage);

        resultSuccess = false;
        resultFile = null;
        resultException = null;

        state.set(STATE_IDLE);
    }
}
