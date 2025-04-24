/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.sufrace;


import com.ventooth.swansong.debug.GLObjectLabel;
import com.ventooth.swansong.gl.GLTexture;
import com.ventooth.swansong.image.ImageUtils;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.intellij.lang.annotations.MagicConstant;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import java.awt.image.BufferedImage;
import java.lang.management.RuntimeMXBean;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

@Accessors(fluent = true,
           chain = false)
public class Texture2D {
    @Getter
    private final String name;
    private final GLTexture texture;
    @Getter
    private final int internalFormat;
    @Getter
    private int width;
    @Getter
    private int height;

    public Texture2D(String name, GLTexture texture, int internalFormat, int width, int height) {
        this.name = name;
        this.texture = texture;
        this.internalFormat = internalFormat;
        this.width = width;
        this.height = height;

        if (GLObjectLabel.isEnabled()) {
            GLObjectLabel.set(this.texture, name);
        }
    }

    public static boolean sizeEquals(Texture2D a, Texture2D b) {
        return a.width == b.width && a.height == b.height;
    }

    public int glName() {
        return texture.glName;
    }

    public void resize(int width, int height) {
        val isDepth = internalFormat == GL11.GL_DEPTH_COMPONENT;

        texture.glBindTexture2D();
        if (isDepth) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                              0,
                              GL11.GL_DEPTH_COMPONENT,
                              width,
                              height,
                              0,
                              GL11.GL_DEPTH_COMPONENT,
                              GL11.GL_FLOAT,
                              (ByteBuffer) null);
        } else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                              0,
                              internalFormat,
                              width,
                              height,
                              0,
                              GL12.GL_BGRA,
                              GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                              (ByteBuffer) null);
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        this.width = width;
        this.height = height;
    }

    // region Factory

    /**
     * @apiNote Texture remains bound after call
     */
    public static Texture2D ofColorDrawBuffer(String name, int width, int height, int internalformat) {
        val texture = new GLTexture();
        texture.glGenTextures();
        texture.glBindTexture2D();

        setParams2D(GL11.GL_CLAMP, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                          0,
                          internalformat,
                          width,
                          height,
                          0,
                          GL12.GL_BGRA,
                          GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                          (ByteBuffer) null);

        return new Texture2D(name, texture, internalformat, width, height);
    }

    /**
     * @apiNote Texture remains bound after call
     */
    public static Texture2D ofDepthDrawBuffer(String name, int width, int height) {
        val texture = new GLTexture();
        texture.glGenTextures();
        texture.glBindTexture2D();

        setParams2D(GL11.GL_CLAMP, GL11.GL_NEAREST);
        // Honestly, still unsure what this even does!
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_DEPTH_TEXTURE_MODE, GL11.GL_LUMINANCE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                          0,
                          GL11.GL_DEPTH_COMPONENT,
                          width,
                          height,
                          0,
                          GL11.GL_DEPTH_COMPONENT,
                          GL11.GL_FLOAT,
                          (ByteBuffer) null);

        return new Texture2D(name, texture, GL11.GL_DEPTH_COMPONENT, width, height);
    }

    /**
     * @apiNote Texture remains bound after call
     */
    public static Texture2D ofShadowDepthBuffer(String name,
                                                int width,
                                                int height,
                                                boolean nearest,
                                                boolean mipped,
                                                boolean hwFiltering) {
        val texture = new GLTexture();
        texture.glGenTextures();
        texture.glBindTexture2D();

        setParams2D(GL11.GL_CLAMP, getMinFilter(nearest, mipped), getMagFilter(nearest));
        if (hwFiltering) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL30.GL_COMPARE_REF_TO_TEXTURE);
        }
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                          0,
                          GL11.GL_DEPTH_COMPONENT,
                          width,
                          height,
                          0,
                          GL11.GL_DEPTH_COMPONENT,
                          GL11.GL_FLOAT,
                          (ByteBuffer) null);

        return new Texture2D(name, texture, GL11.GL_DEPTH_COMPONENT, width, height);
    }

    /**
     * @apiNote Texture remains bound after call
     */
    public static Texture2D ofShadowColorBuffer(String name, int width, int height, boolean nearest, boolean mipped) {
        val texture = new GLTexture();
        texture.glGenTextures();
        texture.glBindTexture2D();

        setParams2D(GL11.GL_CLAMP, getMinFilter(nearest, mipped), getMagFilter(nearest));
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                          0,
                          GL11.GL_RGBA,
                          width,
                          height,
                          0,
                          GL12.GL_BGRA,
                          GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                          (ByteBuffer) null);

        return new Texture2D(name, texture, GL11.GL_RGBA, width, height);
    }

    /**
     * @apiNote Texture remains bound after call
     */
    public static Texture2D ofColoredPixel(String name, int rgba) {
        val texture = new GLTexture();
        texture.glGenTextures();
        texture.glBindTexture2D();

        setParams2D(GL11.GL_CLAMP, GL11.GL_NEAREST, GL11.GL_NEAREST);

        val pixel = BufferUtils.createIntBuffer(1);
        pixel.put(0, rgba);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                          0,
                          GL11.GL_RGBA,
                          1,
                          1,
                          0,
                          GL11.GL_RGBA,
                          GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
                          pixel);

        return new Texture2D(name, texture, GL11.GL_RGBA, 1, 1);
    }

    public static void setParams2D(boolean clamp, boolean blur) {
        setParams2D(clamp ? GL11.GL_CLAMP : GL11.GL_REPEAT, blur ? GL11.GL_LINEAR : GL11.GL_NEAREST);
    }

    public static void setParams2D(@MagicConstant(intValues = {GL11.GL_CLAMP, GL11.GL_REPEAT}) int wrap,
                                   @MagicConstant(intValues = {GL11.GL_NEAREST, GL11.GL_LINEAR}) int filter) {
        setParams2D(wrap, filter, filter);
    }

    public static void setParams2D(@MagicConstant(intValues = {GL11.GL_CLAMP, GL11.GL_REPEAT}) int wrap,
                                   @MagicConstant(intValues = {GL11.GL_NEAREST,
                                                               GL11.GL_LINEAR,
                                                               GL11.GL_NEAREST_MIPMAP_NEAREST,
                                                               GL11.GL_LINEAR_MIPMAP_LINEAR}) int minFilter,
                                   @MagicConstant(intValues = {GL11.GL_NEAREST, GL11.GL_LINEAR}) int magFilter) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
    }

    private static @MagicConstant(intValues = {GL11.GL_NEAREST,
                                               GL11.GL_LINEAR,
                                               GL11.GL_NEAREST_MIPMAP_NEAREST,
                                               GL11.GL_LINEAR_MIPMAP_LINEAR}) int getMinFilter(boolean nearest,
                                                                                               boolean mipped) {
        if (mipped) {
            return nearest ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_LINEAR_MIPMAP_LINEAR;
        } else {
            return nearest ? GL11.GL_NEAREST : GL11.GL_LINEAR;
        }
    }

    public static @MagicConstant(intValues = {GL11.GL_NEAREST, GL11.GL_LINEAR}) int getMagFilter(boolean nearest) {
        return nearest ? GL11.GL_NEAREST : GL11.GL_LINEAR;
    }

    // endregion

    public void deinit() {
        texture.glDeleteShader();
    }

    public void bind() {
        texture.glBindTexture2D();
    }

    public void attachToFramebufferColor(int attachment) {
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, GL11.GL_TEXTURE_2D, texture.glName, 0);
    }

    public void attachToFramebufferDepth() {
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,
                                    GL30.GL_DEPTH_ATTACHMENT,
                                    GL11.GL_TEXTURE_2D,
                                    texture.glName,
                                    0);
    }

    @Deprecated
    private BufferedImage img; // ONLY HERE FOR INTELLIJ DEBUGGING
    private static final boolean IS_DEBUGGER_PRESENT = isDebuggerPresent();

    @Override
    public String toString() {
        if (IS_DEBUGGER_PRESENT) {
            img = ImageUtils.downloadGLTextureAsBGRA(texture.glName)
                            .asBufImg(true, true);
        }
        return super.toString();
    }

    private static boolean isDebuggerPresent() {
        // Get ahold of the Java Runtime Environment (JRE) management interface
        RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();

        // Get the command line arguments that we were originally passed in
        List<String> args = runtime.getInputArguments();

        // Check if the Java Debug Wire Protocol (JDWP) agent is used.
        // One of the items might contain something like "-agentlib:jdwp=transport=dt_socket,address=9009,server=y,suspend=n"
        // We're looking for the string "jdwp".
        boolean jdwpPresent = args.toString()
                                  .contains("jdwp");

        return jdwpPresent;
    }
}
