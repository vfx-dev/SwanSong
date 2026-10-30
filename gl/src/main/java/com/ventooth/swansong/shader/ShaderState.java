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

import com.ventooth.swansong.gl.LwjglCompat;
import com.ventooth.swansong.util.MathUtils;
import com.ventooth.swansong.shader.loader.ShaderLoaderOutParams;
import com.ventooth.swansong.shader.uniform.GeneralUniforms;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.val;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector2d;
import org.joml.Vector2dc;
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

import java.nio.DoubleBuffer;

public abstract class ShaderState {
    private final DoubleBuffer tempDoubleBuf = BufferUtils.createDoubleBuffer(16);

    private static final double LOG_HALF = Math.log(0.5);

    private static final Vector4dc sunPosModelView = new Vector4d(0, 100, 0, 0);
    private static final Vector4dc upPosModelView = new Vector4d(0, 100, 0, 0);
    private static final Vector4dc moonPosModelView = new Vector4d(0, -100, 0, 0);

    private final Matrix4d tempMat4d = new Matrix4d();
    private final Vector4d tempVec4d = new Vector4d();

    private final GeneralUniforms uniforms = new GeneralUniforms(this);

    // region State
    private float subTick = 0F;

    private long currentTimeMs = 0L;
    private long lastFrameTimeMs = 0L;
    private long diffSystemTime = 0L;

    private double frameTime = 0;
    private double frameTimeCounter = 0;

    private int worldTime = 0;
    private int worldDay = 0;
    private int moonPhase = 0;
    private int frameCounter = 0;

    private int isEyeInWater = 0;
    private double rainStrength = 0;

    private double wetness;

    private double nightVision;
    private double blindness;

    /**
     * Note that this is the *scaled* view size
     */
    private final Vector2i viewSize = new Vector2i();

    private final Vector2i eyeBrightness = new Vector2i();
    private final Vector2d eyeBrightnessD = new Vector2d();
    private final Vector2d eyeBrightnessSmooth = new Vector2d();
    private final Vector2i eyeBrightnessSmoothRound = new Vector2i();
    private final Vector2d eyeBrightnessSmoothRoundD = new Vector2d();

    private final Vector3d camPos = new Vector3d();
    private final Vector3d camPosFract = new Vector3d();
    private final Vector3i camPosInt = new Vector3i();
    private final Vector3d camPosIntD = new Vector3d();
    private final Vector3d prevCamPos = new Vector3d();
    private final Vector3d prevCamPosFract = new Vector3d();
    private final Vector3i prevCamPosInt = new Vector3i();
    private final Vector3d prevCamPosIntD = new Vector3d();

    private final Vector3d upPos = new Vector3d();

    private final Matrix4d projectionMat = new Matrix4d();
    private final Matrix4d prevProjectionMat = new Matrix4d();
    private final Matrix4d projectionMatInv = new Matrix4d();

    private final Matrix4d modelViewMat = new Matrix4d();
    private final Matrix4d prevModelViewMat = new Matrix4d();
    private final Matrix4d modelViewMatInv = new Matrix4d();

    private final Matrix4d shadowProjectionMat = new Matrix4d();
    private final Matrix4d shadowProjectionMatInv = new Matrix4d();

    private final Matrix4d shadowModelViewMat = new Matrix4d();
    private final Matrix4d shadowModelViewMatInv = new Matrix4d();

    private double celestialAngle = 0;
    private double sunAngle = 0;
    private double shadowAngle = 0;
    private boolean isShadowMoon = false;

    private double sunPathRotation = 30;

    private final Vector3d sunPosition = new Vector3d();
    private final Vector3d moonPosition = new Vector3d();
    private final Vector3d shadowLightPosition = new Vector3d();

    // TODO: Wire fog state
    private final boolean fogEnabled = false;
    private int fogMode = GL11.GL_EXP;
    private final Vector3d fogColor = new Vector3d();
    private final Vector3d skyColor = new Vector3d();

    private final Vector4d entityColor = new Vector4d(0D);

    private final Vector2i atlasSize = new Vector2i();

    private boolean isHeldItemTranslucent = false;

    private int biome = 0;

    private double wetnessHalfLife = 600D;
    private double drynessHalfLife = 200D;
    private double eyeBrightnessHalfLife = 10D;
    private double centerDepthHalfLife = 1.0;

    private double centerDepth = 0;
    private double centerDepthSmooth = 0;

    private boolean useOldBlockLight = true;

