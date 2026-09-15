#version 120

uniform sampler2D blitsrc;

varying vec2 texcoord;

void main() {
	gl_FragData[0] = texture2D(blitsrc, texcoord);
}