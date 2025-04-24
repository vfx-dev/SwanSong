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

//TODO convert to record
public final class RawImage {
    private final int[] data;
    private final int width;
    private final int height;

    public RawImage(int[] data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

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

    public int[] data() {
        return data;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (RawImage) obj;
        return Objects.equals(this.data, that.data) && this.width == that.width && this.height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, width, height);
    }

    @Override
    public String toString() {
        return "RawImage[" + "data=" + data + ", " + "width=" + width + ", " + "height=" + height + ']';
    }

}
