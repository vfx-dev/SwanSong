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

import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.dynamicBrightnessB;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.dynamicBrightnessG;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.dynamicBrightnessR;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.dynamicModelMat;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.staticColor;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.staticEdgeTexture;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.staticEntityData;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.staticMidTexture;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.staticNormal;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.staticPosition;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.staticTangent;
import static com.ventooth.swansong.api.SwanSongAttributes.Instanced.staticTexture;

@Deprecated
public class DanglingWiresTess {
    public static int entityAttrib = 10;
    public static int midTexCoordAttrib = 11;
    public static int tangentAttrib = 12;
    public static int edgeTexCoordAttrib = 13;

    public static boolean useEntityAttrib = true;
    public static boolean useMidTexCoordAttrib = true;
    public static boolean useMultiTexCoord3Attrib = false;
    public static boolean useTangentAttrib = true;
    public static boolean useEdgeTexCoordAttrib = true;

    public static final @Unmodifiable ObjectList<AttribMapping> attribs;

    static {
        val tempAttribs = new ObjectArrayList<AttribMapping>();
        tempAttribs.add(new AttribMapping(entityAttrib, "mc_Entity"));
        tempAttribs.add(new AttribMapping(midTexCoordAttrib, "mc_midTexCoord"));
        tempAttribs.add(new AttribMapping(tangentAttrib, "at_tangent"));
        tempAttribs.add(new AttribMapping(edgeTexCoordAttrib, "rple_edgeTexCoord"));
        tempAttribs.add(new AttribMapping(edgeTexCoordAttrib, "swan_edgeTexCoord"));

        tempAttribs.add(new AttribMapping(staticPosition, "inst_Position"));
        tempAttribs.add(new AttribMapping(staticTexture, "inst_Texture"));
        tempAttribs.add(new AttribMapping(staticColor, "inst_Color"));
        tempAttribs.add(new AttribMapping(staticEntityData, "inst_EntityData"));
        tempAttribs.add(new AttribMapping(staticNormal, "inst_Normal"));
        tempAttribs.add(new AttribMapping(staticTangent, "inst_Tangent"));
        tempAttribs.add(new AttribMapping(staticMidTexture, "inst_MidTexture"));
        tempAttribs.add(new AttribMapping(staticEdgeTexture, "inst_EdgeTexture"));

        tempAttribs.add(new AttribMapping(dynamicModelMat, "inst_ModelMat"));
        tempAttribs.add(new AttribMapping(dynamicBrightnessR, "inst_BrightnessR"));
        tempAttribs.add(new AttribMapping(dynamicBrightnessG, "inst_BrightnessG"));
        tempAttribs.add(new AttribMapping(dynamicBrightnessB, "inst_BrightnessB"));

        attribs = ObjectLists.unmodifiable(tempAttribs);
    }

    //TODO make this a record
    @RequiredArgsConstructor
    public static class AttribMapping {
        public final int index;
        public final String name;
    }
}
