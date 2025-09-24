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

import com.ventooth.swansong.debug.DebugMarker;
import com.ventooth.swansong.debug.GLObjectLabel;
import com.ventooth.swansong.gl.GLFramebuffer;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.Map;

@Accessors(fluent = true,
           chain = false)
public class Framebuffer {

    @Getter
    protected final String name;
    public final GLFramebuffer framebuffer;

    // TODO: Dumb flag, only exists because I'm dumb and wrap the Minecraft FB (shouldn't do that)
    boolean isManaged = true;

    public Framebuffer(String name, GLFramebuffer framebuffer) {
        this.name = name;
        this.framebuffer = framebuffer;

        if (GLObjectLabel.isEnabled() && name != null && !name.isEmpty()) {
            GLObjectLabel.set(framebuffer, name);
        }
    }

    public static Framebuffer wrap(String name, int glName) {
        val framebuffer = new GLFramebuffer();
        framebuffer.glName = glName;

        val fb = new Framebuffer(name, framebuffer);
        fb.isManaged = false;
        return fb;
    }

    public static Framebuffer create(String name, boolean depthOnly) {
        val framebuffer = new GLFramebuffer();
        framebuffer.glGenFramebuffers();
        framebuffer.glBindFramebuffer();

        if (depthOnly) {
            GL20.glDrawBuffers(GL11.GL_NONE);
        } else {
            GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
        }

        return new Framebuffer(name, framebuffer);
    }

    public static Framebuffer create(String name, Texture2D colorTex) {
        val framebuffer = new GLFramebuffer();
        framebuffer.glGenFramebuffers();
        framebuffer.glBindFramebuffer();

        colorTex.attachToFramebufferColor(GL30.GL_COLOR_ATTACHMENT0);

        GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        return new Framebuffer(name, framebuffer);
    }

    public static Framebuffer create(String name, Texture2D colorTex, Texture2D depthTex) {
        val framebuffer = new GLFramebuffer();
        framebuffer.glGenFramebuffers();
        framebuffer.glBindFramebuffer();

        depthTex.attachToFramebufferDepth();
        colorTex.attachToFramebufferColor(GL30.GL_COLOR_ATTACHMENT0);

        GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        return new Framebuffer(name, framebuffer);
    }

    private static void attachColorAndSetDrawBuffers(Map<FramebufferAttachment, Texture2D> colorTexList) {
        val size = colorTexList.size();
        val drawBuffers = BufferUtils.createIntBuffer(size);
        for (val entry: colorTexList.entrySet()) {
            val attachment = entry.getKey().toGLConstant();
            val tex = entry.getValue();
            tex.attachToFramebufferColor(attachment);
            drawBuffers.put(attachment);
        }
        drawBuffers.flip();

        GL20.glDrawBuffers(drawBuffers);
    }

    public static Framebuffer create(String name, Map<FramebufferAttachment, Texture2D> colorTexList, Texture2D depthTex) {
        val framebuffer = new GLFramebuffer();
        framebuffer.glGenFramebuffers();
        framebuffer.glBindFramebuffer();

        depthTex.attachToFramebufferDepth();

        attachColorAndSetDrawBuffers(colorTexList);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        return new Framebuffer(name, framebuffer);
    }

    public static Framebuffer create(String name, Map<FramebufferAttachment, Texture2D> colorTexList) {
        val framebuffer = new GLFramebuffer();
        framebuffer.glGenFramebuffers();
        framebuffer.glBindFramebuffer();

        attachColorAndSetDrawBuffers(colorTexList);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        return new Framebuffer(name, framebuffer);
    }

    public static Framebuffer create(String name, Texture2D colorTex0, Texture2D colorTex1, Texture2D depthTex) {
        val framebuffer = new GLFramebuffer();
        framebuffer.glGenFramebuffers();
        framebuffer.glBindFramebuffer();

        depthTex.attachToFramebufferDepth();
        colorTex0.attachToFramebufferColor(GL30.GL_COLOR_ATTACHMENT0);
        colorTex1.attachToFramebufferColor(GL30.GL_COLOR_ATTACHMENT1);

        val drawBuffers = BufferUtils.createIntBuffer(2);
        drawBuffers.put(GL30.GL_COLOR_ATTACHMENT0);
        drawBuffers.put(GL30.GL_COLOR_ATTACHMENT1);
        drawBuffers.flip();

        GL20.glDrawBuffers(drawBuffers);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        return new Framebuffer(name, framebuffer);
    }

    public void bind() {
        framebuffer.glBindFramebuffer();
        DebugMarker.FRAMEBUFFER_BIND.insertFormat("[{0}]", name);
    }

    public void bindDraw() {
        framebuffer.glBindFramebufferDraw();
        DebugMarker.FRAMEBUFFER_BIND_DRAW.insertFormat("[{0}]", name);
    }

    public void bindRead() {
        framebuffer.glBindFramebufferRead();
        DebugMarker.FRAMEBUFFER_BIND_DRAW.insertFormat("[{0}]", name);
    }

    public void deinit() {
        if (isManaged) {
            framebuffer.glDeleteFramebuffers();
        }
    }

    public void clear() {
        framebuffer.glBindFramebuffer();


        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}
