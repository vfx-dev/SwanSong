/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.FastTextRender;

import com.ventooth.swansong.FastTextRender.CharHelper;
import com.ventooth.swansong.FastTextRender.FastFontRenderer;
import com.ventooth.swansong.FastTextRender.TextTess;
import com.ventooth.swansong.FastTextRender.VertexConsumer;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import java.util.BitSet;
import java.util.Random;

@Mixin(value = FontRenderer.class,
       // Fixes crash with HodgePodge font patches
       // Also fixes crash with LegacyFixes nbsp patch
       priority = 900)
public abstract class FontRendererMixin implements FastFontRenderer {
    @Shadow
    private boolean bidiFlag;

    @Shadow
    protected abstract String bidiReorder(String text);

    @Shadow
    private float red;

    @Shadow
    private float blue;

    @Shadow
    private float green;

    @Shadow
    private float alpha;

    @Shadow
    protected float posX;

    @Shadow
    protected float posY;

    @Shadow
    private boolean italicStyle;

    @Shadow
    private boolean randomStyle;

    @Shadow
    private boolean boldStyle;

    @Shadow
    private boolean strikethroughStyle;

    @Shadow
    private boolean underlineStyle;

    @Shadow
    private int[] colorCode;

    @Shadow
    private int textColor;

    @Shadow
    private boolean unicodeFlag;

    @Shadow
    public Random fontRandom;

    @Shadow
    protected int[] charWidth;

    @Shadow(remap = false)
    protected abstract void enableAlpha();

    @Shadow
    protected abstract void resetStyles();

    @Shadow
    public int FONT_HEIGHT;

    @Shadow
    protected abstract void loadGlyphTexture(int page);

    @Shadow(remap = false)
    protected abstract void bindTexture(ResourceLocation location);

    @Shadow
    @Final
    protected ResourceLocation locationFontTexture;
    @Shadow
    protected byte[] glyphWidth;
    @Unique
    private boolean swan$tessellating;
    @Unique
    private boolean swan$decoratorTessActive;
    @Unique
    private boolean swan$textTessActive;

    @Unique
    private TextTess swan$textTess;
    @Unique
    private TextTess swan$decoratorTess;
    @Unique
    private final BitSet swan$unicodeTessellatorsActive = new BitSet();
    @Unique
    private final TextTess[] swan$unicodeTessellators = new TextTess[256];

    @Unique
    @Override
    public boolean swan$beginTessellating() {
        boolean thisStart = !swan$tessellating;
        if (thisStart) {
            if (swan$textTess == null) {
                swan$textTess = new TextTess(true);
            }
            if (swan$decoratorTess == null) {
                swan$decoratorTess = new TextTess(false);
            }
            swan$tessellating = true;
            swan$textTessActive = false;
            swan$decoratorTessActive = false;
            swan$unicodeTessellatorsActive.clear();
        }
        return thisStart;
    }

    @Unique
    @Override
    public void swan$draw(boolean thisStart) {
        if (thisStart) {
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            swan$tessellating = false;
            bindTexture(locationFontTexture);
            if (swan$textTessActive) {
                swan$textTess.draw();
                swan$textTessActive = false;
            }
            for (int i = 0; i < 256; i++) {
                if (!swan$unicodeTessellatorsActive.get(i)) {
                    continue;
                }
                loadGlyphTexture(i);
                swan$unicodeTessellators[i].draw();
            }
            if (swan$decoratorTessActive) {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                swan$decoratorTess.draw();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                swan$decoratorTessActive = false;
            }
        }
    }

    /**
     * @author _
     * @reason _
     */
    @Overwrite
    public int drawString(String text, int x, int y, int color, boolean dropShadow) {
        this.resetStyles();
        int l;

        val thisStart = swan$beginTessellating();

        if (dropShadow) {
            l = this.renderString(text, x + 1, y + 1, color, true);
            l = Math.max(l, this.renderString(text, x, y, color, false));
        } else {
            l = this.renderString(text, x, y, color, false);
        }

        swan$draw(thisStart);

        return l;
    }

