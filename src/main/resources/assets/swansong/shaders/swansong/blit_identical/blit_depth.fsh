#version 120

// Allows using `textureSize2D()` and `texelFetch2D()`
#extension GL_EXT_gpu_shader4 : enable

uniform sampler2D blitsrc;

varying vec2 texcoord;

void main() {
	int lod = 0;
	ivec2 size = textureSize2D(blitsrc, lod);
	ivec2 pos = ivec2(size.x * texcoord.x, size.y * texcoord.y);
	gl_FragDepth = texelFetch2D(blitsrc, pos, lod).x;
}