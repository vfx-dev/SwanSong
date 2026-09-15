#version 120

#define VSH

// Static
attribute vec3 inst_Position;
attribute vec2 inst_Texture;
attribute vec4 inst_Color;
attribute vec4 inst_EntityData;
attribute vec3 inst_Normal;
attribute vec4 inst_Tangent;
attribute vec2 inst_MidTexture;
attribute vec2 inst_EdgeTexture;

// Dynamic
attribute mat4 inst_ModelMat;
attribute vec2 inst_BrightnessR;
attribute vec2 inst_BrightnessG;
attribute vec2 inst_BrightnessB;

#define gl_MultiTexCoord1 vec4(inst_BrightnessR.xy, 0.0, 1.0)
#define gl_MultiTexCoord6 vec4(inst_BrightnessG.xy, 0.0, 1.0)
#define gl_MultiTexCoord7 vec4(inst_BrightnessB.xy, 0.0, 1.0)
#include "/libs/rple.glsl"

varying vec2 texcoord;
varying vec4 glcolor;

void main() {
    setLightMapCoordinates();

    texcoord = inst_Texture;
    // TODO: Validate that this is indeed, the correct math?
    gl_Position = gl_ModelViewProjectionMatrix * (inst_ModelMat * vec4(inst_Position, 1.0));
    glcolor = inst_Color;
}