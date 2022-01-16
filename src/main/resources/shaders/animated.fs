#version 120

varying vec2 texCoord0;
varying mat3 tbnMatrix;

uniform sampler2D diffuse;

vec2 CalcParallaxTexCoords(mat3 tbnMatrix, vec2 texCoords) {
	return texCoords.xy + (vec3(1.0, 1.0, 1.0) * tbnMatrix).xy;
}

void main() {
	vec2 texCoords = CalcParallaxTexCoords(tbnMatrix, texCoord0);
	gl_FragColor = texture2D(diffuse, texCoords);
}
