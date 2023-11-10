#version 150 core

#define ambientLight 0.6f

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;
uniform sampler2D mask;
uniform vec3 color;

uniform float lightLevel;

void main() {
    vec4 baseColor = texture(diffuse, texCoord0);
    if (baseColor.a < 0.01) discard;

    float mask = texture(mask, texCoord0).x;

    vec3 color1 = baseColor.xyz * mix(vec3(1.0), color, mask);

    outColor = vec4(vec3(mask), 1.0f);
}
