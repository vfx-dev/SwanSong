/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.resources;

import com.ventooth.swansong.shader.preprocessor.FSProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class ShaderpackResourceManagerAdapter implements IResourceManager {
    private final FSProvider fs;

    @Override
    public Set<String> getResourceDomains() {
        return Collections.singleton("minecraft");
    }

    @Override
    public IResource getResource(ResourceLocation location) throws IOException {
        if (!"minecraft".equals(location.getResourceDomain())) {
            return null;
        }
        InputStream is;
        try {
            is = fs.get(fs.absolutize(null, location.getResourcePath()));
        } catch (FileNotFoundException ignored) {
            return null;
        }
        return new FSResource(is);
    }

    @Override
    public List<IResource> getAllResources(ResourceLocation location) throws IOException {
        val res = getResource(location);
        return res == null ? Collections.emptyList() : Collections.singletonList(res);
    }

    @RequiredArgsConstructor
    private class FSResource implements IResource {
        private final InputStream is;

        @SneakyThrows
        @Override
        public InputStream getInputStream() {
            return is;
        }

        @Override
        public boolean hasMetadata() {
            return false;
        }

        @Override
        public IMetadataSection getMetadata(String sectionName) {
            return null;
        }
    }
}
