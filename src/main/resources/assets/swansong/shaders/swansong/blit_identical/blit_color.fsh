#version 130

uniform sampler2D blitsrc;

varying vec2 texcoord;

void main() {
	int lod = 0;
	ivec2 size = textureSize(blitsrc, lod);
	ivec2 pos = ivec2(size.x * texcoord.x, size.y * texcoord.y);
	gl_FragData[0] = texelFetch(blitsrc, pos, lod);
}