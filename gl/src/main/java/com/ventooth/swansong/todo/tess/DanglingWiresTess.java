/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.todo.tess;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Unmodifiable;

@Deprecated
public class DanglingWiresTess {
    public static final int entityAttrib = 10;
    public static final int midTexCoordAttrib = 11;
    public static final int tangentAttrib = 12;
    public static final int edgeTexCoordAttrib = 13;

    public static final boolean useEntityAttrib = true;
    public static final boolean useMidTexCoordAttrib = true;
    public static final boolean useMultiTexCoord3Attrib = false;
    public static final boolean useTangentAttrib = true;
    public static final boolean useEdgeTexCoordAttrib = true;

    public static final @Unmodifiable ObjectList<AttribMapping> attribs;

    static {
        val tempAttribs = new ObjectArrayList<AttribMapping>();
        tempAttribs.add(new AttribMapping(entityAttrib, "mc_Entity"));
        tempAttribs.add(new AttribMapping(midTexCoordAttrib, "mc_midTexCoord"));
        tempAttribs.add(new AttribMapping(tangentAttrib, "at_tangent"));
        tempAttribs.add(new AttribMapping(edgeTexCoordAttrib, "rple_edgeTexCoord"));
        tempAttribs.add(new AttribMapping(edgeTexCoordAttrib, "swan_edgeTexCoord"));

        attribs = ObjectLists.unmodifiable(tempAttribs);
    }

    public record AttribMapping(int index,
                                String name) {
    }
}
