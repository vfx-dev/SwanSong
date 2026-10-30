/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */
package com.ventooth.swansong.ornithe;

import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.VertexFormat;
import net.minecraft.client.render.vertex.VertexFormatElement;
import net.minecraft.client.render.vertex.VertexFormatElement.Type;
import net.minecraft.client.render.vertex.VertexFormatElement.Usage;

public final class OrnitheVertexFormats {
    public static final VertexFormat SHADER_BLOCK = new VertexFormat(DefaultVertexFormat.BLOCK).addElement(new VertexFormatElement(0, Type.BYTE, Usage.PADDING, 4))
                                                                                              .addElement(new VertexFormatElement(0, Type.SHORT, Usage.PADDING, 4))
                                                                                              .addElement(new VertexFormatElement(0, Type.FLOAT, Usage.PADDING, 2))
                                                                                              .addElement(new VertexFormatElement(0, Type.BYTE, Usage.PADDING, 4));

    private OrnitheVertexFormats() {
    }
}