    /**
     * @author _
     * @reason _
     */
    @Overwrite
    public int renderString(String str, int x, int y, int color, boolean shadow) {
        if (str == null) {
            return 0;
        } else {
            val thisStart = swan$beginTessellating();
            if (this.bidiFlag) {
                str = this.bidiReorder(str);
            }

            if ((color & 0xfc000000) == 0) {
                color |= 0xff000000;
            }

            if (shadow) {
                color = (color & 0xfcfcfc) >> 2 | color & 0xff000000;
            }

            this.red = (float) (color >> 16 & 255) / 255.0F;
            this.green = (float) (color >> 8 & 255) / 255.0F;
            this.blue = (float) (color & 255) / 255.0F;
            this.alpha = (float) (color >> 24 & 255) / 255.0F;
            setColor(this.red, this.green, this.blue, this.alpha);
            this.posX = (float) x;
            this.posY = (float) y;
            this.renderStringAtPos(str, shadow);
            swan$draw(thisStart);

            // TODO: Color somehow doesn't get reset sometimes in this mess?
            //       Related to the DragonAPI pop-up text breaking I guess.
            GL11.glColor4f(1F, 1F, 1F, 1F);
            return (int) this.posX;
        }
    }

    /**
     * @author _
     * @reason _
     */
    @Overwrite
    public void renderStringAtPos(String text, boolean shadow) {
        val thisStart = swan$beginTessellating();
        for (int i = 0; i < text.length(); ++i) {
            char c0 = text.charAt(i);
            int j;
            int k;

            if (c0 == 167 && i + 1 < text.length()) {
                j = "0123456789abcdefklmnor".indexOf(text.toLowerCase()
                                                         .charAt(i + 1));

                if (j < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;

                    if (j < 0) {
                        j = 15;
                    }

                    if (shadow) {
                        j += 16;
                    }

                    k = this.colorCode[j];
                    this.textColor = k;
                    setColor((float) (k >> 16) / 255.0F,
                             (float) (k >> 8 & 255) / 255.0F,
                             (float) (k & 255) / 255.0F,
                             this.alpha);
                } else if (j == 16) {
                    this.randomStyle = true;
                } else if (j == 17) {
                    this.boldStyle = true;
                } else if (j == 18) {
                    this.strikethroughStyle = true;
                } else if (j == 19) {
                    this.underlineStyle = true;
                } else if (j == 20) {
                    this.italicStyle = true;
                } else if (j == 21) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    setColor(this.red, this.green, this.blue, this.alpha);
                }

                ++i;
            } else {
                j = CharHelper.getIndex(c0);

                if (this.randomStyle && j != -1) {
                    do {
                        k = this.fontRandom.nextInt(this.charWidth.length);
                    } while (this.charWidth[j] != this.charWidth[k]);

                    j = k;
                }

                float f1 = this.unicodeFlag ? 0.5F : 1.0F;
                boolean flag1 = (c0 == 0 || j == -1 || this.unicodeFlag) && shadow;

                if (flag1) {
                    this.posX -= f1;
                    this.posY -= f1;
                }

                float f = this.renderCharAtPos(j, c0, this.italicStyle);

                if (flag1) {
                    this.posX += f1;
                    this.posY += f1;
                }

                if (this.boldStyle) {
                    this.posX += f1;

                    if (flag1) {
                        this.posX -= f1;
                        this.posY -= f1;
                    }

                    this.renderCharAtPos(j, c0, this.italicStyle);
                    this.posX -= f1;

                    if (flag1) {
                        this.posX += f1;
                        this.posY += f1;
                    }

                    ++f;
                }

                doDraw(f);
            }
        }
        swan$draw(thisStart);
    }

    @Unique
    private VertexConsumer swan$beginDecoration() {
        if (swan$tessellating) {
            if (!swan$decoratorTessActive) {
                swan$decoratorTessActive = true;
                swan$decoratorTess.startDrawingQuads();
            }
            swan$decoratorTess.setColorRGBA_F(red, green, blue, alpha);
            return swan$decoratorTess::addVertex;
        } else {
            val tess = Tessellator.instance;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            tess.startDrawingQuads();
            return tess::addVertex;
        }
    }

    @Unique
    private void swan$endDecoration() {
        if (swan$tessellating) {
            return;
        }
        val tess = Tessellator.instance;
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * @author _
     * @reason _
     */
    @Overwrite(remap = false)
    protected void doDraw(float f) {
        if (this.strikethroughStyle) {
            val tessellator = swan$beginDecoration();
            tessellator.addVertex(this.posX, this.posY + (float) (this.FONT_HEIGHT / 2), 0.0F);
            tessellator.addVertex(this.posX + f, this.posY + (float) (this.FONT_HEIGHT / 2), 0.0F);
            tessellator.addVertex(this.posX + f, this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F, 0.0F);
            tessellator.addVertex(this.posX, this.posY + (float) (this.FONT_HEIGHT / 2) - 1.0F, 0.0F);
            swan$endDecoration();
        }

        if (this.underlineStyle) {
            val tess = swan$beginDecoration();
            int l = this.underlineStyle ? -1 : 0;
            tess.addVertex(this.posX + (float) l, this.posY + (float) this.FONT_HEIGHT, 0.0F);
            tess.addVertex(this.posX + f, this.posY + (float) this.FONT_HEIGHT, 0.0F);
            tess.addVertex(this.posX + f, this.posY + (float) this.FONT_HEIGHT - 1.0F, 0.0F);
            tess.addVertex(this.posX + (float) l, this.posY + (float) this.FONT_HEIGHT - 1.0F, 0.0F);
            swan$endDecoration();
        }

        this.posX += (float) ((int) f);
    }


    /**
     * @author _
     * @reason _
     */
    @Overwrite(remap = false)
    protected void setColor(float r, float g, float b, float a) {
        if (swan$tessellating) {
            this.swan$textTess.setColorRGBA_F(r, g, b, a);
        } else {
            GL11.glColor4f(r, g, b, a);
        }
    }

    /**
     * @author _
     * @reason _
     */
    @Overwrite
    public int getCharWidth(char character) {
        if (character == 167) {
            return -1;
        } else if (character == 32) {
            return 4;
        } else {
            int i = CharHelper.getIndex(character);

            if (character > 0 && i != -1 && !this.unicodeFlag) {
                return this.charWidth[i];
            } else if (this.glyphWidth[character] != 0) {
                int j = this.glyphWidth[character] >>> 4;
                int k = this.glyphWidth[character] & 15;

                if (k > 7) {
                    k = 15;
                    j = 0;
                }

                ++k;
                return (k - j) / 2 + 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * @author _
     * @reason _
     */
    @Overwrite
    public float renderCharAtPos(int p_78278_1_, char p_78278_2_, boolean p_78278_3_) {
        //@formatter:off
        return p_78278_2_ == 32 ? 4.0F
                                : (CharHelper.isVanillaChar(p_78278_2_) &&
                                   !this.unicodeFlag
                                   ? this.renderDefaultChar(p_78278_1_, p_78278_3_)
                                   : this.renderUnicodeChar(p_78278_2_, p_78278_3_));
        //@formatter:on
    }

    /**
     * @author _
     * @reason _
     */
    @Overwrite
    protected float renderDefaultChar(int ch, boolean italic) {
        float u = (float) (ch % 16 * 8);
        float v = (float) (ch / 16 * 8);
        float tilt = italic ? 1.0F : 0.0F;
        float width = (float) this.charWidth[ch] - 0.01F;
        if (swan$tessellating) {
            if (!swan$textTessActive) {
                swan$textTessActive = true;
                swan$textTess.startDrawingQuads();
            }
            //@formatter:off
            swan$textTess.addVertexWithUV(this.posX + tilt, this.posY, 0.0F, u / 128.0F, v / 128.0F);
            swan$textTess.addVertexWithUV(this.posX - tilt, this.posY + 7.99F, 0.0F, u / 128.0F, (v + 7.99F) / 128.0F);
            swan$textTess.addVertexWithUV(this.posX + width - 1.0F - tilt, this.posY + 7.99F, 0.0F, (u + width - 1.0F) / 128.0F, (v + 7.99F) / 128.0F);
            swan$textTess.addVertexWithUV(this.posX + width - 1.0F + tilt, this.posY, 0.0F, (u + width - 1.0F) / 128.0F, v / 128.0F);
            //@formatter:on
        } else {
            bindTexture(this.locationFontTexture);
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glTexCoord2f(u / 128.0F, v / 128.0F);
            GL11.glVertex3f(this.posX + tilt, this.posY, 0.0F);
            GL11.glTexCoord2f(u / 128.0F, (v + 7.99F) / 128.0F);
            GL11.glVertex3f(this.posX - tilt, this.posY + 7.99F, 0.0F);
            GL11.glTexCoord2f((u + width - 1.0F) / 128.0F, v / 128.0F);
            GL11.glVertex3f(this.posX + width - 1.0F + tilt, this.posY, 0.0F);
            GL11.glTexCoord2f((u + width - 1.0F) / 128.0F, (v + 7.99F) / 128.0F);
            GL11.glVertex3f(this.posX + width - 1.0F - tilt, this.posY + 7.99F, 0.0F);
            GL11.glEnd();
        }
        return (float) this.charWidth[ch];
    }

    /**
     * @author _
     * @reason _
     */
    @Overwrite
    protected float renderUnicodeChar(char ch, boolean italic) {
        if (this.glyphWidth[ch] == 0) {
            return 0.0F;
        } else {
            int glyphTex = ch / 256;
            float widthM = (float) (this.glyphWidth[ch] >>> 4);
            float widthL = (float) (this.glyphWidth[ch] & 15);
            float kerning = widthL + 1;
            float u = (float) (ch % 16 * 16) + widthM;
            float v = (float) ((ch & 255) / 16 * 16);
            float width = kerning - widthM - 0.02F;
            float tilt = italic ? 1.0F : 0.0F;
            if (swan$tessellating) {
                TextTess tess;
                if (swan$unicodeTessellators[glyphTex] == null) {
                    swan$unicodeTessellators[glyphTex] = new TextTess(true);
                }
                tess = swan$unicodeTessellators[glyphTex];
                if (!swan$unicodeTessellatorsActive.get(glyphTex)) {
                    tess.startDrawingQuads();
                    swan$unicodeTessellatorsActive.set(glyphTex);
                }
                tess.setColorRGBA_F(red, green, blue, alpha);
                //@formatter:off
                tess.addVertexWithUV(this.posX + tilt, this.posY, 0.0F, u / 256.0F, v / 256.0F);
                tess.addVertexWithUV(this.posX - tilt, this.posY + 7.99F, 0.0F, u / 256.0F, (v + 15.98F) / 256.0F);
                tess.addVertexWithUV(this.posX + width / 2.0F - tilt, this.posY + 7.99F, 0.0F, (u + width) / 256.0F, (v + 15.98F) / 256.0F);
                tess.addVertexWithUV(this.posX + width / 2.0F + tilt, this.posY, 0.0F, (u + width) / 256.0F, v / 256.0F);
                //@formatter:on
            } else {
                this.loadGlyphTexture(glyphTex);
                GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
                GL11.glTexCoord2f(u / 256.0F, v / 256.0F);
                GL11.glVertex3f(this.posX + tilt, this.posY, 0.0F);
                GL11.glTexCoord2f(u / 256.0F, (v + 15.98F) / 256.0F);
                GL11.glVertex3f(this.posX - tilt, this.posY + 7.99F, 0.0F);
                GL11.glTexCoord2f((u + width) / 256.0F, v / 256.0F);
                GL11.glVertex3f(this.posX + width / 2.0F + tilt, this.posY, 0.0F);
                GL11.glTexCoord2f((u + width) / 256.0F, (v + 15.98F) / 256.0F);
                GL11.glVertex3f(this.posX + width / 2.0F - tilt, this.posY + 7.99F, 0.0F);
                GL11.glEnd();
            }
            return (kerning - (float) widthM) / 2.0F + 1.0F;
        }
    }
}
