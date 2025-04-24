#version 120

#define FSH

#include "/libs/rple.glsl"

uniform sampler2D texture;

varying vec4 texcoord;
varying vec4 glcolor;

void main() {
    vec4 textureColor = texture2DProj(texture, texcoord);
    vec4 blockLightColor = blockLight();

    vec4 color = glcolor * textureColor * blockLightColor;

/* DRAWBUFFERS:0 */
    gl_FragData[0] = color; //gcolor
}