/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader;

import com.ventooth.swansong.gl.GLTexture;
import com.ventooth.swansong.shader.shaderobjects.GBufferShader;
import com.ventooth.swansong.shader.texbuf.BufferConfig;
import com.ventooth.swansong.shader.texbuf.ColorBuffers;
import com.ventooth.swansong.sufrace.Framebuffer;
import com.ventooth.swansong.sufrace.Texture2D;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Map;

public class DrawBuffers {
    // @formatter:off
    // @formatter:on
    public final @NotNull ColorBuffers gColor;
    public final @NotNull Texture2D gDepthTex;
    public final @NotNull Texture2D depthTex0; // includes all geometry
    public final @NotNull Texture2D depthTex1; // excludes translucent geometry
    public final @NotNull Texture2D depthTex2; // excludes translucent and hand geometry

    public final Texture2D shadowDepthTex0;
    public final Texture2D shadowDepthTex1;
    public final Texture2D shadowColorTex0;
    public final Texture2D shadowColorTex1;

    public final Framebuffer shadow;

    public final @NotNull Framebuffer tempColor;
    public final @NotNull Framebuffer tempDepth;

    public final @NotNull Framebuffer _final;

    private boolean deinited = false;

    @lombok.Builder(builderClassName = "Builder")
    public DrawBuffers(@NonNull Map<CompositeTextureData, BufferConfig> colorDrawBufferConfigs,
                       @Nullable ShadowProperties shadow,
                       int width,
                       int height) {
        gColor = new ColorBuffers(colorDrawBufferConfigs, "G_", width, height);
        gDepthTex = Texture2D.ofDepthDrawBuffer("G_depthtex", width, height);
        depthTex0 = Texture2D.ofDepthDrawBuffer("depthtex0", width, height);
        depthTex1 = Texture2D.ofDepthDrawBuffer("depthtex1", width, height);
        depthTex2 = Texture2D.ofDepthDrawBuffer("depthtex2", width, height);
        tempColor = Framebuffer.create("Temp_Color", false);
        tempDepth = Framebuffer.create("Temp_Depth", true);
        _final = Framebuffer.create("Final", gColor.get(CompositeTextureData.colortex0));
        if (shadow != null) {
            shadowDepthTex0 = Texture2D.ofShadowDepthBuffer("shadow0",
                                                            shadow.resolution,
                                                            shadow.resolution,
                                                            shadow.depthFilterNearest(0),
                                                            shadow.depthMipmapEnabled(0),
                                                            shadow.depthHWFilterEnabled(0));
            shadowDepthTex1 = Texture2D.ofShadowDepthBuffer("shadow1",
                                                            shadow.resolution,
                                                            shadow.resolution,
                                                            shadow.depthFilterNearest(1),
                                                            shadow.depthMipmapEnabled(1),
                                                            shadow.depthHWFilterEnabled(1));
            shadowColorTex0 = Texture2D.ofShadowColorBuffer("shadowcolor0",
                                                            shadow.resolution,
                                                            shadow.resolution,
                                                            shadow.colorFilterNearest(0),
                                                            shadow.colorMipmapEnabled(0));
            shadowColorTex1 = Texture2D.ofShadowColorBuffer("shadowcolor1",
                                                            shadow.resolution,
                                                            shadow.resolution,
                                                            shadow.colorFilterNearest(1),
                                                            shadow.colorMipmapEnabled(1));

            this.shadow = Framebuffer.create("shadow_fb", shadowColorTex0, shadowColorTex1, shadowDepthTex0);
        } else {
            shadowDepthTex0 = null;
            shadowDepthTex1 = null;
            shadowColorTex0 = null;
            shadowColorTex1 = null;
            this.shadow = null;
        }
    }

    public static @Nullable CompositeTextureData textureFromColorTexIndex(int index) {
        return switch (index) {
            case 0 -> CompositeTextureData.colortex0;
            case 1 -> CompositeTextureData.colortex1;
            case 2 -> CompositeTextureData.colortex2;
            case 3 -> CompositeTextureData.colortex3;
            case 4 -> CompositeTextureData.colortex4;
            case 5 -> CompositeTextureData.colortex5;
            case 6 -> CompositeTextureData.colortex6;
            case 7 -> CompositeTextureData.colortex7;
            case 8 -> CompositeTextureData.colortex8;
            case 9 -> CompositeTextureData.colortex9;
            case 10 -> CompositeTextureData.colortex10;
            case 11 -> CompositeTextureData.colortex11;
            case 12 -> CompositeTextureData.colortex12;
            case 13 -> CompositeTextureData.colortex13;
            case 14 -> CompositeTextureData.colortex14;
            case 15 -> CompositeTextureData.colortex15;
            default -> null;
        };
    }

