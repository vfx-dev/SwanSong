/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.resources.pack;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.WorldProvider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class ResolvedShaderPack extends ShaderPack {
    private final byte[] storage;

    private final Object2LongMap<String> offsets;

    private final List<WorldSpecializationPredicate> specializations;

    private final boolean hasWorld0;

    public ResolvedShaderPack(String name,
                              byte[] storage,
                              Object2LongMap<String> offsets,
                              List<WorldSpecializationPredicate> specializations,
                              boolean hasWorld0) {
        super(name);
        this.storage = storage;
        this.offsets = offsets;
        this.specializations = specializations;
        this.hasWorld0 = hasWorld0;
    }

    @Override
    public @NotNull InputStream get(String path) throws IOException {
        val region = offsets.getOrDefault(path, -1);
        if (region == -1) {
            throw new FileNotFoundException(path);
        }
        val offset = (int) ((region >>> 32) & 0xFFFFFFFFL);
        val length = (int) (region & 0xFFFFFFFFL);
        return new ByteArrayInputStream(storage, offset, length);
    }

    @Override
    public boolean has(String path) {
        return offsets.containsKey(path);
    }

    @Override
    public @Nullable String getWorldSpecialization(@Nullable WorldProvider dimension) {
        if (dimension != null) {
            for (val specialization : specializations) {
                if (specialization.matches(dimension)) {
                    return specialization.dirName();
                }
            }
        }
        if (hasWorld0) {
            return "world0";
        }
        return null;
    }

    @RequiredArgsConstructor
    public static class Builder {
        private final String name;
        private final ExposedByteArrayOutputStream out = new ExposedByteArrayOutputStream();
        private final Object2LongMap<String> offsets = new Object2LongOpenHashMap<>();
        private final ObjectList<WorldSpecializationPredicate> specializations = new ObjectArrayList<>();
        private final ObjectList<WorldSpecializationPredicate> specializationsIris = new ObjectArrayList<>();
        private final ObjectList<WorldSpecializationPredicate> specializationsSwansong = new ObjectArrayList<>();
        private boolean hasWorld0 = false;
        private final byte[] copyBuf = new byte[4096];

        public void addSpecialization(WorldSpecializationPredicate specialization) {
            specializations.add(specialization);
        }

        public void hasWorld0() {
            this.hasWorld0 = true;
        }

        public void add(String path, InputStream data) throws IOException {
            val offset = out.size();
            val size = copyFrom(data);
            if ("shaders/dimensions.properties".equals(path)) {
                parseDimensionProperties(offset, size, DimensionPropertiesVariant.Iris);
            } else if ("shaders/dimensions_swansong.properties".equals(path)) {
                parseDimensionProperties(offset, size, DimensionPropertiesVariant.Swansong);
            }
            val packed = (((long) offset & 0xFFFFFFFFL) << 32) | ((long) size & 0xFFFFFFFFL);
            if (path.charAt(0) != '/') {
                path = "/" + path;
            }
            offsets.put(path, packed);
        }

        private void parseDimensionProperties(int offset, int size, DimensionPropertiesVariant variant)
                throws IOException {
            val properties = new Properties();
            val in = new ByteArrayInputStream(out.buf(), offset, size);
            properties.load(in);
            for (val name : properties.stringPropertyNames()) {
                if (!name.startsWith("dimension.")) {
                    continue;
                }
                val value = properties.getProperty(name);
                if (value == null) {
                    continue;
                }
                val dirName = name.substring("dimension.".length());
                switch (variant) {
                    case Iris -> {
                        val namespaces = StringUtils.split(value, ' ');
                        for (val namespace : namespaces) {
                            switch (namespace) {
                                case "minecraft:the_overworld" ->
                                        specializationsIris.add(new WorldSpecializationPredicate.ByName("Overworld",
                                                                                                        dirName));
                                case "minecraft:the_nether" ->
                                        specializationsIris.add(new WorldSpecializationPredicate.ByName("Nether",
                                                                                                        dirName));
                                case "minecraft:the_end" ->
                                        specializationsIris.add(new WorldSpecializationPredicate.ByName("The End",
                                                                                                        dirName));
                                case "*" -> specializationsIris.add(new WorldSpecializationPredicate.All(dirName));
                            }
                        }
                    }
                    case Swansong -> {
                        val patterns = StringUtils.split(value, ';');
                        for (val pattern : patterns) {
                            val parts = StringUtils.split(pattern, ":", 2);
                            if (parts.length != 2) {
                                continue;
                            }
                            val type = parts[0].trim();
                            var subPattern = parts[1].trim();
                            if (subPattern.length() > 2 &&
                                subPattern.charAt(0) == '"' &&
                                subPattern.charAt(subPattern.length() - 1) == '"') {
                                subPattern = subPattern.substring(1, subPattern.length() - 1);
                            }
                            switch (type) {
                                case "id" -> {
                                    try {
                                        int dimID = Integer.parseInt(subPattern);
                                        specializationsSwansong.add(new WorldSpecializationPredicate.ByID(dimID,
                                                                                                          dirName));
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                                case "class" -> {
                                    specializationsSwansong.add(new WorldSpecializationPredicate.ByClass(subPattern,
                                                                                                         dirName));
                                }
                                case "name" -> {
                                    specializationsSwansong.add(new WorldSpecializationPredicate.ByName(subPattern,
                                                                                                        dirName));
                                }
                            }
                        }
                    }
                }
            }
        }

        private int copyFrom(InputStream data) throws IOException {
            int totalRead = 0;
            int read;
            while ((read = data.read(copyBuf)) >= 0) {
                out.write(copyBuf, 0, read);
                totalRead += read;
            }
            return totalRead;
        }

        public ResolvedShaderPack build() {
            val spec = new ObjectArrayList<WorldSpecializationPredicate>();
            spec.addAll(specializationsSwansong);
            spec.addAll(specializationsIris);
            spec.addAll(specializations);
            return new ResolvedShaderPack(name, out.toByteArray(), offsets, spec, hasWorld0);
        }

        private static class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
            public byte[] buf() {
                return buf;
            }
        }
    }

    public interface WorldSpecializationPredicate {
        boolean matches(@NotNull WorldProvider dimension);

        String dirName();

        @RequiredArgsConstructor
        class ByID implements WorldSpecializationPredicate {
            private final int id;
            @Getter
            private final String dirName;

            @Override
            public boolean matches(@NotNull WorldProvider dimension) {
                return dimension.dimensionId == id;
            }
        }

        @RequiredArgsConstructor
        class ByClass implements WorldSpecializationPredicate {
            private final String className;
            @Getter
            private final String dirName;

            @Override
            public boolean matches(@NotNull WorldProvider dimension) {
                return matchesClass(dimension.getClass());
            }

            private boolean matchesClass(Class<?> klass) {
                if (className.equals(klass.getName())) {
                    return true;
                }
                val sup = klass.getSuperclass();
                if (sup != null && matchesClass(sup)) {
                    return true;
                }
                val itfs = klass.getInterfaces();
                for (val itf : itfs) {
                    if (matchesClass(itf)) {
                        return true;
                    }
                }
                return false;
            }
        }

        @RequiredArgsConstructor
        class ByName implements WorldSpecializationPredicate {
            private final String dimName;
            @Getter
            private final String dirName;

            @Override
            public boolean matches(@NotNull WorldProvider dimension) {
                return dimName.equals(dimension.getDimensionName());
            }
        }

        @RequiredArgsConstructor
        class All implements WorldSpecializationPredicate {
            @Getter
            private final String dirName;

            @Override
            public boolean matches(@NotNull WorldProvider dimension) {
                return true;
            }
        }
    }

    enum DimensionPropertiesVariant {
        Iris,
        Swansong
    }
}
