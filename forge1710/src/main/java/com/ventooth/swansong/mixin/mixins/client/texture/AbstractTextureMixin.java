/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.mixin.mixins.client.texture;

import com.ventooth.swansong.mixin.interfaces.PBRTextureHolder;
import com.ventooth.swansong.pbr.PBRTextureEngine;
import com.ventooth.swansong.sufrace.PBRTexture2D;
import com.ventooth.swansong.sufrace.TextureMeta;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.util.ResourceLocation;


@Unique
@Mixin(AbstractTexture.class)
@SuppressWarnings("MissingUnique") // MCDev Plugin Jank
public abstract class AbstractTextureMixin implements PBRTextureHolder {
    protected boolean swan$isLoaded;
    protected ResourceLocation swan$base;
    protected TextureMeta swan$meta;
    protected int swan$width;
    protected int swan$height;
    protected @Nullable PBRTexture2D.Bundle swan$pbrTex;

    /**
     * @implNote Reset state after the constructor runs
     */
    @Inject(method = "<init>",
            at = @At("RETURN"),
            require = 1)
    private void pbr_PostInit(CallbackInfo ci) {
        swan$reset();
    }

    /**
     * @implNote Reset happens before the main texture is deleted
     */
    @Inject(method = "deleteGlTexture",
            at = @At("HEAD"),
            require = 1)
    private void pbr_TexDeinit(CallbackInfo ci) {
        if (swan$pbrTex != null) {
            PBRTextureEngine.deinitPbrTex(swan$pbrTex, "On Delete");
        }
        swan$reset();
    }

    private void swan$reset() {
        swan$isLoaded = false;
        swan$meta = null;
        swan$base = null;
        swan$width = -1;
        swan$height = -1;
        swan$pbrTex = null;
    }

    @Override
    public ResourceLocation swan$base() throws IllegalStateException {
        swan$ensureValid(true);
        return swan$base;
    }

    @Override
    public TextureMeta swan$meta() throws IllegalStateException {
        swan$ensureValid(true);
        return swan$meta;
    }

    @Override
    public int swan$width() throws IllegalStateException {
        swan$ensureValid(true);
        return swan$width;
    }

    @Override
    public int swan$height() throws IllegalStateException {
        swan$ensureValid(true);
        return swan$height;
    }

    @Override
    public @Nullable PBRTexture2D.Bundle swan$pbrTex() throws IllegalStateException {
        swan$ensureValid(true);
        return swan$pbrTex;
    }

    @Override
    public @Nullable PBRTexture2D.Bundle swan$pbrTex(@Nullable PBRTexture2D.Bundle pbrTex)
            throws IllegalStateException {
        swan$ensureValid(true);
        val old = swan$pbrTex;
        swan$pbrTex = pbrTex;
        return old;
    }

    @Override
    public boolean swan$isValid() {
        return swan$ensureValid(false);
    }

    @Override
    public void swan$baseInit(ResourceLocation base, int width, int height, boolean blur, boolean clamp) {
        if (swan$pbrTex != null) {
            PBRTextureEngine.deinitPbrTex(swan$pbrTex, "On Re-Init");
            swan$pbrTex = null;
        }

        if (swan$supportsPbr()) {
            swan$isLoaded = true;
            swan$base = base;
            swan$meta = new TextureMeta(blur, clamp);
            swan$width = width;
            swan$height = height;

            PBRTextureEngine.logInitialLoad(this);
        }
    }

    @Override
    public String swan$baseToString() {
        if (!swan$isLoaded) {
            return "!UNKNOWN!";
        }

        val sb = new StringBuilder();
        sb.append("base=");
        if (swan$base == null) {
            sb.append("[UNKNOWN]");
        } else {
            sb.append('[')
              .append(swan$base)
              .append(']');
        }

        sb.append(" size=");
        if (swan$width > 0 && swan$height > 0) {
            sb.append('[')
              .append(swan$width)
              .append('x')
              .append(swan$height)
              .append(']');
        } else {
            sb.append("[UNKNOWN]");
        }
        if (swan$meta != null) {
            sb.append(" [blur=")
              .append(swan$meta.blur())
              .append(",clamp=")
              .append(swan$meta.clamp())
              .append(']');
        } else {
            sb.append(" [blur=UNKNOWN,clamp=UNKNOWN]");
        }

        return sb.toString();
    }

    private boolean swan$ensureValid(boolean throwing) throws IllegalStateException {
        if (!swan$supportsPbr()) {
            if (throwing) {
                throw new IllegalStateException("Texture does not support PBR");
            } else {
                return false;
            }
        }
        if (!swan$isLoaded) {
            if (throwing) {
                throw new IllegalStateException("Texture not loaded");
            } else {
                return false;
            }
        }
        if (swan$base == null) {
            if (throwing) {
                throw new IllegalStateException("No texture base present");
            } else {
                return false;
            }
        }
        if (swan$meta == null) {
            if (throwing) {
                throw new IllegalStateException("No texture meta present");
            } else {
                return false;
            }
        }
        if (swan$width <= 0) {
            if (throwing) {
                throw new IllegalStateException("Invalid width: " + swan$width);
            } else {
                return false;
            }
        }
        if (swan$height <= 0) {
            if (throwing) {
                throw new IllegalStateException("Invalid height: " + swan$height);
            } else {
                return false;
            }
        }
        return true;
    }
}
