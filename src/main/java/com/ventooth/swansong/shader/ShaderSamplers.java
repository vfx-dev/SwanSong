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

import com.ventooth.swansong.shader.uniform.Uniform;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShaderSamplers {
    public static final class GBuffer {
        // @formatter:off
        private static final List<Uniform<?>> uniforms = new SamplerUniformListBuilder()
                .add("texture",         GBuffer::texture)
                .add("albedoTex",       GBuffer::texture) //Used by Inhouse shader
                .add("lightmap",        GBuffer::lightmap)
                .add("normals",         GBuffer::normals)
                .add("normalTex",       GBuffer::normals) //Used by Inhouse shader
                .add("specular",        GBuffer::specular)
                .add("specularTex",     GBuffer::specular) //Used by Inhouse shader
                .add("shadowtex0",      CompositeTextureData.shadowtex0::gpuIndex)
                .add("shadow",          CompositeTextureData.shadowtex0::gpuIndex)
                .add("shadowtex1",      CompositeTextureData.shadowtex1::gpuIndex)
                .add("watershadow",     CompositeTextureData.shadowtex1::gpuIndex)
                .add("depthtex0",       CompositeTextureData.depthtex0::gpuIndex)
                .add("gaux1",           CompositeTextureData.colortex4::gpuIndex)
                .add("gaux2",           CompositeTextureData.colortex5::gpuIndex)
                .add("gaux3",           CompositeTextureData.colortex6::gpuIndex)
                .add("gaux4",           CompositeTextureData.colortex7::gpuIndex)
                .add("depthtex1",       CompositeTextureData.depthtex1::gpuIndex)
                .add("shadowcolor0",    CompositeTextureData.shadowcolor0::gpuIndex)
                .add("shadowcolor",     CompositeTextureData.shadowcolor0::gpuIndex)
                .add("shadowcolor1",    CompositeTextureData.shadowcolor1::gpuIndex)
                .add("noisetex",        CompositeTextureData.noisetex::gpuIndex)
                .build();
        // @formatter:on

        public static List<Uniform<?>> uniforms() {
            return uniforms;
        }

        // @formatter:off
        public static int texture()         {return  0;}
        public static int lightmap()        {return  1;}
        public static int normals()         {return  2;}
        public static int specular()        {return  3;}
        // @formatter:on
    }

    public static final class Shadow {
        // @formatter:off
        private static final List<Uniform<?>> uniforms = new SamplerUniformListBuilder()
                .add("tex",             Shadow::tex)
                .add("texture",         Shadow::texture)
                .add("lightmap",        Shadow::lightmap)
                .add("normals",         Shadow::normals)
                .add("specular",        Shadow::specular)
                .add("shadowtex0",      CompositeTextureData.shadowtex0::gpuIndex)
                .add("shadow",          CompositeTextureData.shadowtex0::gpuIndex)
                .add("shadowtex1",      CompositeTextureData.shadowtex1::gpuIndex)
                .add("watershadow",     CompositeTextureData.shadowtex1::gpuIndex)
                .add("gaux1",           CompositeTextureData.colortex4::gpuIndex)
                .add("gaux2",           CompositeTextureData.colortex5::gpuIndex)
                .add("gaux3",           CompositeTextureData.colortex6::gpuIndex)
                .add("gaux4",           CompositeTextureData.colortex7::gpuIndex)
                .add("shadowcolor",     CompositeTextureData.shadowcolor0::gpuIndex)
                .add("shadowcolor0",    CompositeTextureData.shadowcolor0::gpuIndex)
                .add("shadowcolor1",    CompositeTextureData.shadowcolor1::gpuIndex)
                .add("noisetex",        CompositeTextureData.noisetex::gpuIndex)
                .build();
        // @formatter:on

        public static List<Uniform<?>> uniforms() {
            return uniforms;
        }

        // @formatter:off
        public static int tex()             {return  0;}
        public static int texture()         {return  0;}
        public static int lightmap()        {return  1;}
        public static int normals()         {return  2;}
        public static int specular()        {return  3;}
        public static int shadow()          {return  4;}
        // @formatter:on
    }

    public static final class Composite {
        // @formatter:off
        private static final List<Uniform<?>> uniforms = new SamplerUniformListBuilder()
                .add("colortex0",       CompositeTextureData.colortex0::gpuIndex)
                .add("gcolor",          CompositeTextureData.colortex0::gpuIndex)
                .add("texture",         CompositeTextureData.colortex0::gpuIndex) //Spotted in Tea shaders
                .add("colortex1",       CompositeTextureData.colortex1::gpuIndex)
                .add("gdepth",          CompositeTextureData.colortex1::gpuIndex)
                .add("colortex2",       CompositeTextureData.colortex2::gpuIndex)
                .add("gnormal",         CompositeTextureData.colortex2::gpuIndex)
                .add("colortex3",       CompositeTextureData.colortex3::gpuIndex)
                .add("composite",       CompositeTextureData.colortex3::gpuIndex)
                .add("colortex4",       CompositeTextureData.colortex4::gpuIndex)
                .add("gaux1",           CompositeTextureData.colortex4::gpuIndex)
                .add("colortex5",       CompositeTextureData.colortex5::gpuIndex)
                .add("gaux2",           CompositeTextureData.colortex5::gpuIndex)
                .add("colortex6",       CompositeTextureData.colortex6::gpuIndex)
                .add("gaux3",           CompositeTextureData.colortex6::gpuIndex)
                .add("colortex7",       CompositeTextureData.colortex7::gpuIndex)
                .add("gaux4",           CompositeTextureData.colortex7::gpuIndex)
                .add("colortex8",       CompositeTextureData.colortex8::gpuIndex)
                .add("colortex9",       CompositeTextureData.colortex9::gpuIndex)
                .add("colortex10",      CompositeTextureData.colortex10::gpuIndex)
                .add("colortex11",      CompositeTextureData.colortex11::gpuIndex)
                .add("colortex12",      CompositeTextureData.colortex12::gpuIndex)
                .add("colortex13",      CompositeTextureData.colortex13::gpuIndex)
                .add("colortex14",      CompositeTextureData.colortex14::gpuIndex)
                .add("colortex15",      CompositeTextureData.colortex15::gpuIndex)
                .add("shadowtex0",      CompositeTextureData.shadowtex0::gpuIndex)
                .add("shadow",          CompositeTextureData.shadowtex0::gpuIndex)
                .add("shadowtex1",      CompositeTextureData.shadowtex1::gpuIndex)
                .add("watershadow",     CompositeTextureData.shadowtex0::gpuIndex)
                .add("depthtex0",       CompositeTextureData.depthtex0::gpuIndex)
                .add("gdepthtex",       CompositeTextureData.depthtex0::gpuIndex)
                .add("depthtex1",       CompositeTextureData.depthtex1::gpuIndex)
                .add("depthtex2",       CompositeTextureData.depthtex2::gpuIndex)
                .add("shadowcolor0",    CompositeTextureData.shadowcolor0::gpuIndex)
                .add("shadowcolor",     CompositeTextureData.shadowcolor0::gpuIndex)
                .add("shadowcolor1",    CompositeTextureData.shadowcolor1::gpuIndex)
                .add("noisetex",        CompositeTextureData.noisetex::gpuIndex)
                .add("blitsrc",         CompositeTextureData.blitsrc::gpuIndex)
                .build();
        // @formatter:on

        public static List<Uniform<?>> uniforms() {
            return uniforms;
        }

        // @formatter:off
        // @formatter:on
    }

    private static class SamplerUniformListBuilder {
        private final List<Uniform<?>> uniforms = new ArrayList<>();

        SamplerUniformListBuilder add(String name, Uniform.IntSupplier src) {
            uniforms.add(new Uniform.OfInt(name, src, Uniform::set));
            return this;
        }

        List<Uniform<?>> build() {
            return Collections.unmodifiableList(uniforms);
        }
    }
}
