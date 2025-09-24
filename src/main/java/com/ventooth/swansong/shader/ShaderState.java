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

import com.falsepattern.lib.util.MathUtil;
import com.ventooth.swansong.config.ShadersConfig;
import com.ventooth.swansong.shader.loader.ShaderLoaderOutParams;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4d;
import org.joml.Vector4dc;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;

import java.nio.DoubleBuffer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderState {
    private static final DoubleBuffer tempDoubleBuf = BufferUtils.createDoubleBuffer(16);

    private static final double LOG_HALF = Math.log(0.5);

    private static final Vector4dc sunPosModelView = new Vector4d(0, 100, 0, 0);
    private static final Vector4dc upPosModelView = new Vector4d(0, 100, 0, 0);
    private static final Vector4dc moonPosModelView = new Vector4d(0, -100, 0, 0);

    private static final Matrix4d tempMat4d = new Matrix4d();
    private static final Vector4d tempVec4d = new Vector4d();

    @Nullable
    private static Runnable uniformUpdateTask = null;

    // region State
    private static float subTick = 0F;

    private static long currentTimeMs = 0L;
    private static long lastFrameTimeMs = 0L;
    private static long diffSystemTime = 0L;

    private static double frameTime = 0;
    private static double frameTimeCounter = 0;

    private static int worldTime = 0;
    private static int worldDay = 0;
    private static int moonPhase = 0;
    private static int frameCounter = 0;

    private static int isEyeInWater = 0;
    private static double rainStrength = 0;

    private static double wetness;

    private static double nightVision;
    private static double blindness;

    /**
     * Note that this is the *scaled* view size
     */
    private static final Vector2i viewSize = new Vector2i();

    private static final Vector2i eyeBrightness = new Vector2i();
    private static final Vector2d eyeBrightnessSmooth = new Vector2d();
    private static final Vector2i eyeBrightnessSmoothRound = new Vector2i();

    private static final Vector3d camPos = new Vector3d();
    private static final Vector3d camPosFract = new Vector3d();
    private static final Vector3i camPosInt = new Vector3i();
    private static final Vector3d prevCamPos = new Vector3d();
    private static final Vector3d prevCamPosFract = new Vector3d();
    private static final Vector3i prevCamPosInt = new Vector3i();

    private static final Vector3d upPos = new Vector3d();

    private static final Matrix4d projectionMat = new Matrix4d();
    private static final Matrix4d prevProjectionMat = new Matrix4d();
    private static final Matrix4d projectionMatInv = new Matrix4d();

    private static final Matrix4d modelViewMat = new Matrix4d();
    private static final Matrix4d prevModelViewMat = new Matrix4d();
    private static final Matrix4d modelViewMatInv = new Matrix4d();

    private static final Matrix4d shadowProjectionMat = new Matrix4d();
    private static final Matrix4d shadowProjectionMatInv = new Matrix4d();

    private static final Matrix4d shadowModelViewMat = new Matrix4d();
    private static final Matrix4d shadowModelViewMatInv = new Matrix4d();

    private static double celestialAngle = 0;
    private static double sunAngle = 0;
    private static double shadowAngle = 0;
    private static boolean isShadowMoon = false;

    private static double sunPathRotation = 30;

    private static final Vector3d sunPosition = new Vector3d();
    private static final Vector3d moonPosition = new Vector3d();
    private static final Vector3d shadowLightPosition = new Vector3d();

    // TODO: Wire fog state
    private static final boolean fogEnabled = false;
    private static int fogMode = GL11.GL_EXP;
    private static final Vector3d fogColor = new Vector3d();
    private static final Vector3d skyColor = new Vector3d();

    private static final Vector4d entityColor = new Vector4d();

    private static final boolean[] portalEye = new boolean[4];

    private static @Nullable ItemStack heldItem = null;
    private static boolean isHeldItemTranslucent = false;

    private static int biome = 0;

    private static double wetnessHalfLife = 600D;
    private static double drynessHalfLife = 200D;
    private static double eyeBrightnessHalfLife = 10D;
    private static double centerDepthHalfLife = 1.0;


    private static double centerDepth = 0;
    private static double centerDepthSmooth = 0;

    private static boolean useOldBlockLight = true;

    private static int blockEntityId = 0;
    private static AbstractIntList blockEntityIdStack = new IntArrayList();
    private static int entityId = 0;

    private static int renderStage = 0;
    private static AbstractIntList renderStageStack = new IntArrayList();
    // endregion

    public static void applyParams(ShaderLoaderOutParams params) {
        sunPathRotation = params.sunPathRotation;
        wetnessHalfLife = params.wetnessHalfLife;
        drynessHalfLife = params.drynessHalfLife;
        eyeBrightnessHalfLife = params.eyeBrightnessHalfLife;
        centerDepthHalfLife = params.centerDepthHalfLife;

        if (params.oldLighting == null) {
            useOldBlockLight = true;
        } else {
            useOldBlockLight = params.oldLighting;
        }
    }

    // region Getters
    public static double sunAngle() {
        return sunAngle;
    }

    public static double shadowAngle() {
        return shadowAngle;
    }

    public static int biome() {
        return biome;
    }

    public static float blockLightLevel(float old) {
        if (useOldBlockLight) {
            return old;
        } else {
            return 1;
        }
    }

    public static float blockAoLight() {
        return 0.2f;  //TODO: Note when wiring this up, the config value is not 1:1 with what the block gets. See OptiFine or Angelica as ref.
    }

    public static Vector3dc shadowLightPosition() {
        return shadowLightPosition;
    }

    public static Vector3dc sunPosition() {
        return sunPosition;
    }

    public static Vector3dc moonPosition() {
        return moonPosition;
    }

    public static Matrix4dc shadowProjection() {
        return shadowProjectionMat;
    }

    public static Matrix4dc shadowProjectionInverse() {
        return shadowProjectionMatInv;
    }

    public static Matrix4dc shadowModelView() {
        return shadowModelViewMat;
    }

    public static Matrix4dc shadowModelViewInverse() {
        return shadowModelViewMatInv;
    }

    public static int isEyeInWater() {
        return isEyeInWater;
    }

    public static double rainStrength() {
        return rainStrength;
    }

    public static double wetness() {
        return wetness;
    }

    public static double nightVision() {
        return nightVision;
    }

    public static double blindness() {
        return blindness;
    }

    public static Vector2ic eyeBrightness() {
        return eyeBrightness;
    }

    public static Vector2ic eyeBrightnessSmooth() {
        return eyeBrightnessSmoothRound;
    }

    public static int worldTime() {
        return worldTime;
    }

    public static int worldDay() {
        return worldDay;
    }

    public static int moonPhase() {
        return moonPhase;
    }

    public static int frameCounter() {
        return frameCounter;
    }

    public static Vector2ic viewSize() {
        return viewSize;
    }

    public static double viewWidth() {
        return viewSize.x;
    }

    public static double viewHeight() {
        return viewSize.y;
    }

    public static double aspectRatio() {
        return viewWidth() / viewHeight();
    }

    public static Vector3dc camPos() {
        return camPos;
    }

    public static Vector3dc camPosFract() {
        return camPosFract;
    }

    public static Vector3ic camPosInt() {
        return camPosInt;
    }

    public static Vector3dc prevCamPos() {
        return prevCamPos;
    }

    public static Vector3dc prevCamPosFract() {
        return prevCamPosFract;
    }

    public static Vector3ic prevCamPosInt() {
        return prevCamPosInt;
    }

    public static Vector3dc upPos() {
        return upPos;
    }

    public static double eyeAltitude() {
        return camPos.y;
    }

    public static Matrix4dc projectionMat() {
        return projectionMat;
    }

    public static Matrix4dc prevProjectionMat() {
        return prevProjectionMat;
    }

    public static Matrix4dc projectionMatInv() {
        return projectionMatInv;
    }

    public static Matrix4dc modelViewMat() {
        return modelViewMat;
    }

    public static Matrix4dc prevModelViewMat() {
        return prevModelViewMat;
    }

    public static Matrix4dc modelViewMatInv() {
        return modelViewMatInv;
    }

    public static int fogMode() {
        return fogEnabled ? fogMode : 0;
    }

    public static Vector3dc fogColor() {
        return fogColor;
    }

    public static Vector3dc skyColor() {
        return skyColor;
    }

    public static Vector4dc entityColor() {
        return entityColor;
    }

    public static boolean[] portalEye() {
        return portalEye;
    }

    public static double nearPlane() {
        return 0.05;
    }

    public static double farPlane() {
        return mc().gameSettings.renderDistanceChunks * 16;
    }

    public static boolean isGuiHidden() {
        return mc().gameSettings.hideGUI;
    }

    public static double screenBrightness() {
        return mc().gameSettings.gammaSetting;
    }

    public static double frameTimeCounter() {
        return frameTimeCounter;
    }

    public static double frameTime() {
        return frameTime;
    }

    public static double centerDepthSmooth() {
        return centerDepthSmooth;
    }

    public static int blockEntityId() {
        return blockEntityId;
    }

    public static int entityId() {
        return entityId;
    }

    public static int renderStage() {
        return renderStage;
    }

    // endregion

    public static void updateSubTick(float subTick) {
        ShaderState.subTick = subTick;
    }

    public static float getSubTick() {
        return subTick;
    }

    public static void updateRenderStage(MCRenderStage stage) {
        int newStage = stage.value();
        if (newStage != renderStage) {
            renderStage = newStage;
            updateUniforms();
        }
    }

    public static void pushRenderStage() {
        if (renderStageStack.size() >= 32) {
            throw new IllegalStateException("Render stage stack overflow!");
        }
        renderStageStack.push(renderStage);
    }

    public static void popRenderStage() {
        if (renderStageStack.isEmpty()) {
            throw new IllegalStateException("Tried to pop empty render stage stack!");
        }
        int newStage = renderStageStack.popInt();
        if (newStage != renderStage) {
            renderStage = newStage;
            updateUniforms();
        }
    }

    public static void updateCenterDepth(double currentCenterDepth) {
        centerDepth = currentCenterDepth;
        double temp1 = (double) diffSystemTime * 0.01;
        double temp2 = Math.exp(LOG_HALF * temp1 / centerDepthHalfLife);
        centerDepthSmooth = centerDepth + (centerDepthSmooth - centerDepth) * temp2;
    }

    public static boolean isHeldItemTranslucent() {
        return isHeldItemTranslucent;
    }

    public static void nextBlockEntity(TileEntity tileEntity) {
        int newId = ShaderEngine.getBlockEntityID(tileEntity);
        if (newId != blockEntityId) {
            blockEntityId = newId;
            updateUniforms();
        }
    }

    public static void pushBlockEntity() {
        if (blockEntityIdStack.size() >= 32) {
            throw new IllegalStateException("Block entity ID stack overflow!");
        }
        blockEntityIdStack.push(blockEntityId);
    }

    public static void popBlockEntity() {
        if (blockEntityIdStack.isEmpty()) {
            throw new IllegalStateException("Tried to pop empty block entity ID stack");
        }
        int newId = blockEntityIdStack.popInt();
        if (newId != blockEntityId) {
            blockEntityId = newId;
            updateUniforms();
        }
    }

    public static void portal() {
        blockEntityId = ShaderEngine.getBlockID(Blocks.end_portal, 0);
        updateUniforms();
    }

    public static void nextEntity(Entity entity) {
        entityId = ShaderEngine.getEntityID(entity);
        updateUniforms();
    }

    public static void setHeldItem(@Nullable ItemStack itemStack) {
        heldItem = itemStack;
        isHeldItemTranslucent = false;

        if (heldItem == null) {
            return;
        }

        val item = itemStack.getItem();
        if (!(item instanceof ItemBlock itemBlock)) {
            return;
        }

        val block = itemBlock.field_150939_a;
        if (block == null) {
            return;
        }

        isHeldItemTranslucent = block.getRenderBlockPass() != 0;
    }

    public static boolean updateViewSize() {
        val q = ShadersConfig.RenderQuality.get();
        val width = (int) (mc().displayWidth * q);
        val height = (int) (mc().displayHeight * q);

        if (width == viewSize.x && height == viewSize.y) {
            return false;
        }

        viewSize.set(width, height);
        return true;
    }

    public static void updatePreRenderWorld() {
        val mc = mc();
        val world = mc.theWorld;
        val partialTick = ShaderState.getSubTick();
        val viewEntity = mc.renderViewEntity;
        @Nullable val playerEntity = mc.thePlayer;

        isEyeInWater = 0;
        if (mc.gameSettings.thirdPersonView == 0 && !playerEntity.isPlayerSleeping()) {
            if (viewEntity.isInsideOfMaterial(Material.water)) {
                isEyeInWater = 1;
            } else if (viewEntity.isInsideOfMaterial(Material.lava)) {
                isEyeInWater = 2;
            }
        }

        nightVision = 0;
        blindness = 0;
        if (playerEntity != null) {
            if (playerEntity.isPotionActive(Potion.nightVision)) {
                nightVision = mc.entityRenderer.getNightVisionBrightness(mc.thePlayer, partialTick);
            }
            if (playerEntity.isPotionActive(Potion.blindness)) {
                val blindnessTicks = playerEntity.getActivePotionEffect(Potion.blindness)
                                                 .getDuration();
                blindness = MathUtil.clamp(blindnessTicks / 20.0, 0, 1);
            }
        }

        updateEyeBrightness(viewEntity, partialTick);

        rainStrength = world.getRainStrength(partialTick);

        val time = world.getWorldTime();
        worldTime = (int) (time % 24000L);
        worldDay = (int) (time % 24000L);
        moonPhase = world.getMoonPhase();

        frameCounter++;
        if (frameCounter >= 720720) {// Legacy value
            frameCounter = 0;
        }

        currentTimeMs = System.currentTimeMillis();
        if (lastFrameTimeMs == 0L) {
            lastFrameTimeMs = currentTimeMs;
        }
        diffSystemTime = currentTimeMs - lastFrameTimeMs;
        lastFrameTimeMs = currentTimeMs;

        frameTime = diffSystemTime / 1000.0;
        frameTimeCounter += frameTime;
        frameTimeCounter %= 3600.0;
        val fadeScalar = diffSystemTime * 0.01;
        val temp1 = Math.exp(LOG_HALF * fadeScalar / (wetness < rainStrength ? drynessHalfLife : wetnessHalfLife));
        wetness = wetness * temp1 + rainStrength * (1.0 - temp1);

        prevCamPos.set(camPos);
        prevCamPosInt.set(camPosInt);
        prevCamPosFract.set(camPosFract);

        prevProjectionMat.set(projectionMat);
        prevModelViewMat.set(modelViewMat);

        val skyColor = mc().theWorld.getSkyColor(viewEntity, ShaderState.getSubTick());
        updateSkyColor(skyColor.xCoord, skyColor.yCoord, skyColor.zCoord);

        biome = world.getBiomeGenForCoords(camPosInt.x, camPosInt.z).biomeID;

        entityId = -1;
        blockEntityId = -1;
    }

    private static void eyeBrightnessFromRaw(int brightness) {
        eyeBrightness.x = brightness & 0xFFFF;
        eyeBrightness.y = (brightness >>> 16) & 0xFFFF;
    }

    private static void updateEyeBrightness(Entity viewEntity, float partialTick) {
        eyeBrightnessFromRaw(viewEntity.getBrightnessForRender(partialTick));
        double temp1 = (double) diffSystemTime * 0.01;
        double temp2 = Math.exp(LOG_HALF * temp1 / eyeBrightnessHalfLife);
        eyeBrightnessSmooth.x = eyeBrightness.x + (eyeBrightnessSmooth.x - eyeBrightness.x) * temp2;
        eyeBrightnessSmooth.y = eyeBrightness.y + (eyeBrightnessSmooth.y - eyeBrightness.y) * temp2;
        eyeBrightnessSmoothRound.set((int) Math.round(eyeBrightnessSmooth.x), (int) Math.round(eyeBrightnessSmooth.y));
    }

    public static void setCameraShadow(int size, double dist, Double fov, double intervalStep) {
        updateCamera(false);

        GL11.glViewport(0, 0, size, size);
        GL11.glMatrixMode(GL11.GL_PROJECTION);

        {
            val near = 0.05;
            val far = 256.0;
            if (fov == null) {
                GL11.glLoadIdentity();
                GL11.glOrtho(-dist, dist, -dist, dist, near, far);
            } else {
                tempMat4d.setPerspective(Math.toRadians(fov), 1, near, far);
                tempDoubleBuf.clear();
                tempMat4d.get(tempDoubleBuf);
                GL11.glLoadMatrix(tempDoubleBuf);
            }
        }

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslated(0.0, 0.0, -100);
        GL11.glRotated(90, 1, 0, 0);

        var angle = celestialAngle * -360;
        if (isShadowMoon) {
            angle += 180D;
        }

        GL11.glRotated(angle, 0D, 0D, 1D);
        GL11.glRotated(sunPathRotation, 1D, 0D, 0D);

        if (fov == null) {
            val mod = intervalStep - (intervalStep / 2.0);
            GL11.glTranslated(camPos.x % mod, camPos.y % mod, camPos.z % mod);
        }

        // We clear this just to be sure...
        tempDoubleBuf.clear();

        // Yoink Projection Matrix
        GL11.glGetDouble(GL11.GL_PROJECTION_MATRIX, tempDoubleBuf);
        shadowProjectionMat.set(tempDoubleBuf)
                           .invert(shadowProjectionMatInv);

        // Yoink Model View Matrix
        GL11.glGetDouble(GL11.GL_MODELVIEW_MATRIX, tempDoubleBuf);
        shadowModelViewMat.set(tempDoubleBuf)
                          .invert(shadowModelViewMatInv);

        updateUniforms();
    }

    public static void preCelestialRotate() {
        GL11.glRotated(sunPathRotation, 0, 0, 1);
    }

    public static void postCelestialRotate() {
        tempDoubleBuf.clear();
        GL11.glGetDouble(GL11.GL_MODELVIEW_MATRIX, tempDoubleBuf);
        tempMat4d.set(tempDoubleBuf);

        sunPosition.set(sunPosModelView.mul(tempMat4d, tempVec4d));
        moonPosition.set(moonPosModelView.mul(tempMat4d, tempVec4d));

        if (shadowAngle == sunAngle) {
            shadowLightPosition.set(sunPosition);
        } else {
            shadowLightPosition.set(moonPosition);
        }

        updateUniforms();
    }

    public static void updateCamera(boolean withUpdate) {
        val viewEntity = mc().renderViewEntity;
        val partialTick = ShaderState.getSubTick();

        // Stuff is needed-needed, otherwise stuff will jitter to hell and back
        camPos.x = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTick;
        camPos.y = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTick;
        camPos.z = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTick;
        camPos.floor(camPosFract);
        camPosInt.set(camPosFract);
        camPos.sub(camPosFract, camPosFract);

        // We clear this just to be sure...
        tempDoubleBuf.clear();

        // Yoink Projection Matrix
        GL11.glGetDouble(GL11.GL_PROJECTION_MATRIX, tempDoubleBuf);
        projectionMat.set(tempDoubleBuf)
                     .invert(projectionMatInv);

        // Yoink Model View Matrix
        GL11.glGetDouble(GL11.GL_MODELVIEW_MATRIX, tempDoubleBuf);
        modelViewMat.set(tempDoubleBuf)
                    .invert(modelViewMatInv);

        if (withUpdate) {
            updateUniforms();
        }
    }

    public static void updateCelestialAngle() {
        celestialAngle = mc().theWorld.getCelestialAngle(ShaderState.getSubTick());
        sunAngle = celestialAngle < 0.75 ? celestialAngle + 0.25 : celestialAngle - 0.75;

        if (sunAngle <= 0.5D) {
            shadowAngle = sunAngle;
            isShadowMoon = false;
        } else {
            shadowAngle = sunAngle - 0.5D;
            isShadowMoon = true;
        }
    }

    public static void setUpPosition() {
        GL11.glGetDouble(GL11.GL_MODELVIEW_MATRIX, tempDoubleBuf);
        tempMat4d.set(tempDoubleBuf);
        upPos.set(upPosModelView.mul(tempMat4d, tempVec4d));
        updateUniforms();
    }

    public static void updateFogColor(double r, double g, double b) {
        fogColor.set(r, g, b);
        updateUniforms();
    }

    public static void updateSkyColor(double r, double g, double b) {
        skyColor.set(r, g, b);
        updateUniforms();
    }

    public static void updateFogMode(int mode) {
        if (mode == GL11.GL_LINEAR || mode == GL11.GL_EXP || mode == GL11.GL_EXP2) {
            fogMode = mode;
        } else {
            throw new AssertionError();
        }
        updateUniforms();
    }

    public static void updateEntityColor(double r, double g, double b, double a) {
        entityColor.set(r, g, b, a);
        updateUniforms();
    }

    public static void updatePortalEyeState(boolean s, boolean t, boolean r, boolean q) {
        portalEye[0] = s;
        portalEye[1] = t;
        portalEye[2] = r;
        portalEye[3] = q;
        updateUniforms();
    }

    public static void setUniformUpdateTask(Runnable task) {
        if (uniformUpdateTask == null) {
            uniformUpdateTask = task;
        } else {
            throw new AssertionError();
        }
    }

    public static int heldBlockLightValue() {
        val itemId = heldItemId();
        if (itemId == -1) {
            return 0;
        }
        val block = (Block) Block.blockRegistry.getObjectById(itemId);
        return block != null ? block.getLightValue() : 0;
    }

    public static int heldItemId() {
        val stack = heldItem();
        val item = stack != null ? stack.getItem() : null;
        int itemId = -1;
        if (item != null) {
            itemId = Item.itemRegistry.getIDForObject(item);
        }
        return itemId;
    }

    private static ItemStack heldItem() {
        val mc = mc();
        val plr = mc.thePlayer;
        val stack = plr != null ? plr.getHeldItem() : null;
        return stack;
    }

    private static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    private static void updateUniforms() {
        if (uniformUpdateTask != null) {
            uniformUpdateTask.run();
        } else {
            throw new AssertionError();
        }
    }
}
