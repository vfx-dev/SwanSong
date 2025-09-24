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
import com.ventooth.swansong.debug.DebugMarker;
import com.ventooth.swansong.debug.GLDebugGroups;
import com.ventooth.swansong.mixin.extensions.WorldRendererExt;
import com.ventooth.swansong.mixin.interfaces.ShaderGameSettings;
import com.ventooth.swansong.resources.ShaderPackManager;
import com.ventooth.swansong.resources.pack.DefaultShaderPack;
import com.ventooth.swansong.shader.StateGraph.Node;
import com.ventooth.swansong.shader.config.ConfigEntry;
import com.ventooth.swansong.shader.shaderobjects.CompositeShader;
import com.ventooth.swansong.shader.shaderobjects.GBufferShader;
import com.ventooth.swansong.shader.shaderobjects.ManagedShader;
import com.ventooth.swansong.shader.shaderobjects.ShadowShader;
import com.ventooth.swansong.shader.texbuf.CompositePipeline;
import com.ventooth.swansong.sufrace.CustomTexture2D;
import com.ventooth.swansong.sufrace.Framebuffer;
import com.ventooth.swansong.sufrace.HFNoiseTexture2D;
import com.ventooth.swansong.sufrace.Texture2D;
import com.ventooth.swansong.uniforms.StatefulBuiltins;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.Locale;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.ForgeHooksClient;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// [ShaderType] -> Had a resource location, and can create a Managed Program
// Provided defaults are: composite-style and gbuffers-style

// [ManagedShader] -> Is created by a ShaderType, generally contains references to the OpenGL handles.
// Also manages the uniforms and framebuffer attachments relevant to itself.

