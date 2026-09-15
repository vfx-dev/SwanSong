/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader.texbuf;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.shader.BufferNameUtil;
import com.ventooth.swansong.shader.CompositeTextureData;
import com.ventooth.swansong.shader.DrawBuffers;
import com.ventooth.swansong.sufrace.FramebufferAttachment;
import com.ventooth.swansong.sufrace.Texture2D;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class ColorBuffers {
    private final EnumSet<CompositeTextureData> clear;
    private final Map<CompositeTextureData, Texture2D> textures;
    private boolean deinited = false;

    public ColorBuffers(@NotNull Map<CompositeTextureData, BufferConfig> configs,
                        @NotNull String namePrefix,
                        int width,
                        int height) {
        val textures = new EnumMap<CompositeTextureData, Texture2D>(CompositeTextureData.class);
        val clear = EnumSet.noneOf(CompositeTextureData.class);
        for (val entry : configs.entrySet()) {
            val id = entry.getKey();
            val config = entry.getValue();
            val name = namePrefix + config.name;
            textures.put(id, Texture2D.ofColorDrawBuffer(name, width, height, config.format));
            if (config.clear) {
                clear.add(id);
            }
            Share.log.trace("Created Color Draw Buffer: {}, format {}",
                            name,
                            BufferNameUtil.gbufferFormatNameFromEnum(config.format));
        }
        this.clear = clear;
        this.textures = Collections.unmodifiableMap(textures);
    }

    public void deinit() {
        if (deinited) {
            return;
        }
        deinited = true;
        textures.values()
                .forEach(Texture2D::deinit);
    }

    public Texture2D get(CompositeTextureData id) {
        return textures.get(id);
    }

    public void resize(int width, int height) {
        for (val texture : textures.values()) {
            texture.resize(width, height);
        }
    }

    public Map<FramebufferAttachment, Texture2D> getFramebufferAttachments(IntList indices) {
        val result = new EnumMap<FramebufferAttachment, Texture2D>(FramebufferAttachment.class);
        val iter = indices.intIterator();
        int i = 0;
        while (iter.hasNext()) {
            val index = iter.nextInt();
            val id = DrawBuffers.textureFromColorTexIndex(index);
            val tex = textures.get(id);
            if (tex == null) {
                throw new IllegalArgumentException("Color buffer with index " + index + " not found!");
            }
            val attachment = FramebufferAttachment.fromColorIndex(i);
            if (attachment == null) {
                throw new IllegalArgumentException("Invalid framebuffer color attachment index " + i);
            }
            result.put(attachment, tex);
            i++;
        }
        return Collections.unmodifiableMap(result);
    }

    public Map<CompositeTextureData, Texture2D> getAllTextures() {
        return textures;
    }

    public void clear(Vector3dc fogColor) {
        if (deinited) {
            throw new IllegalStateException("Cannot clear deinited color textures!");
        }
        for (val tex : textures.entrySet()) {
            val id = tex.getKey();
            val texture = tex.getValue();
            if (clear.contains(id)) {
                switch (id) {
                    case colortex0 -> clearBuffer(texture, (float) fogColor.x(), (float) fogColor.y(), (float) fogColor.z(), 1);
                    case colortex1 -> clearBuffer(texture, 1, 1, 1, 1);
                    default -> clearBuffer(texture, 0, 0, 0, 0);
                }
            }
        }
    }

    private void clearBuffer(Texture2D texture, float r, float g, float b, float a) {
        texture.attachToFramebufferColor(GL30.GL_COLOR_ATTACHMENT0);
        GL11.glClearColor(r, g, b, a);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }
}
