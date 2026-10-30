#version 120

uniform sampler2D blitsrc;

varying vec2 texcoord;

void main() {
	gl_FragDepth = texture2D(blitsrc, texcoord).x;
}