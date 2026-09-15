/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.gui;

import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

public class TooltipRenderer {
    private static final int delay = 700;
    private int prevX = 0;
    private int prevY = 0;
    private long prevMouseMove = 0;

    public void drawTooltip(GuiScreen screen, int x, int y, ObjectList<String> lines) {
        if (Math.abs(x - prevX) > 5 || Math.abs(y - prevY) > 5) {
            prevX = x;
            prevY = y;
            prevMouseMove = System.currentTimeMillis();
            return;
        }
        if (lines == null || lines.isEmpty()) {
            return;
        }

        if (System.currentTimeMillis() - prevMouseMove < delay) {
            return;
        }

        val fr = Minecraft.getMinecraft().fontRenderer;
        int width = 0;
        int height = 0;
        for (val line : lines) {
            width = Math.max(width, fr.getStringWidth(line));
            height += 11;
        }

        int rectX = x + 2;
        int rectY = y + 2;
        int rectW = width + 10;
        int rectH = height + 10;
        rectX = Math.min(rectX, screen.width - rectW);
        rectY = Math.min(rectY, screen.height - rectH);

        drawRectBorder(rectX, rectY, rectX + rectW, rectY + rectH, Colors.COLOR_RECT_BORDER);
        Gui.drawRect(rectX, rectY, rectX + rectW, rectY + rectH, Colors.COLOR_RECT_FILL);

        int offsetY = 5;
        for (val line : lines) {
            int color = Colors.COLOR_DEFAULT_RGB;
            if (line.endsWith("!")) {
                color = Colors.COLOR_RED_RGB;
            }
            fr.drawStringWithShadow(line, rectX + 5, rectY + offsetY, color);
            offsetY += 11;
        }
    }

    private void drawRectBorder(int x1, int y1, int x2, int y2, int col) {
        Gui.drawRect(x1, y1 - 1, x2, y1, col);
        Gui.drawRect(x1, y2, x2, y2 + 1, col);
        Gui.drawRect(x1 - 1, y1, x1, y2, col);
        Gui.drawRect(x2, y1, x2 + 1, y2, col);
    }
}
