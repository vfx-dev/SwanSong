#version 130

#define VSH

#include "/libs/rple.glsl"

varying vec4 texcoord;
varying vec4 glcolor;

uniform bool swan_portalEyeS;
uniform bool swan_portalEyeT;
uniform bool swan_portalEyeR;
uniform bool swan_portalEyeQ;

float ftexgenSingle(vec4 vert, vec4 eyeVert, bool cond, vec4 objectPlane, vec4 eyePlane) {
    if (cond) {
        return dot(eyeVert, eyePlane);
    } else {
        return dot(vert, objectPlane);
    }
}

vec4 ftexgen() {
    vec4 tex = vec4(0.0);
    vec4 eyeVert = gl_ModelViewMatrix * gl_Vertex;
    tex.s = ftexgenSingle(gl_Vertex, eyeVert, swan_portalEyeS, gl_ObjectPlaneS[0], gl_EyePlaneS[0]);
    tex.t = ftexgenSingle(gl_Vertex, eyeVert, swan_portalEyeT, gl_ObjectPlaneT[0], gl_EyePlaneT[0]);
    tex.p = ftexgenSingle(gl_Vertex, eyeVert, swan_portalEyeR, gl_ObjectPlaneR[0], gl_EyePlaneR[0]);
    tex.q = ftexgenSingle(gl_Vertex, eyeVert, swan_portalEyeQ, gl_ObjectPlaneQ[0], gl_EyePlaneQ[0]);
    return gl_TextureMatrix[0] * tex;
}

void main() {
    setLightMapCoordinates();
    gl_Position = ftransform();
    texcoord = ftexgen();
    glcolor = gl_Color;
}