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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ShadowFrustumTest {
    @Test
    void castersMustOverlapReceivers() {
        var frustum = new ShadowFrustum();
        frustum.begin();
        frustum.end();
        Assertions.assertFalse(frustum.testAab(0, 0, 0, 1, 1, 1));

        frustum.begin();
        frustum.addShadowReceiver(0, 0, 0, 16, 16, 16);
        frustum.end();
        Assertions.assertTrue(frustum.testAab(4, 4, 4, 5, 5, 5));
        Assertions.assertFalse(frustum.testAab(32, 0, 0, 33, 1, 1));
    }
}