    private int blockEntityId = 0;
    private int entityId = 0;

    private int renderStage = 0;
    private AbstractIntList renderStageStack = new IntArrayList();
    // endregion

    public void applyParams(ShaderLoaderOutParams params) {
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
    public double sunAngle() {
        return sunAngle;
    }

    public double shadowAngle() {
        return shadowAngle;
    }

    public int biome() {
        return biome;
    }

    public float blockLightLevel(float old) {
        if (useOldBlockLight) {
            return old;
        } else {
            return 1;
        }
    }

    public float blockAoLight() {
        return 0.2f;  //TODO: Note when wiring this up, the config value is not 1:1 with what the block gets. See OptiFine or Angelica as ref.
    }

    public Vector3dc shadowLightPosition() {
        return shadowLightPosition;
    }

    public Vector3dc sunPosition() {
        return sunPosition;
    }

    public Vector3dc moonPosition() {
        return moonPosition;
    }

    public Matrix4dc shadowProjection() {
        return shadowProjectionMat;
    }

    public Matrix4dc shadowProjectionInverse() {
        return shadowProjectionMatInv;
    }

    public Matrix4dc shadowModelView() {
        return shadowModelViewMat;
    }

    public Matrix4dc shadowModelViewInverse() {
        return shadowModelViewMatInv;
    }

    public int isEyeInWater() {
        return isEyeInWater;
    }

    public double rainStrength() {
        return rainStrength;
    }

    public double wetness() {
        return wetness;
    }

    public double nightVision() {
        return nightVision;
    }

    public double blindness() {
        return blindness;
    }

    public Vector2ic eyeBrightness() {
        return eyeBrightness;
    }

    public Vector2ic eyeBrightnessSmooth() {
        return eyeBrightnessSmoothRound;
    }

    public Vector2dc eyeBrightnessD() {
        return eyeBrightnessD;
    }

    public Vector2dc eyeBrightnessSmoothD() {
        return eyeBrightnessSmoothRoundD;
    }

    public int worldTime() {
        return worldTime;
    }

    public int worldDay() {
        return worldDay;
    }

    public int moonPhase() {
        return moonPhase;
    }

    public int frameCounter() {
        return frameCounter;
    }

    public Vector2ic viewSize() {
        return viewSize;
    }

    public double viewWidth() {
        return viewSize.x;
    }

    public double viewHeight() {
        return viewSize.y;
    }

    public double aspectRatio() {
        return viewWidth() / viewHeight();
    }

    public Vector3dc camPos() {
        return camPos;
    }

    public Vector3dc camPosFract() {
        return camPosFract;
    }

    public Vector3ic camPosInt() {
        return camPosInt;
    }

    public Vector3dc camPosIntD() {
        return camPosIntD;
    }

    public Vector3dc prevCamPos() {
        return prevCamPos;
    }

    public Vector3dc prevCamPosFract() {
        return prevCamPosFract;
    }

    public Vector3dc prevCamPosIntD() {
        return prevCamPosIntD;
    }

    public Vector3dc upPos() {
        return upPos;
    }

    public double eyeAltitude() {
        return camPos.y;
    }

    public Matrix4dc projectionMat() {
        return projectionMat;
    }

    public Matrix4dc prevProjectionMat() {
        return prevProjectionMat;
    }

    public Matrix4dc projectionMatInv() {
        return projectionMatInv;
    }

    public Matrix4dc modelViewMat() {
        return modelViewMat;
    }

    public Matrix4dc prevModelViewMat() {
        return prevModelViewMat;
    }

    public Matrix4dc modelViewMatInv() {
        return modelViewMatInv;
    }

    public int fogMode() {
        return fogEnabled ? fogMode : 0;
    }

    public Vector3dc fogColor() {
        return fogColor;
    }

    public Vector3dc skyColor() {
        return skyColor;
    }

    public Vector4dc entityColor() {
        return entityColor;
    }

    public double nearPlane() {
        return 0.05;
    }

    public abstract double farPlane();

    public boolean isGuiHidden() {
        return false;
    }

    public abstract double screenBrightness();

    public double frameTimeCounter() {
        return frameTimeCounter;
    }

    public double frameTime() {
        return frameTime;
    }

    public double centerDepthSmooth() {
        return centerDepthSmooth;
    }

    public int blockEntityId() {
        return blockEntityId;
    }

    public int entityId() {
        return entityId;
    }

    public int renderStage() {
        return renderStage;
    }

    public Vector2ic atlasSize() {
        return atlasSize;
    }

    // endregion

    public void updateSubTick(float subTick) {
        this.subTick = subTick;
    }

    public float getSubTick() {
        return subTick;
    }

    public void updateAtlasSize(int width, int height) {
        if (atlasSize.x != width || atlasSize.y != height) {
            atlasSize.set(width, height);
            updateUniforms();
        }
    }

    public void updateRenderStage(MCRenderStage stage) {
        int newStage = stage.value();
        if (newStage != renderStage) {
            renderStage = newStage;
            updateUniforms();
        }
    }

    public void pushRenderStage() {
        if (renderStageStack.size() >= 32) {
            throw new IllegalStateException("Render stage stack overflow!");
        }
        renderStageStack.push(renderStage);
    }

    public void popRenderStage() {
        if (renderStageStack.isEmpty()) {
            throw new IllegalStateException("Tried to pop empty render stage stack!");
        }
        int newStage = renderStageStack.popInt();
        if (newStage != renderStage) {
            renderStage = newStage;
            updateUniforms();
        }
    }

    public void updateCenterDepth(double currentCenterDepth) {
        centerDepth = currentCenterDepth;
        double temp1 = (double) diffSystemTime * 0.01;
        double temp2 = Math.exp(LOG_HALF * temp1 / centerDepthHalfLife);
        centerDepthSmooth = centerDepth + (centerDepthSmooth - centerDepth) * temp2;
    }

    public boolean isHeldItemTranslucent() {
        return isHeldItemTranslucent;
    }

    public void nextBlockEntity(int newId) {
        if (newId != blockEntityId) {
            blockEntityId = newId;
            updateUniforms();
        }
    }

    public void nextEntity(int newEntityId) {
        entityId = newEntityId;
        resetEntityColor();
        // resetEntityColor() implicitly calls updateUniforms();
    }

    public void setHeldItemTranslucent(boolean translucent) {
        isHeldItemTranslucent = translucent;
    }

    public boolean updateViewSize(double q) {
        val width = (int) (displayWidth() * q);
        val height = (int) (displayHeight() * q);

        if (width == viewSize.x && height == viewSize.y) {
            return false;
        }

        viewSize.set(width, height);
        return true;
    }

    public void updatePreRenderWorld() {
        val sample = sampleWorld();

        isEyeInWater = sample.eyeInWater();
        nightVision = sample.nightVision();
        blindness = MathUtils.clamp(sample.blindnessTicks() / 20.0, 0, 1);

        updateEyeBrightness(sample.rawEyeBrightness());

        rainStrength = sample.rainStrength();

        worldTime = (int) (sample.worldTime() % 24000L);
        worldDay = (int) (sample.totalWorldTime() / 24000L);
        moonPhase = sample.moonPhase();

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
        prevCamPosIntD.set(prevCamPosInt);
        prevCamPosFract.set(camPosFract);

        prevProjectionMat.set(projectionMat);
        prevModelViewMat.set(modelViewMat);

        updateSkyColor(sample.skyR(), sample.skyG(), sample.skyB());

        biome = sample.biomeId();

        entityId = -1;
        blockEntityId = -1;
    }

    private void eyeBrightnessFromRaw(int brightness) {
        eyeBrightness.x = brightness & 0xFFFF;
        eyeBrightness.y = (brightness >>> 16) & 0xFFFF;
        eyeBrightnessD.set(eyeBrightness);
    }

    private void updateEyeBrightness(int rawBrightness) {
        eyeBrightnessFromRaw(rawBrightness);
        double temp1 = (double) diffSystemTime * 0.01;
        double temp2 = Math.exp(LOG_HALF * temp1 / eyeBrightnessHalfLife);
        eyeBrightnessSmooth.x = eyeBrightness.x + (eyeBrightnessSmooth.x - eyeBrightness.x) * temp2;
        eyeBrightnessSmooth.y = eyeBrightness.y + (eyeBrightnessSmooth.y - eyeBrightness.y) * temp2;
        eyeBrightnessSmoothRound.set((int) Math.round(eyeBrightnessSmooth.x), (int) Math.round(eyeBrightnessSmooth.y));
        eyeBrightnessSmoothRoundD.set(eyeBrightnessSmoothRound);
    }

    public void setCameraShadow(int size, double dist, Double fov, double intervalStep) {
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
                LwjglCompat.glLoadMatrix(tempDoubleBuf);
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
        LwjglCompat.glGetDouble(GL11.GL_PROJECTION_MATRIX, tempDoubleBuf);
        shadowProjectionMat.set(tempDoubleBuf)
                           .invert(shadowProjectionMatInv);

        // Yoink Model View Matrix
        LwjglCompat.glGetDouble(GL11.GL_MODELVIEW_MATRIX, tempDoubleBuf);
        shadowModelViewMat.set(tempDoubleBuf)
                          .invert(shadowModelViewMatInv);

        updateUniforms();
    }

    public void preCelestialRotate() {
        GL11.glRotated(sunPathRotation, 0, 0, 1);
    }

    public void postCelestialRotate() {
        tempDoubleBuf.clear();
        LwjglCompat.glGetDouble(GL11.GL_MODELVIEW_MATRIX, tempDoubleBuf);
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

    public void updateCamera(boolean withUpdate) {
        // Stuff is needed-needed, otherwise stuff will jitter to hell and back
        camPos.x = cameraX();
        camPos.y = cameraY();
        camPos.z = cameraZ();
        camPos.floor(camPosFract);
        camPosInt.set(camPosFract);
        camPosIntD.set(camPosInt);
        camPos.sub(camPosFract, camPosFract);

        // We clear this just to be sure...
        tempDoubleBuf.clear();

        // Yoink Projection Matrix
        LwjglCompat.glGetDouble(GL11.GL_PROJECTION_MATRIX, tempDoubleBuf);
        projectionMat.set(tempDoubleBuf)
                     .invert(projectionMatInv);

        // Yoink Model View Matrix
        LwjglCompat.glGetDouble(GL11.GL_MODELVIEW_MATRIX, tempDoubleBuf);
        modelViewMat.set(tempDoubleBuf)
                    .invert(modelViewMatInv);

        if (withUpdate) {
            updateUniforms();
        }
    }

    public void updateCelestialAngle() {
        celestialAngle = celestialAngle();
        sunAngle = celestialAngle < 0.75 ? celestialAngle + 0.25 : celestialAngle - 0.75;

        if (sunAngle <= 0.5D) {
            shadowAngle = sunAngle;
            isShadowMoon = false;
        } else {
            shadowAngle = sunAngle - 0.5D;
            isShadowMoon = true;
        }
    }

    public void setUpPosition() {
        LwjglCompat.glGetDouble(GL11.GL_MODELVIEW_MATRIX, tempDoubleBuf);
        tempMat4d.set(tempDoubleBuf);
        upPos.set(upPosModelView.mul(tempMat4d, tempVec4d));
        updateUniforms();
    }

    public void updateFogColor(double r, double g, double b) {
        fogColor.set(r, g, b);
        updateUniforms();
    }

    public void updateSkyColor(double r, double g, double b) {
        skyColor.set(r, g, b);
        updateUniforms();
    }

    public void updateFogMode(int mode) {
        if (mode == GL11.GL_LINEAR || mode == GL11.GL_EXP || mode == GL11.GL_EXP2) {
            fogMode = mode;
        } else {
            throw new AssertionError();
        }
        updateUniforms();
    }

    public void resetEntityColor() {
        updateEntityColor(0D, 0D, 0D, 0D);
    }

    /**
     * Sets the entity color uniform, where the {@code mixFactor} is treated as a weight, see example GLSL:
     * <p>
     * <code>
     * color.rgb = mix(color.rgb, entityColor.rgb, entityColor.a);
     * </code>
     *
     * @param r         Red
     * @param g         Green
     * @param b         Blue
     * @param mixFactor Mix Factor
     *
     * @see <a href="https://shaders.properties/current/reference/uniforms/rendering/#entitycolor">https://shaders.properties/current/reference/uniforms/rendering/#entitycolor</a>
     */
    public void updateEntityColor(double r, double g, double b, double mixFactor) {
        entityColor.set(r, g, b, mixFactor);
        updateUniforms();
    }

    public GeneralUniforms uniforms() {
        return uniforms;
    }

    public int heldBlockLightValue() {
        return 0;
    }

    public int heldItemId() {
        return -1;
    }

    public abstract double cameraX();

    public abstract double cameraY();

    public abstract double cameraZ();

    public abstract double celestialAngle();

    public abstract int displayWidth();

    public abstract int displayHeight();

    public WorldSample sampleWorld() {
        return WorldSample.EMPTY;
    }

    private void updateUniforms() {
        uniforms.updateLive();
    }
}
