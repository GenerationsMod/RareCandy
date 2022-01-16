#version 120
#include "sampling.glh"

varying vec2 texCoord0;
varying vec3 worldPos0;
varying mat3 tbnMatrix;

uniform vec3 C_eyePos;
uniform sampler2D diffuse;

void main() {
	vec3 directionToEye = normalize(C_eyePos - worldPos0);
	vec2 texCoords = CalcParallaxTexCoords(diffuse, tbnMatrix, directionToEye, texCoord0, 1, 0);
	gl_FragColor = texture2D(diffuse, texCoords);
}
