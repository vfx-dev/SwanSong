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

import lombok.val;

import java.awt.image.BufferedImage;
import java.util.Objects;

public record RawImage(int[] data,
                       int width,
                       int height) {

    public BufferedImage asBufImg(boolean withAlpha, boolean flipY) {
        val img = new BufferedImage(width,
                                    height,
                                    withAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        if (flipY) {
            var offset = width * height;
            for (var y = 0; y < height; y++) {
                offset -= width;
                img.setRGB(0, y, width, 1, data, offset, width);
            }
        } else {
            img.setRGB(0, 0, width, height, data, 0, width);
        }
        return img;
    }
    
}
