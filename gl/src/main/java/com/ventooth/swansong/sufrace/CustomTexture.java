package com.ventooth.swansong.sufrace;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.gl.GLTexture;
import com.ventooth.swansong.shader.CompositeTextureData;
import com.ventooth.swansong.shader.HostRenderer;
import com.ventooth.swansong.shader.preprocessor.FSProvider;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public record CustomTexture(CompositeTextureData unit, int target, @Nullable Texture2D image,
                            @Nullable GLTexture raw, int internalFormat, int width, int height) {
    public static @Nullable CustomTexture load(CompositeTextureData unit, FSProvider pack, HostRenderer host, String declaration) {
        try {
            FSProvider resources = new FSProvider() {
                @Override
                public InputStream get(String path) throws IOException {
                    return path.contains(":") ? host.openResource(path) : pack.get("/shaders/" + path.replaceFirst("^/", ""));
                }

                @Override
                public boolean has(String path) throws IOException {
                    try (var ignored = get(path)) {
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                }
            };
            if (declaration.trim().split("\\s+", 2).length == 1) {
                var image = CustomTexture2D.load(resources, declaration.trim());
                return image == null ? null : new CustomTexture(unit, GL11.GL_TEXTURE_2D, image, null,
                        image.internalFormat(), image.width(), image.height());
            }
            var data = RawTextureData.parse(declaration);
            byte[] bytes;
            try (var input = resources.get(data.path())) {
                bytes = input.readNBytes(Math.addExact(data.byteSize(), 1));
            }
            if (bytes.length != data.byteSize()) throw new IOException("Incorrect raw texture size: " + data.path());
            var buffer = BufferUtils.createByteBuffer(bytes.length).put(bytes);
            buffer.flip();
            var meta = new TextureMeta(false, false);
            if (resources.has(data.path() + ".mcmeta")) {
                try (var input = resources.get(data.path() + ".mcmeta")) {
                    var parsed = TextureMeta.read(new String(input.readAllBytes(), StandardCharsets.UTF_8));
                    if (parsed != null) meta = parsed;
                }
            }
            var texture = new GLTexture();
            texture.glGenTextures();
            int alignment = GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);
            int binding = GL11.glGetInteger(switch (data.target()) {
                case GL11.GL_TEXTURE_1D -> GL11.GL_TEXTURE_BINDING_1D;
                case GL12.GL_TEXTURE_3D -> GL12.GL_TEXTURE_BINDING_3D;
                case org.lwjgl.opengl.GL31.GL_TEXTURE_RECTANGLE -> org.lwjgl.opengl.GL31.GL_TEXTURE_BINDING_RECTANGLE;
                default -> GL11.GL_TEXTURE_BINDING_2D;
            });
            try {
                texture.glBindTexture(data.target());
                int wrap = meta.clamp() || data.target() == org.lwjgl.opengl.GL31.GL_TEXTURE_RECTANGLE
                        ? GL12.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT;
                GL11.glTexParameteri(data.target(), GL11.GL_TEXTURE_WRAP_S, wrap);
                if (data.target() != GL11.GL_TEXTURE_1D) GL11.glTexParameteri(data.target(), GL11.GL_TEXTURE_WRAP_T, wrap);
                if (data.target() == GL12.GL_TEXTURE_3D) GL11.glTexParameteri(data.target(), GL12.GL_TEXTURE_WRAP_R, wrap);
                int filter = meta.blur() ? GL11.GL_LINEAR : GL11.GL_NEAREST;
                GL11.glTexParameteri(data.target(), GL11.GL_TEXTURE_MIN_FILTER, filter);
                GL11.glTexParameteri(data.target(), GL11.GL_TEXTURE_MAG_FILTER, filter);
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
                switch (data.target()) {
                    case GL11.GL_TEXTURE_1D -> GL11.glTexImage1D(data.target(), 0, data.internalFormat(), data.width(), 0,
                            data.pixelFormat(), data.pixelType(), buffer);
                    case GL12.GL_TEXTURE_3D -> GL12.glTexImage3D(data.target(), 0, data.internalFormat(), data.width(), data.height(),
                            data.depth(), 0, data.pixelFormat(), data.pixelType(), buffer);
                    default -> GL11.glTexImage2D(data.target(), 0, data.internalFormat(), data.width(), data.height(), 0,
                            data.pixelFormat(), data.pixelType(), buffer);
                }
            } catch (RuntimeException | Error e) {
                texture.glDeleteTextures();
                throw e;
            } finally {
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, alignment);
                GL11.glBindTexture(data.target(), binding);
            }
            if (data.target() == GL11.GL_TEXTURE_2D) {
                var image = new Texture2D("custom:" + data.path(), texture, data.internalFormat(), data.width(), data.height());
                return new CustomTexture(unit, data.target(), image, null, data.internalFormat(), data.width(), data.height());
            }
            return new CustomTexture(unit, data.target(), null, texture, data.internalFormat(), data.width(), data.height());
        } catch (Exception e) {
            Share.log.warn("Failed to load custom texture: " + declaration, e);
            return null;
        }
    }

    public void bind() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit.gpuIndex());
        if (image != null) image.bind();
        else raw.glBindTexture(target);
    }

    public void deinit() {
        if (image != null) image.deinit();
        else raw.glDeleteTextures();
    }
}
