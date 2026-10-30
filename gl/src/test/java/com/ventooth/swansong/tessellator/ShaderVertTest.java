/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.tessellator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ShaderVertTest {
    @Test
    void completeQuad() {
        var a = vert(0, 0, 0, 0);
        var b = vert(1, 0, 1, 0);
        var c = vert(1, 1, 1, 1);
        var d = vert(0, 1, 0, 1);

        ShaderVert.completeQuad(a, b, c, d);

        for (var v : new ShaderVert[]{a, b, c, d}) {
            Assertions.assertArrayEquals(new float[]{0, 0, 1}, new float[]{v.normalX, v.normalY, v.normalZ}, 1e-6F);
            Assertions.assertArrayEquals(new float[]{1, 0, 0, -1},
                                         new float[]{v.tangentX, v.tangentY, v.tangentZ, v.tangentW},
                                         1e-6F);
            Assertions.assertArrayEquals(new float[]{0.5F, 0.5F, 0, 0},
                                         new float[]{v.midTextureU, v.midTextureV, v.edgeTextureU, v.edgeTextureV},
                                         1e-6F);
        }
    }

    @Test
    void completeTriangleLeavesNoFourthVertex() {
        var a = vert(0, 0, 0, 0);
        var b = vert(1, 0, 1, 0);
        var c = vert(1, 1, 1, 1);

        ShaderVert.completeTriangle(a, b, c);

        Assertions.assertArrayEquals(new float[]{0, 0, 1}, new float[]{c.normalX, c.normalY, c.normalZ}, 1e-6F);
        Assertions.assertEquals(1, c.tangentX);
        Assertions.assertEquals(0.5F, c.midTextureU);
    }

    private static ShaderVert vert(float x, float y, float u, float v) {
        var vert = new ShaderVert();
        vert.positionX = x;
        vert.positionY = y;
        vert.textureU = u;
        vert.textureV = v;
        return vert;
    }
}
