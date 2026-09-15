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

import com.ventooth.swansong.Share;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

import java.nio.ByteBuffer;

public final class DepthSampler {
    private static final Logger log = Share.getLogger();

    /**
     * Generally this should be a multiple of: <a href="https://opengl.gpuinfo.org/displaycapability.php?name=GL_MIN_MAP_BUFFER_ALIGNMENT">GL_MIN_MAP_BUFFER_ALIGNMENT</a>
     * <p>
     * Despite the fact that we need just 4 bytes for the single float.
     */
    private static final int BUFFER_SIZE_BYTES = 64;

    // TODO: There are two implementations of the "readback" from the GPU. Our goal is to avoid stalls, so use the faster one.
    private static final boolean USE_MAPPING = true;

    private static final int DELAY = 5;

    private int @Nullable [] glBuffers;
    private @Nullable ByteBuffer resultBuf;

    private int index;

    public DepthSampler() {
        this.glBuffers = null;
        this.resultBuf = null;

        this.index = 0;
    }

    public void init() {
        assert glBuffers == null;

        glBuffers = new int[DELAY];
        for (var i = 0; i < DELAY; i++) {
            val buf = GL15.glGenBuffers();
            glBuffers[i] = buf;

            GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, buf);
            // TODO: Test swapping out STREAM for DYNAMIC? It's a hint anyway so shouldn't matter.
            GL15.glBufferData(GL21.GL_PIXEL_PACK_BUFFER, BUFFER_SIZE_BYTES, GL15.GL_STREAM_READ);
        }
        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);

        if (!USE_MAPPING) {
            resultBuf = BufferUtils.createByteBuffer(BUFFER_SIZE_BYTES);
        }

        index = 0;

        log.debug("Initialized");
        if (USE_MAPPING) {
            log.debug("Mapped buffers enabled");
            log.debug("Using: {} buffers, each sized at: {} bytes", DELAY, BUFFER_SIZE_BYTES);
        } else {
            log.debug("Mapped buffers disabled (fallback)");
        }
    }

    public void deinit() {
        assert glBuffers != null;

        for (var i = 0; i < DELAY; i++) {
            val buf = glBuffers[i];
            GL15.glDeleteBuffers(buf);
        }

        glBuffers = null;
        resultBuf = null;
        index = 0;

        log.debug("Deinitialized");
    }

    /**
     * Schedules the next sample in the chain.
     * <p>
     * This must ONLY be called ONCE per frame as it increments the internal index counter.
     * <p>
     * The sampled depth is that of the currently bound framebuffer.
     *
     * @apiNote Should be called when {@link DriverHook#PRE_WATER} is fired, but before hand is drawn.
     */
    public void scheduleSample(int centerX, int centerY) {
        assert glBuffers != null;

        val buf = glBuffers[index];
        index = (index + 1) % DELAY;

        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, buf);

        // TODO: This part seems to choke on Nvidia drivers?
        GL11.glReadPixels(centerX, centerY, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, 0L);
        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);
    }

    /**
     * @return The latest center depth sample
     *
     * @apiNote Should be called in {@link ShaderEngine#beginRenderAll()} before anything else renders
     */
    public float getSample() {
        try {
            // TODO: Caused by OpenComputers robot rendering in inventory in resonant rise
            if (glBuffers == null) {
                throw new NullPointerException("glBuffers not initialized!");
            }
            return USE_MAPPING ? getSampleWithMapping() : getSampleNoMapping();
        } catch (Exception e) {
            log.error("Failed to sample center depth: ", e);
            return 0;
        }
    }

    private float getSampleWithMapping() {
        assert glBuffers != null;

        try {
            val buf = glBuffers[index];
            GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, buf);
            resultBuf = GL15.glMapBuffer(GL21.GL_PIXEL_PACK_BUFFER, GL15.GL_READ_ONLY, BUFFER_SIZE_BYTES, resultBuf);
            return resultBuf.getFloat(0);
        } finally {
            GL15.glUnmapBuffer(GL21.GL_PIXEL_PACK_BUFFER);
            GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);
        }
    }

    private float getSampleNoMapping() {
        assert glBuffers != null;

        // Was set on init if mapping disabled
        assert resultBuf != null;

        try {
            val buf = glBuffers[index];
            GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, buf);
            GL15.glGetBufferSubData(GL21.GL_PIXEL_PACK_BUFFER, 0, resultBuf);
            return resultBuf.getFloat(0);
        } finally {
            GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);
        }
    }
}
