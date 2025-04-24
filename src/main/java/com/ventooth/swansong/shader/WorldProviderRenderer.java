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

import com.ventooth.swansong.shader.StateGraph.Node;
import lombok.AllArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.IRenderHandler;

import static com.ventooth.swansong.shader.ShaderEngine.graph;
import static com.ventooth.swansong.shader.ShaderEngine.preSkyList;

public final class WorldProviderRenderer {
    private static final IRenderHandler SKY_RENDER_HANDLER = new DynRenderHandler(WorldProviderRenderer::renderSky);

    public static IRenderHandler wrapSkyRenderer(@NotNull WorldProvider worldProvider,
                                                 @Nullable IRenderHandler oldRenderer) {
        if (oldRenderer == null) {
            return SKY_RENDER_HANDLER;
        }
        return oldRenderer;
    }

    private static void renderSky(float partialTickTime, WorldClient world, Minecraft mc) {
        if (world.provider.dimensionId == 1) {
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            GL11.glDepthMask(false);

            graph.moveTo(Node.RenderSkyTextured);
            mc.renderEngine.bindTexture(RenderGlobal.locationEndSkyPng);
            Tessellator tessellator = Tessellator.instance;

            for (int i = 0; i < 6; ++i) {
                GL11.glPushMatrix();

                if (i == 1) {
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 2) {
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 3) {
                    GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 4) {
                    GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                }

                if (i == 5) {
                    GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
                }

                tessellator.startDrawingQuads();
                tessellator.setColorOpaque_I(2631720);
                tessellator.addVertexWithUV(-100.0D, -100.0D, -100.0D, 0.0D, 0.0D);
                tessellator.addVertexWithUV(-100.0D, -100.0D, 100.0D, 0.0D, 16.0D);
                tessellator.addVertexWithUV(100.0D, -100.0D, 100.0D, 16.0D, 16.0D);
                tessellator.addVertexWithUV(100.0D, -100.0D, -100.0D, 16.0D, 0.0D);
                tessellator.draw();
                GL11.glPopMatrix();
            }

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
        } else if (world.provider.isSurfaceWorld()) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            graph.moveTo(Node.RenderSkyBasic);
            Vec3 vec3 = world.getSkyColor(mc.renderViewEntity, partialTickTime);
            float f1 = (float) vec3.xCoord;
            float f2 = (float) vec3.yCoord;
            float f3 = (float) vec3.zCoord;
            float f6;

            if (mc.gameSettings.anaglyph) {
                float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
                float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
                f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
                f1 = f4;
                f2 = f5;
                f3 = f6;
            }

            GL11.glColor3f(f1, f2, f3);
            val tess = Tessellator.instance;
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_FOG);
            GL11.glColor3f(f1, f2, f3);
            preSkyList();
            GL11.glCallList(mc.renderGlobal.glSkyList);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTickTime),
                                                                    partialTickTime);
            float f7;
            float f8;
            float f9;
            float f10;

            if (afloat != null) {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                graph.moveTo(Node.RenderSkyBasic);
                GL11.glShadeModel(GL11.GL_SMOOTH);
                GL11.glPushMatrix();
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(MathHelper.sin(world.getCelestialAngleRadians(partialTickTime)) < 0.0F ? 180.0F : 0.0F,
                               0.0F,
                               0.0F,
                               1.0F);
                GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
                f6 = afloat[0];
                f7 = afloat[1];
                f8 = afloat[2];
                float f11;

                if (mc.gameSettings.anaglyph) {
                    f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
                    f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
                    f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
                    f6 = f9;
                    f7 = f10;
                    f8 = f11;
                }

                tess.startDrawing(6);
                tess.setColorRGBA_F(f6, f7, f8, afloat[3]);
                tess.addVertex(0.0D, 100.0D, 0.0D);
                byte b0 = 16;
                tess.setColorRGBA_F(afloat[0], afloat[1], afloat[2], 0.0F);

                for (int j = 0; j <= b0; ++j) {
                    f11 = (float) j * (float) Math.PI * 2.0F / (float) b0;
                    float f12 = MathHelper.sin(f11);
                    float f13 = MathHelper.cos(f11);
                    tess.addVertex(f12 * 120.0F, f13 * 120.0F, -f13 * 40.0F * afloat[3]);
                }

                tess.draw();
                GL11.glPopMatrix();
                GL11.glShadeModel(GL11.GL_FLAT);
            }

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            graph.moveTo(Node.RenderSkyTextured);
            OpenGlHelper.glBlendFunc(770, 1, 1, 0);
            GL11.glPushMatrix();
            f6 = 1.0F - world.getRainStrength(partialTickTime);
            f7 = 0.0F;
            f8 = 0.0F;
            f9 = 0.0F;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, f6);
            GL11.glTranslatef(f7, f8, f9);
            GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            ShaderState.preCelestialRotate();
            GL11.glRotatef(world.getCelestialAngle(partialTickTime) * 360.0F, 1.0F, 0.0F, 0.0F);
            ShaderState.postCelestialRotate();
            f10 = 30.0F;
            ShaderState.updateRenderStage(MCRenderStage.SUN);
            mc.renderEngine.bindTexture(RenderGlobal.locationSunPng);
            tess.startDrawingQuads();
            tess.addVertexWithUV(-f10, 100.0D, -f10, 0.0D, 0.0D);
            tess.addVertexWithUV(f10, 100.0D, -f10, 1.0D, 0.0D);
            tess.addVertexWithUV(f10, 100.0D, f10, 1.0D, 1.0D);
            tess.addVertexWithUV(-f10, 100.0D, f10, 0.0D, 1.0D);
            tess.draw();
            f10 = 20.0F;
            ShaderState.updateRenderStage(MCRenderStage.MOON);
            mc.renderEngine.bindTexture(RenderGlobal.locationMoonPhasesPng);
            int k = world.getMoonPhase();
            int l = k % 4;
            int i1 = k / 4 % 2;
            float f14 = (float) (l) / 4.0F;
            float f15 = (float) (i1) / 2.0F;
            float f16 = (float) (l + 1) / 4.0F;
            float f17 = (float) (i1 + 1) / 2.0F;
            tess.startDrawingQuads();
            tess.addVertexWithUV(-f10, -100.0D, f10, f16, f17);
            tess.addVertexWithUV(f10, -100.0D, f10, f14, f17);
            tess.addVertexWithUV(f10, -100.0D, -f10, f14, f15);
            tess.addVertexWithUV(-f10, -100.0D, -f10, f16, f15);
            tess.draw();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            graph.moveTo(Node.RenderSkyBasic);
            float f18 = world.getStarBrightness(partialTickTime) * f6;

            if (f18 > 0.0F) {
                GL11.glColor4f(f18, f18, f18, f18);
                ShaderState.updateRenderStage(MCRenderStage.STARS);
                GL11.glCallList(mc.renderGlobal.starGLCallList);
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_FOG);
            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            graph.moveTo(Node.RenderSkyBasic);
            GL11.glColor3f(0.0F, 0.0F, 0.0F);
            double d0 = mc.thePlayer.getPosition(partialTickTime).yCoord - world.getHorizon();

            if (d0 < 0.0D) {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, 12.0F, 0.0F);
                GL11.glCallList(mc.renderGlobal.glSkyList2);
                GL11.glPopMatrix();
                f8 = 1.0F;
                f9 = -((float) (d0 + 65.0D));
                f10 = -f8;
                tess.startDrawingQuads();
                tess.setColorRGBA_I(0, 255);
                tess.addVertex(-f8, f9, f8);
                tess.addVertex(f8, f9, f8);
                tess.addVertex(f8, f10, f8);
                tess.addVertex(-f8, f10, f8);
                tess.addVertex(-f8, f10, -f8);
                tess.addVertex(f8, f10, -f8);
                tess.addVertex(f8, f9, -f8);
                tess.addVertex(-f8, f9, -f8);
                tess.addVertex(f8, f10, -f8);
                tess.addVertex(f8, f10, f8);
                tess.addVertex(f8, f9, f8);
                tess.addVertex(f8, f9, -f8);
                tess.addVertex(-f8, f9, -f8);
                tess.addVertex(-f8, f9, f8);
                tess.addVertex(-f8, f10, f8);
                tess.addVertex(-f8, f10, -f8);
                tess.addVertex(-f8, f10, -f8);
                tess.addVertex(-f8, f10, f8);
                tess.addVertex(f8, f10, f8);
                tess.addVertex(f8, f10, -f8);
                tess.draw();
            }

            if (world.provider.isSkyColored()) {
                GL11.glColor3f(f1 * 0.2F + 0.04F, f2 * 0.2F + 0.04F, f3 * 0.6F + 0.1F);
            } else {
                GL11.glColor3f(f1, f2, f3);
            }

            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, -((float) (d0 - 16.0D)), 0.0F);
            GL11.glCallList(mc.renderGlobal.glSkyList2);
            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            graph.moveTo(Node.RenderSkyTextured);
            GL11.glDepthMask(true);
        }
    }

    private interface IRenderHandlerFn {
        void render(float subTick, WorldClient world, Minecraft mc);
    }

    @AllArgsConstructor
    private static final class DynRenderHandler extends IRenderHandler {
        private final IRenderHandlerFn fn;

        public void render(float subTick, WorldClient world, Minecraft mc) {
            fn.render(subTick, world, mc);
        }
    }
}