// [GLShader/GLProgram] -> Containers for OpenGL handles
// Loose, open, no magic or implicit behaviour, simple wrappers on top of the opengl functions

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderEngine {
    public static final boolean DO_GRAPH_LOG = false;
    public static final Logger log = Share.getLogger();

    static final ShaderEntityData shaderData = ShaderEntityData.get();

    private static final Matrix4d tempMat4 = new Matrix4d();
    private static final DoubleBuffer tempDoubleBuffer = BufferUtils.createDoubleBuffer(16);

    static @Nullable FixedEngineState state;

    /// runtime varying state

    // TODO: [CUSTOM_TEX] Do we want to keep it loose like this?
    private static Map<CompositeTextureData, Texture2D> gbuffersCustomTex = Collections.emptyMap();
    private static Map<CompositeTextureData, Texture2D> compositeCustomTex = Collections.emptyMap();
    private static Map<CompositeTextureData, Texture2D> deferredCustomTex = Collections.emptyMap();

    // TODO: [CUSTOM_TEX] Do we bundle this with the custom textures?
    private static Texture2D noiseTex;

    private static DrawBuffers buffers;

    private static @Nullable CompositePipeline deferredPipeline;
    private static @Nullable CompositePipeline compositePipeline;
    private static @Nullable CompositePipeline finalPipeline;
    private static Framebuffer mcFramebuffer;
    private static Texture2D mcTexture;
    // TODO [SAMPLER]: Move to a better spot
    private static int blitSrcSampler;

    private static final AbstractObjectList<ManagedShader> shaderStack = new ObjectArrayList<>();

    public static boolean needsFramebufferResize;
    public static boolean needsShaderPackReload;

    private static boolean useShaderLocked = false;
    private static int shaderSwitches = 0;
    public static int prevFrameShaderSwitches = 0;
    public static List<Node> graphLog = new ArrayList<>();

    public static StateGraph graph = new StateGraph();

    public static Locale locale() {
        return state == null ? null : state.locale;
    }

    public static ConfigEntry.RootScreen configScreen() {
        return state == null ? null : state.configScreen;
    }

    public static boolean isInitialized() {
        return state != null;
    }

    public static boolean shadowPassExists() {
        if (state == null) {
            return false;
        } else {
            return state.shadow != null;
        }
    }

    static void renderHand() {
        sampleCenterDepth();
        DebugMarker.GENERIC.insert("PRE_COPY_DEPTH_2");
        blitDepth(buffers.gDepthTex, buffers.depthTex2);
        DebugMarker.GENERIC.insert("POST_COPY_DEPTH_2");

        if (!ShaderState.isHeldItemTranslucent()) {
            val mc = Minecraft.getMinecraft();

            val isGuiVisible = !mc.gameSettings.hideGUI;
            val isFirstPerson = mc.gameSettings.thirdPersonView == 0;
            val isSleeping = mc.renderViewEntity.isPlayerSleeping();

            if (isGuiVisible && isFirstPerson && !isSleeping) {
                renderHand(false);
            }
        }

        DebugMarker.GENERIC.insert("PRE_COPY_DEPTH_0");
        blitDepth(buffers.gDepthTex, buffers.depthTex0);
        DebugMarker.GENERIC.insert("POST_COPY_DEPTH_0");

        DebugMarker.GENERIC.insert("PRE_COPY_DEPTH_1");
        blitDepth(buffers.gDepthTex, buffers.depthTex1);
        DebugMarker.GENERIC.insert("POST_COPY_DEPTH_1");
    }

    private static void sampleCenterDepth() {
        assert state != null : "Not Initialized";

        if (state.depthSampler != null) {
            buffers.tempDepth.bindRead();

            // TODO: .attachToFramebufferDepth() only attaches directly to a FB, here we use the READ binding ONLY!
            GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER,
                                        GL30.GL_DEPTH_ATTACHMENT,
                                        GL11.GL_TEXTURE_2D,
                                        buffers.gDepthTex.glName(),
                                        0);

            val viewSize = ShaderState.viewSize();
            val centerX = viewSize.x() / 2;
            val centerY = viewSize.y() / 2;
            state.depthSampler.scheduleSample(centerX, centerY);

            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
        }
    }

    public static boolean hasPortalShader() {
        if (state == null) {
            return false;
        }
        return !state.manager.portal.isFallback();
    }

    public static void beginRenderAll() {
        needsFramebufferResize = ShaderState.updateViewSize();
        if (needsShaderPackReload) {
            doShaderPackReload();
        } else if (needsFramebufferResize) {
            doFramebufferResize();
        }
        needsShaderPackReload = false;
        needsFramebufferResize = false;

        assert state != null : "Not Initialized";
        prevFrameShaderSwitches = shaderSwitches;
        if (DO_GRAPH_LOG) {
            graphLog.clear();
            graphLog.addAll(graph.graphLog);
            graph.graphLog.clear();
        }
        shaderSwitches = 0;

        clearColorBufs();
        use(null);
        mcFramebuffer.bind();
    }

    public static void endRenderAll() {

    }

    private static void alphaAndDepthClear() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GL11.glDepthMask(true);
        GL11.glColorMask(false, false, false, true);
        GL11.glClearColor(1F, 1F, 1F, 1F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glPopAttrib();
    }

    private static void renderHand(boolean isTranslucent) {
        assert state != null : "Not Initialized";

        val mc = Minecraft.getMinecraft();
        val partialTick = ShaderState.getSubTick();
        val entityRenderer = Minecraft.getMinecraft().entityRenderer;
        val anaglyph = ((ShaderGameSettings) mc.gameSettings).swan$anaglyph();
        val anaglyphField = EntityRenderer.anaglyphField;

        // Some weird state is leaked somewhere in the pipeline, and this ensures we're working with the base value
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();

        // TODO: Shaders/Config should define this!
        val handDepth = 0.125F;

        {
            val fov = Math.toRadians(entityRenderer.getFOVModifier(partialTick, false));
            val aspect = ShaderState.aspectRatio();
            val near = 0.05;
            val far = entityRenderer.farPlaneDistance * 2;
            tempDoubleBuffer.clear();
            tempMat4.scaling(1, 1, handDepth);
            if (anaglyph != 0) {
                tempMat4.translate((float) (-(anaglyphField * 2 - 1)) * 0.07f * anaglyph, 0, 0);
            }
            tempMat4.perspective(fov, aspect, near, far);
            tempMat4.get(tempDoubleBuffer);
            GL11.glLoadMatrix(tempDoubleBuffer);
        }

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        if (anaglyph != 0) {
            GL11.glTranslatef((float) (anaglyphField * 2 - 1) * 0.1f * anaglyph, 0, 0);
        }

        entityRenderer.hurtCameraEffect(partialTick);

        if (mc.gameSettings.viewBobbing) {
            entityRenderer.setupViewBobbing(partialTick);
        }


        val lastShader = state.manager.current();

        if (isTranslucent) {
            ShaderState.updateRenderStage(MCRenderStage.HAND_TRANSLUCENT);
            use(state.manager.hand_water);
        } else {
            ShaderState.updateRenderStage(MCRenderStage.HAND_SOLID);
            use(state.manager.hand);
        }

        GL11.glDepthMask(true);

        entityRenderer.enableLightmap(partialTick);
        entityRenderer.itemRenderer.renderItemInFirstPerson(partialTick);
        entityRenderer.disableLightmap(partialTick);

        use(lastShader);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();

        GL11.glPopAttrib();

        ShaderState.updateRenderStage(MCRenderStage.NONE);
    }

    private static void captureLastDepth() {
        if (ShaderState.isHeldItemTranslucent()) {
            val mc = Minecraft.getMinecraft();

            val isGuiVisible = !mc.gameSettings.hideGUI;
            val isFirstPerson = mc.gameSettings.thirdPersonView == 0;
            val isSleeping = mc.renderViewEntity.isPlayerSleeping();

            if (isGuiVisible && isFirstPerson && !isSleeping) {
                renderHand(true);
            }
        }

        DebugMarker.GENERIC.insert("PRE_COPY_DEPTH_0");

        blitDepth(buffers.gDepthTex, buffers.depthTex0);

        DebugMarker.GENERIC.insert("POST_COPY_DEPTH_0");
    }

    private static void renderComposite() {
        assert state != null : "Not Initialized";
        if (compositePipeline != null) {
            GLDebugGroups.RENDER_COMPOSITE.push();
            compositePipeline.run();
            GLDebugGroups.RENDER_COMPOSITE.pop();
        }
    }

    private static void renderFinal() {
        assert state != null : "Not Initialized";
        val anaglyph = ((ShaderGameSettings) Minecraft.getMinecraft().gameSettings).swan$anaglyph();

        if (finalPipeline != null) {
            // Actual shader pass doing it
            finalPipeline.run();
        } else {
            // Our poor blit...

            val src = buffers.gColor.get(CompositeTextureData.colortex0);
            val dst = mcTexture;

            if (Texture2D.sizeEquals(src, dst)) {
                use(state.manager.blit_color_identical);
            } else {
                use(state.manager.blit_color_mismatched);
            }
            // I'm paranoid.
            Minecraft.getMinecraft()
                     .getFramebuffer()
                     .bindFramebuffer(false);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.blitsrc.gpuIndex());
            src.bind();
            GL13.glActiveTexture(GL13.GL_TEXTURE0);

            if (anaglyph != 0) {
                ShadersCompositeMesh.drawWithAnaglyphField(EntityRenderer.anaglyphField);
            } else {
                ShadersCompositeMesh.drawWithColor();
            }
            if (DebugMarker.isEnabled()) {
                DebugMarker.TEXTURE_COLOR_BLIT.insertFormat("{0} -> {1}", src.name(), mcTexture.name());
            }
        }
    }

    private static @Nullable WorldProvider mcDimensionID() {
        val mc = Minecraft.getMinecraft();
        val world = mc.theWorld;
        if (world == null) {
            return null;
        }
        val provider = world.provider;
        if (provider == null) {
            return null;
        }
        return provider;
    }

    /**
     * Called once during initialization
     */
    public static void firstInit() {
        if (state != null) {
            throw new IllegalStateException("First init called twice!");
        }

        log.info("Initializing for the very first time...");
        try {
            ShaderState.updateViewSize();
            doShaderPackReload();
        } catch (RuntimeException | Error e) {
            log.fatal("Failed to initialize: ", e);
            throw e;
        }
        log.info("Successful Init");
    }

    /**
     * Called to reload the shader
     */
    public static void scheduleShaderPackReload() {
        assert state != null : "Not Initialized";
        ShaderEngine.log.debug("Scheduled ShaderPack Reload");
        needsShaderPackReload = true;
    }

    /**
     * Called any time the window resizes
     */
    public static void scheduleFramebufferResize() {
        assert state != null : "Not Initialized";
        ShaderEngine.log.debug("Scheduled Framebuffer Resize");
        needsFramebufferResize = true;
    }

    private static void doShaderPackReload() {
        deinit();
        val report = new Report();
        try {
            init(report);

            val viewSize = ShaderState.viewSize();
            val width = viewSize.x();
            val height = viewSize.y();
            resizeFramebuffers(width, height, report);

            needsShaderPackReload = false;
            needsFramebufferResize = false;

            report.endTime = System.nanoTime();
            report.print();
        } catch (RuntimeException e) {
            report.endTime = System.nanoTime();
            report.print();
            if (DefaultShaderPack.NAME.equals(ShaderPackManager.getCurrentShaderPackName())) {
                ShaderEngine.log.fatal("Failed to load internal shaderpack! Unrecoverable.");
                ShaderEngine.log.fatal("Please report this as a bug:", e);
                throw e;
            } else {
                ShaderEngine.log.error("Caught internal error while loading shaderpack!");
                ShaderEngine.log.error("Please report this as a bug:", e);
                ShaderEngine.log.error("Now attempting to load the fallback");
                // TODO: This should use the (internal) shaderpack! As we will still crash if the bad shader comes from the resourcepack!
                ShaderPackManager.setShaderPackByName(DefaultShaderPack.NAME);
                doShaderPackReload();
            }
        }
    }

    private static void doFramebufferResize() {
        val viewSize = ShaderState.viewSize();
        val width = viewSize.x();
        val height = viewSize.y();
        resizeFramebuffers(width, height, null);
        needsFramebufferResize = false;
    }

    private static void init(Report report) {
        report.startTime = System.nanoTime();
        state = FixedEngineState.init(mcDimensionID(), report);
        use(null);

        ShadersCompositeMesh.init();

        gbuffersCustomTex = new EnumMap<>(CompositeTextureData.class);
        compositeCustomTex = new EnumMap<>(CompositeTextureData.class);
        deferredCustomTex = new EnumMap<>(CompositeTextureData.class);

        if (!state.textures.isEmpty()) {
            for (val stagedTex : state.textures) {
                val index = BufferNameUtil.gbufferIndexFromName(stagedTex.bufferName());
                if (index == null) {
                    Share.log.error("Unknown buffer index for custom texture: {}", stagedTex);
                    continue;
                }

                val stagedCustomTexMap = switch (stagedTex.stage()) {
                    case "gbuffers" -> gbuffersCustomTex;
                    case "composite" -> compositeCustomTex;
                    case "deferred" -> deferredCustomTex;
                    default -> null;
                };
                if (stagedCustomTexMap == null) {
                    Share.log.error("Unknown stage for custom texture: {}", stagedTex);
                    continue;
                }

                if (stagedCustomTexMap.containsKey(index)) {
                    Share.log.error("Duplicate custom texture {} ignored", stagedTex);
                    continue;
                }

                val path = "/shaders/" + stagedTex.path();
                val customTex = CustomTexture2D.load(state.pack, path);
                if (customTex == null) {
                    Share.log.error("Missing custom texture {}, on exact path: {}", stagedTex, path);
                    continue;
                }

                stagedCustomTexMap.put(index, customTex);
                Share.log.debug("Loaded Custom Texture: {}", stagedTex);
                // TODO: Make format look like: deferred.colortex3<TAB>lib/textures/cloud-water.png<TAB>RGBA<TAB>256x256
                report.customTextures.put(stagedTex.path(),
                                          new Report.TextureInfo(customTex.width(),
                                                                 customTex.height(),
                                                                 BufferNameUtil.gbufferFormatNameFromEnum(customTex.internalFormat())));
            }
        }

        if (gbuffersCustomTex.isEmpty()) {
            gbuffersCustomTex = Collections.emptyMap();
        } else {
            gbuffersCustomTex = Collections.unmodifiableMap(gbuffersCustomTex);
        }
        if (compositeCustomTex.isEmpty()) {
            compositeCustomTex = Collections.emptyMap();
        } else {
            compositeCustomTex = Collections.unmodifiableMap(compositeCustomTex);
        }
        if (deferredCustomTex.isEmpty()) {
            deferredCustomTex = Collections.emptyMap();
        } else {
            deferredCustomTex = Collections.unmodifiableMap(deferredCustomTex);
        }

        if (state.noiseTexPath != null) {
            val path = "/shaders/" + state.noiseTexPath;
            noiseTex = CustomTexture2D.load(state.pack, path);
            if (noiseTex == null) {
                Share.log.error("Missing noise texture: {}", path);
            } else {
                // TODO: Make format look like: noise<TAB>lib/textures/noise.png<TAB>RGBA<TAB>128x128
                report.customTextures.put(state.noiseTexPath,
                                          new Report.TextureInfo(noiseTex.width(),
                                                                 noiseTex.height(),
                                                                 BufferNameUtil.gbufferFormatNameFromEnum(noiseTex.internalFormat())));
            }
        } else if (state.noiseTexSize != null) {
            noiseTex = HFNoiseTexture2D.create(state.noiseTexSize, state.noiseTexSize);
            // TODO: Make format look like: noise<TAB>(builtin)<TAB>RGBA<TAB>128x128
            report.customTextures.put("noise",
                                      new Report.TextureInfo(noiseTex.width(),
                                                             noiseTex.height(),
                                                             BufferNameUtil.gbufferFormatNameFromEnum(noiseTex.internalFormat())));
        }

        StatefulBuiltins.reset();

        // Resets the vanilla renderers, important as the baked geometry may have invalid blockids
        Minecraft.getMinecraft().renderGlobal.loadRenderers();

        // TODO [SAMPLER]: Move to a better spot
        {
            blitSrcSampler = GL33.glGenSamplers();
            GL33.glSamplerParameteri(blitSrcSampler, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL33.glSamplerParameteri(blitSrcSampler, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
            GL33.glSamplerParameteri(blitSrcSampler, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL33.glSamplerParameteri(blitSrcSampler, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL33.glBindSampler(CompositeTextureData.blitsrc.gpuIndex(), blitSrcSampler);
        }
    }

    private static void deinit() {
        if (graph.isManaged()) {
            throw new IllegalStateException("Cannot deinit renderer while in managed mode!");
        }

        shaderData.reset();
        for (val tex : gbuffersCustomTex.values()) {
            tex.deinit();
        }
        gbuffersCustomTex = Collections.emptyMap();
        for (val tex : compositeCustomTex.values()) {
            tex.deinit();
        }
        compositeCustomTex = Collections.emptyMap();
        for (val tex : deferredCustomTex.values()) {
            tex.deinit();
        }
        deferredCustomTex = Collections.emptyMap();

        if (noiseTex != null) {
            noiseTex.deinit();
            noiseTex = null;
        }
        deinitFramebuffers();
        ShadersCompositeMesh.deinit();
        shaderStack.clear();
        if (state != null) {
            state.deinit();
            state = null;
        }

        // TODO [SAMPLER]: Move to a better spot
        if (blitSrcSampler != 0) {
            GL33.glDeleteSamplers(blitSrcSampler);
            blitSrcSampler = 0;
        }
    }

    public static void runDeferredPipeline() {
        if (deferredPipeline != null) {
            GLDebugGroups.RENDER_DEFERRED.push();
            deferredPipeline.run();
            GLDebugGroups.RENDER_DEFERRED.pop();
        }
    }

    private static void resizeFramebuffers(int width, int height, @Nullable Report report) {
        assert state != null : "Not Initialized";

        if (buffers == null) {
            initFramebuffers(width, height, report);
            return;
        }
        buffers.resize(width, height);

        if (deferredPipeline != null) {
            deferredPipeline.resize(width, height);
        }
        if (compositePipeline != null) {
            compositePipeline.resize(width, height);
        }
        if (finalPipeline != null) {
            finalPipeline.resize(width, height);
        }

        if (!DrawBuffers.isMinecraftUpToDate(mcFramebuffer, mcTexture)) {
            mcFramebuffer = DrawBuffers.wrapMinecraft();
            mcTexture = DrawBuffers.wrapMinecraftTexture();
            if (state.manager._final != null) {
                state.manager._final.framebuffer = mcFramebuffer;
            }
        }

        ShaderEngine.log.debug("Resized Framebuffer: {}x{}", width, height);
    }

    private static void initFramebuffers(int width, int height, @Nullable Report report) {
        assert state != null : "Not Initialized";

        deinitFramebuffers();

        val fbBuilder = DrawBuffers.builder();
        fbBuilder.colorDrawBufferConfigs(state.colorDrawBufferConfigs);
        if (shadowPassExists()) {
            fbBuilder.shadow(state.shadow);
        }
        fbBuilder.width(width)
                 .height(height);
        buffers = fbBuilder.build();

        buffers.attachTo(state.manager.gBufferList);

        if (state.manager.deferredList != null) {
            deferredPipeline = CompositePipeline.buildPipeline("deferred",
                                                               "DA_",
                                                               state.colorDrawBufferConfigs,
                                                               deferredCustomTex,
                                                               buffers,
                                                               state.manager.deferredList,
                                                               width,
                                                               height,
                                                               report);
        }
        if (state.manager.compositeList != null) {
            compositePipeline = CompositePipeline.buildPipeline("composite",
                                                                "CA_",
                                                                state.colorDrawBufferConfigs,
                                                                compositeCustomTex,
                                                                buffers,
                                                                state.manager.compositeList,
                                                                width,
                                                                height,
                                                                report);
        }

        mcTexture = DrawBuffers.wrapMinecraftTexture();
        mcFramebuffer = DrawBuffers.wrapMinecraft();

        if (state.manager._final != null) {
            state.manager._final.framebuffer = mcFramebuffer;
            finalPipeline = CompositePipeline.buildPipelineFinal(buffers,
                                                                 ObjectLists.singleton(state.manager._final),
                                                                 compositeCustomTex,
                                                                 report);
        }

        ShaderEngine.log.debug("Initialized Framebuffers");
    }

    private static void deinitFramebuffers() {
        if (buffers != null) {
            buffers.deinit();
            buffers = null;
        }
        if (deferredPipeline != null) {
            deferredPipeline.deinit();
            deferredPipeline = null;
        }
        if (compositePipeline != null) {
            compositePipeline.deinit();
            compositePipeline = null;
        }
        if (finalPipeline != null) {
            finalPipeline.deinit();
            finalPipeline = null;
        }

        mcFramebuffer = null;
        if (state != null) {
            DrawBuffers.detach(state.manager.gBufferList);
            if (state.manager._final != null) {
                state.manager._final.framebuffer = null;
            }
        }

        ShaderEngine.log.debug("Deinitialized Framebuffers");
    }

    public static void beginRenderWorld() {
        assert state != null : "Not Initialized";

        graph.moveTo(Node.BeginFrame);
        clearColorBufs();

        if (state.depthSampler != null) {
            ShaderState.updateCenterDepth(state.depthSampler.getSample());
        }

        ShaderState.updatePreRenderWorld();

        if (state.compiledUniforms != null) {
            state.compiledUniforms.update();
        }

        if (noiseTex != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.noisetex.gpuIndex());
            noiseTex.bind();
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
        }

        ShaderState.updateCelestialAngle();

        renderShadowMap();
    }

    public static void finishRenderFinal() {
        ShaderState.updateRenderStage(MCRenderStage.NONE);

        captureLastDepth();
        // Composite first
        renderComposite();
        // Both binds the framebuffer AND sets the viewport!
        Minecraft.getMinecraft()
                 .getFramebuffer()
                 .bindFramebuffer(true);
        // This will either blit, or like the shader figures it out
        renderFinal();
        // Unbind whatever shader is bound.
        use(null);
        // We dont want translucency, or depth.. bleh...
        alphaAndDepthClear();
    }

    public static @Nullable Frustrum mcFrustrum;

    private static final Frustrum frustrum = new Frustrum();
    private static final ClippingHelperShadow ch = new ClippingHelperShadow();

    static {
        frustrum.clippingHelper = ch;
    }

    private static int shadowFrustumCheckOffset = 0;

    private static void clipRenderersByFrustumShadow(WorldRenderer[] wrs) {
        for (int i = 0, wrsLength = wrs.length; i < wrsLength; i++) {
            var wr = wrs[i];
            val wre = (WorldRendererExt) wr;
            wre.swan$backupFrustum();
            if (!wr.skipAllRenderPasses() && (!wr.isInFrustum || (i + shadowFrustumCheckOffset & 15) == 0)) {
                wr.updateInFrustum(frustrum);
            }
        }
        shadowFrustumCheckOffset++;
    }

    private static void addWorldToShadowReceivers(WorldRenderer[] wrs) {
        for (val wr : wrs) {
            val wre = (WorldRendererExt) wr;
            if (wr != null && wre.swan$initialized() && wr.isVisible && wr.isInFrustum && !wr.skipAllRenderPasses()) {
                ch.addShadowReceiver(wr);
            }
        }
    }

    private static void renderShadowMap() {
        assert state != null : "Not Initialized";
        if (state.shadow == null) {
            return;
        }
        GLDebugGroups.RENDER_SHADOW.push();

        val partialTicks = ShaderState.getSubTick();
        val entityRenderer = Minecraft.getMinecraft().entityRenderer;

        // Set to zero before pushing attribs
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        Minecraft mc = Minecraft.getMinecraft();
        RenderGlobal renderGlobal = mc.renderGlobal;
        graph.moveTo(Node.ShadowBegin);
        val preShadowPassThirdPersonView = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = 1;

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();

        entityRenderer.setupCameraTransform(partialTicks, 2);

        ShaderState.setCameraShadow(state.shadow.resolution,
                                    state.shadow.distance,
                                    state.shadow.fov,
                                    state.shadow.intervalSize);
        ActiveRenderInfo.updateRenderInfo(mc.thePlayer, false);

        buffers.shadow.bindDraw();

        GL11.glClearColor(1F, 1F, 1F, 1F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        val wrs = renderGlobal.sortedWorldRenderers;
        val numWrs = wrs.length;

        // region Shadow culling stuff
        val viewEntity = mc.renderViewEntity;
        ch.shadowModelViewMatrix.set(ShaderState.shadowModelView());

        ch.begin();

        addWorldToShadowReceivers(wrs);

        if (mcFrustrum != null) {
            try {
                // Defensive Copy (We do this once a frame, so should be ok?)
                val entities = mc.theWorld.loadedEntityList.toArray(new Entity[0]);
                val tileEntities = mc.theWorld.loadedTileEntityList.toArray(new TileEntity[0]);

                // TODO: Handling for infinite extent bounding boxes?
                for (val entity : entities) {
                    val aabb = entity.boundingBox;
                    if (mcFrustrum.isBoundingBoxInFrustum(aabb)) {
                        ch.addShadowReceiver(aabb);
                    }
                }
                for (val tileEntity : tileEntities) {
                    val aabb = tileEntity.getRenderBoundingBox();
                    if (mcFrustrum.isBoundingBoxInFrustum(aabb)) {
                        ch.addShadowReceiver(aabb);
                    }
                }
            } catch (RuntimeException e) {
                log.error("Caught error while doing the shadow culling: ", e);
            }
        }

        ch.end();

        clipRenderersByFrustumShadow(wrs);
        // endregion

        // region Opaque Uhh, things
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glColorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_CULL_FACE);
        mc.getTextureManager()
          .bindTexture(TextureMap.locationBlocksTexture);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GLDebugGroups.RENDER_SHADOW_0_TERRAIN.push();
        {
            graph.moveTo(Node.ShadowChunk0);
            renderGlobal.renderSortedRenderers(0, numWrs, 0, partialTicks);
        }
        GLDebugGroups.RENDER_SHADOW_0_TERRAIN.pop();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glPushMatrix();

        GLDebugGroups.RENDER_SHADOW_0_ENTITIES.push();
        {
            ForgeHooksClient.setRenderPass(0);
            RenderHelper.enableStandardItemLighting();
            renderGlobal.renderEntities(viewEntity, frustrum, partialTicks);
            RenderHelper.disableStandardItemLighting();
        }
        GLDebugGroups.RENDER_SHADOW_0_ENTITIES.pop();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        // endregion

        unlockShader();
        // shadowtex0 -> [includes all geometry]
        // shadowtex1 -> [excludes transparent geometry]
        //
        // So like, we rendered all the OPAQUE stuff so we blit it over
        blitDepth(buffers.shadowDepthTex0, buffers.shadowDepthTex1);
        // Needed as blit will drop the FB binding...
        buffers.shadow.bind();
        lockShader();

        // region Render Translucent
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        mc.getTextureManager()
          .bindTexture(TextureMap.locationBlocksTexture);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GLDebugGroups.RENDER_SHADOW_1_TERRAIN.push();
        {
            graph.moveTo(Node.ShadowChunk1);
            renderGlobal.renderSortedRenderers(0, numWrs, 1, partialTicks);
        }
        GLDebugGroups.RENDER_SHADOW_1_TERRAIN.pop();

        GLDebugGroups.RENDER_SHADOW_1_ENTITIES.push();
        {
            RenderHelper.enableStandardItemLighting();
            ForgeHooksClient.setRenderPass(1);
            renderGlobal.renderEntities(viewEntity, frustrum, partialTicks);
            ForgeHooksClient.setRenderPass(-1);
            RenderHelper.disableStandardItemLighting();
        }
        GLDebugGroups.RENDER_SHADOW_1_ENTITIES.pop();

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        // endregion

        graph.moveTo(Node.ShadowLast);

        mc.gameSettings.thirdPersonView = preShadowPassThirdPersonView;

        if (state.shadow.depthMipmapEnabled(0)) {
            genMipmap(buffers.shadowDepthTex0);
        }
        if (state.shadow.depthMipmapEnabled(1)) {
            genMipmap(buffers.shadowDepthTex1);
        }
        if (state.shadow.colorMipmapEnabled(0)) {
            genMipmap(buffers.shadowColorTex0);
        }
        if (state.shadow.colorMipmapEnabled(1)) {
            genMipmap(buffers.shadowColorTex1);
        }

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        // Need to reset this before calling pop attrib!
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glPopAttrib();

        GLDebugGroups.RENDER_SHADOW.pop();

        mc.getTextureManager()
          .bindTexture(TextureMap.locationBlocksTexture);

        for (val wr : wrs) {
            if (wr != null) {
                val wre = (WorldRendererExt) wr;
                wre.swan$restoreFrustum();
            }
        }
    }

    // TODO: [CUSTOM_TEX] Bind the custom textures if applicable
    public static void bindCompositeTextures(Map<CompositeTextureData, Texture2D> textures) {
        for (val entry: textures.entrySet()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + entry.getKey().gpuIndex());
            entry.getValue().bind();
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.depthtex0.gpuIndex());
        buffers.depthTex0.bind();
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.depthtex1.gpuIndex());
        buffers.depthTex1.bind();
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.depthtex2.gpuIndex());
        buffers.depthTex2.bind();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    public static void genMipmaps(ObjectList<Texture2D> mipInputs) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);

            for (val mipInput : mipInputs) {
                mipInput.bind();
                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
                DebugMarker.TEXTURE_MIP_GEN.insert(mipInput.name());
            }
        }
        GL11.glPopAttrib();
    }

    private static void genMipmap(@Nullable Texture2D tex) {
        if (tex == null) {
            return;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        tex.bind();
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        GL11.glPopAttrib();

    }

    public static void blitColors(Int2ObjectMap<Texture2D> src, Int2ObjectMap<Texture2D> dst) {
        if (state == null) {
            Share.log.warn("Tried to blit colors with no state");
            return;
        }
        val count = src.size();
        if (count != dst.size()) {
            throw new AssertionError();
        }

        // TODO [SAMPLER]: Move to a better spot
        {
            GL33.glBindSampler(CompositeTextureData.blitsrc.gpuIndex(), blitSrcSampler);
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.blitsrc.gpuIndex());

        val lastShader = state.manager.current();

        boolean sizeEq = true;
        for (val srcEntry : Int2ObjectMaps.fastIterable(src)) {
            val i = srcEntry.getIntKey();
            val srcTex = srcEntry.getValue();
            val dstTex = dst.get(i);
            if (!Texture2D.sizeEquals(srcTex, dstTex)) {
                sizeEq = false;
                break;
            }
        }
        if (sizeEq) {
            use(state.manager.blit_color_identical);
        } else {
            use(state.manager.blit_color_mismatched);
        }
        buffers.tempColor.bind();

        for (val srcEntry : Int2ObjectMaps.fastIterable(src)) {
            val index = srcEntry.getIntKey();

            // Bind Source Texture
            val srcTex = srcEntry.getValue();
            srcTex.bind();

            // Bind Destination Texture
            val dstTex = dst.get(index);
            if (dstTex == null) {
                throw new AssertionError();
            }
            dstTex.attachToFramebufferColor(GL30.GL_COLOR_ATTACHMENT0);

            // Draw using the regular composite
            ShadersCompositeMesh.drawWithColor();

            if (DebugMarker.isEnabled()) {
                DebugMarker.TEXTURE_COLOR_BLIT.insertFormat("{0} -> {1}", srcTex.name(), dstTex.name());
            }
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        use(lastShader);
    }

    public static void blitDepth(Texture2D srcTex, Texture2D dstTex) {
        if (state == null) {
            Share.log.warn("Tried to blit depth with no state");
            return;
        }

        // TODO [SAMPLER]: Move to a better spot
        {
            GL33.glBindSampler(CompositeTextureData.blitsrc.gpuIndex(), blitSrcSampler);
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.blitsrc.gpuIndex());

        val lastShader = state.manager.current();
        if (Texture2D.sizeEquals(srcTex, dstTex)) {
            use(state.manager.blit_depth_identical);
        } else {
            use(state.manager.blit_depth_mismatched);
        }

        buffers.tempDepth.bind();

        // Bind Source Texture
        srcTex.bind();

        // Bind Destination Texture
        dstTex.attachToFramebufferDepth();

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        // Draw using the regular composite (with depth only)
        ShadersCompositeMesh.drawWithDepth();

        if (DebugMarker.isEnabled()) {
            DebugMarker.TEXTURE_DEPTH_BLIT.insertFormat("{0} -> {1}", srcTex.name(), dstTex.name());
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        use(lastShader);
    }

    public static void clearColorBufs() {
        buffers.tempColor.bind();

        buffers.gDepthTex.attachToFramebufferDepth();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        buffers.depthTex0.attachToFramebufferDepth();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        buffers.depthTex1.attachToFramebufferDepth();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        buffers.depthTex2.attachToFramebufferDepth();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        buffers.gColor.clear(ShaderState.fogColor());
    }

    // region Shader Hooks

    public static void preSkyList() {
        ShaderState.setUpPosition();
        val fogColor = ShaderState.fogColor();
        GL11.glColor3d(fogColor.x(), fogColor.y(), fogColor.z());

        Tessellator tess = Tessellator.instance;
        float farDistance = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16;
        double xzq = farDistance * 0.9238;
        double xzp = farDistance * 0.3826;
        double xzn = -xzp;
        double xzm = -xzq;
        double top = 16f;
        double bot = -ShaderState.camPos()
                                 .y();

        tess.startDrawingQuads();
        // horizon
        tess.addVertex(xzn, bot, xzm);
        tess.addVertex(xzn, top, xzm);
        tess.addVertex(xzm, top, xzn);
        tess.addVertex(xzm, bot, xzn);

        tess.addVertex(xzm, bot, xzn);
        tess.addVertex(xzm, top, xzn);
        tess.addVertex(xzm, top, xzp);
        tess.addVertex(xzm, bot, xzp);

        tess.addVertex(xzm, bot, xzp);
        tess.addVertex(xzm, top, xzp);
        tess.addVertex(xzn, top, xzp);
        tess.addVertex(xzn, bot, xzp);

        tess.addVertex(xzn, bot, xzp);
        tess.addVertex(xzn, top, xzp);
        tess.addVertex(xzp, top, xzq);
        tess.addVertex(xzp, bot, xzq);

        tess.addVertex(xzp, bot, xzq);
        tess.addVertex(xzp, top, xzq);
        tess.addVertex(xzq, top, xzp);
        tess.addVertex(xzq, bot, xzp);

        tess.addVertex(xzq, bot, xzp);
        tess.addVertex(xzq, top, xzp);
        tess.addVertex(xzq, top, xzn);
        tess.addVertex(xzq, bot, xzn);

        tess.addVertex(xzq, bot, xzn);
        tess.addVertex(xzq, top, xzn);
        tess.addVertex(xzp, top, xzm);
        tess.addVertex(xzp, bot, xzm);

        tess.addVertex(xzp, bot, xzm);
        tess.addVertex(xzp, top, xzm);
        tess.addVertex(xzn, top, xzm);
        tess.addVertex(xzn, bot, xzm);

        tess.draw();

        val skyColor = ShaderState.skyColor();
        GL11.glColor3d(skyColor.x(), skyColor.y(), skyColor.z());
    }

    // TODO: Used for toggling sky basic/textured, as was done in shaders mod

    // TODO: Used for toggling sky basic/textured, as was done in shaders mod
    // endregion

    static int getBlockID(Block block, int meta) {
        val blockID = Block.getIdFromBlock(block);
        //Thread safety
        val _state = state;
        if (_state != null && _state.remapper != null) {
            return _state.remapper.remap(blockID, meta);
        } else {
            return blockID;
        }
    }

    static int getBlockEntityID(TileEntity tileEntity) {
        // TODO: Would we ever need to be NBT-Aware?
        return getBlockID(tileEntity.getBlockType(), tileEntity.getBlockMetadata());
    }

    static int getEntityID(Entity entity) {
        // TODO: Are there any mapping tables for this?
        return EntityList.getEntityID(entity);
    }

    public static void useCompositeShader(CompositeShader shader) {
        use(shader);
    }

    static void pushShader() {
        shaderStack.push(state.manager.current());
    }

    static void popShader() {
        if (shaderStack.isEmpty()) {
            throw new IllegalStateException("Tried to pop empty shader stack");
        }
        state.manager.use(shaderStack.pop());
    }

    static void lockShader() {
        useShaderLocked = true;
    }

    static void unlockShader() {
        useShaderLocked = false;
    }

    static void use(@Nullable ManagedShader shader) {
        if (useShaderLocked) {
            throw new IllegalStateException("Tried to switch shaders while locked!");
        }
        if (state == null) {
            Share.log.warn("Tried to bind shader with no state!");
            return;
        }
        if (!state.manager.use(shader)) {
            return;
        }
        shaderSwitches++;
        if (shader != null) {
            boolean resetTexture = false;
            val isGBuffer = shader instanceof GBufferShader;
            val isShadow = shader instanceof ShadowShader;
            if (isGBuffer) {
                if (buffers.shadowColorTex0 != null) {
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.shadowcolor0.gpuIndex());
                    resetTexture = true;
                    buffers.shadowColorTex0.bind();
                }
                if (buffers.shadowColorTex1 != null) {
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.shadowcolor1.gpuIndex());
                    resetTexture = true;
                    buffers.shadowColorTex1.bind();
                }
                if (buffers.shadowDepthTex0 != null) {
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.shadowtex0.gpuIndex());
                    resetTexture = true;
                    buffers.shadowDepthTex0.bind();
                }
                if (buffers.shadowDepthTex1 != null) {
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + CompositeTextureData.shadowtex1.gpuIndex());
                    resetTexture = true;
                    buffers.shadowDepthTex1.bind();
                }
            }
            if (isGBuffer || isShadow) {
                for (val entry: gbuffersCustomTex.entrySet()) {
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + entry.getKey().gpuIndex());
                    resetTexture = true;
                    entry.getValue().bind();
                }
            }
            if (resetTexture) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
            }
        }
    }
}