    public static int colorTexIndexFromTexture(@NotNull CompositeTextureData id) {
        return switch (id) {
            case colortex0 -> 0;
            case colortex1 -> 1;
            case colortex2 -> 2;
            case colortex3 -> 3;
            case colortex4 -> 4;
            case colortex5 -> 5;
            case colortex6 -> 6;
            case colortex7 -> 7;
            case colortex8 -> 8;
            case colortex9 -> 9;
            case colortex10 -> 10;
            case colortex11 -> 11;
            case colortex12 -> 12;
            case colortex13 -> 13;
            case colortex14 -> 14;
            case colortex15 -> 15;
            default -> -1;
        };
    }

    public @Nullable Texture2D getByID(@NotNull CompositeTextureData id) {
        return switch (id) {
            case colortex0 -> gColor.get(CompositeTextureData.colortex0);
            case colortex1 -> gColor.get(CompositeTextureData.colortex1);
            case colortex2 -> gColor.get(CompositeTextureData.colortex2);
            case colortex3 -> gColor.get(CompositeTextureData.colortex3);
            case colortex4 -> gColor.get(CompositeTextureData.colortex4);
            case colortex5 -> gColor.get(CompositeTextureData.colortex5);
            case colortex6 -> gColor.get(CompositeTextureData.colortex6);
            case colortex7 -> gColor.get(CompositeTextureData.colortex7);
            case colortex8 -> gColor.get(CompositeTextureData.colortex8);
            case colortex9 -> gColor.get(CompositeTextureData.colortex9);
            case colortex10 -> gColor.get(CompositeTextureData.colortex10);
            case colortex11 -> gColor.get(CompositeTextureData.colortex11);
            case colortex12 -> gColor.get(CompositeTextureData.colortex12);
            case colortex13 -> gColor.get(CompositeTextureData.colortex13);
            case colortex14 -> gColor.get(CompositeTextureData.colortex14);
            case colortex15 -> gColor.get(CompositeTextureData.colortex15);
            case shadowtex0 -> shadowDepthTex0;
            case shadowtex1 -> shadowDepthTex1;
            case depthtex0 -> depthTex0;
            case depthtex1 -> depthTex1;
            case depthtex2 -> depthTex2;
            case shadowcolor0 -> shadowColorTex0;
            case shadowcolor1 -> shadowColorTex1;
            default -> null;
        };
    }

    public void resize(int width, int height) {
        gColor.resize(width, height);
        gDepthTex.resize(width, height);
        depthTex0.resize(width, height);
        depthTex1.resize(width, height);
        depthTex2.resize(width, height);
    }

    public void attachTo(List<GBufferShader> gBufferList) {
        for (val shader : gBufferList) {
            if (shader.framebuffer != null) {
                shader.framebuffer.deinit();
            }
            shader.framebuffer = Framebuffer.create(shader.loc()
                                                          .getResourcePath(),
                                                    gColor.getTexturesByIndex(shader.renderTargets()),
                                                    gDepthTex);
        }
    }

    public static void detach(List<GBufferShader> gBufferList) {
        for (val shader : gBufferList) {
            if (shader.framebuffer != null) {
                shader.framebuffer.deinit();
                shader.framebuffer = null;
            }
        }
    }

    public static Texture2D wrapMinecraftTexture() {
        val fb = Minecraft.getMinecraft()
                          .getFramebuffer();
        val gl = new GLTexture();
        gl.glName = fb.framebufferTexture;
        return new Texture2D("Minecraft", gl, GL11.GL_RGBA8, fb.framebufferWidth, fb.framebufferHeight);
    }

    public static Framebuffer wrapMinecraft() {
        return Framebuffer.wrap("Minecraft",
                                Minecraft.getMinecraft()
                                         .getFramebuffer().framebufferObject);
    }

    public static boolean isMinecraftUpToDate(Framebuffer fb, Texture2D texture) {
        if (fb == null) {
            return false;
        }
        val mc = Minecraft.getMinecraft()
                          .getFramebuffer();
        return fb.framebuffer.glName == mc.framebufferObject &&
               texture.glName() == mc.framebufferTexture &&
               texture.width() == mc.framebufferWidth &&
               texture.height() == mc.framebufferHeight;
    }

    public void deinit() {
        if (deinited) {
            return;
        }
        deinited = true;
        gColor.deinit();
        gDepthTex.deinit();
        depthTex0.deinit();
        depthTex1.deinit();
        depthTex2.deinit();
        _final.deinit();
        tempColor.deinit();
        tempDepth.deinit();
        if (shadow != null) {
            shadow.deinit();
        }
        if (shadowDepthTex0 != null) {
            shadowDepthTex0.deinit();
        }
        if (shadowDepthTex1 != null) {
            shadowDepthTex1.deinit();
        }
        if (shadowColorTex0 != null) {
            shadowColorTex0.deinit();
        }
        if (shadowColorTex1 != null) {
            shadowColorTex1.deinit();
        }
    }
}
