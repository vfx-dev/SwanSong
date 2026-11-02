package com.ventooth.swansong.sufrace;

import com.ventooth.swansong.shader.BufferNameUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

public record RawTextureData(String path, int target, int internalFormat, int width, int height, int depth,
                             int pixelFormat, int pixelType, int byteSize) {
    // NOTE: this only supports unpacked color pixels, add other formats later if a pack needs them
    public static RawTextureData parse(String declaration) {
        var parts = declaration.trim().split("\\s+");
        if (parts.length < 6) throw new IllegalArgumentException("Incomplete raw texture declaration: " + declaration);
        int dimensions = switch (parts[1]) {
            case "TEXTURE_1D" -> 1;
            case "TEXTURE_2D", "TEXTURE_RECTANGLE" -> 2;
            case "TEXTURE_3D" -> 3;
            default -> throw new IllegalArgumentException("Unknown texture target: " + parts[1]);
        };
        if (parts.length != 5 + dimensions) throw new IllegalArgumentException("Invalid raw texture declaration: " + declaration);
        int target = switch (parts[1]) {
            case "TEXTURE_1D" -> GL11.GL_TEXTURE_1D;
            case "TEXTURE_2D" -> GL11.GL_TEXTURE_2D;
            case "TEXTURE_3D" -> GL12.GL_TEXTURE_3D;
            default -> GL31.GL_TEXTURE_RECTANGLE;
        };
        int internal = BufferNameUtil.gbufferFormatFromName(parts[2]);
        if (internal < 0) throw new IllegalArgumentException("Unknown texture format: " + parts[2]);
        int width = Integer.parseInt(parts[3]);
        int height = dimensions >= 2 ? Integer.parseInt(parts[4]) : 1;
        int depth = dimensions == 3 ? Integer.parseInt(parts[5]) : 1;
        if (width <= 0 || height <= 0 || depth <= 0) throw new IllegalArgumentException("Texture dimensions must be positive");
        int format = switch (parts[3 + dimensions]) {
            case "RED" -> GL11.GL_RED;
            case "RG" -> GL30.GL_RG;
            case "RGB" -> GL11.GL_RGB;
            case "RGBA" -> GL11.GL_RGBA;
            case "BGR" -> GL12.GL_BGR;
            case "BGRA" -> GL12.GL_BGRA;
            default -> throw new IllegalArgumentException("Unknown pixel format: " + parts[3 + dimensions]);
        };
        int type = switch (parts[4 + dimensions]) {
            case "BYTE" -> GL11.GL_BYTE;
            case "UNSIGNED_BYTE" -> GL11.GL_UNSIGNED_BYTE;
            case "SHORT" -> GL11.GL_SHORT;
            case "UNSIGNED_SHORT" -> GL11.GL_UNSIGNED_SHORT;
            case "INT" -> GL11.GL_INT;
            case "UNSIGNED_INT" -> GL11.GL_UNSIGNED_INT;
            case "FLOAT" -> GL11.GL_FLOAT;
            case "HALF_FLOAT" -> GL30.GL_HALF_FLOAT;
            default -> throw new IllegalArgumentException("Unknown pixel type: " + parts[4 + dimensions]);
        };
        int components = switch (format) {
            case GL11.GL_RED -> 1;
            case GL30.GL_RG -> 2;
            case GL11.GL_RGB, GL12.GL_BGR -> 3;
            default -> 4;
        };
        int bytes = switch (type) {
            case GL11.GL_BYTE, GL11.GL_UNSIGNED_BYTE -> 1;
            case GL11.GL_SHORT, GL11.GL_UNSIGNED_SHORT, GL30.GL_HALF_FLOAT -> 2;
            default -> 4;
        };
        int size = Math.multiplyExact(Math.multiplyExact(Math.multiplyExact(width, height), depth), components * bytes);
        return new RawTextureData(parts[0], target, internal, width, height, depth, format, type, size);
    }
}
