#version 150 core

#define ambientLight 0.6f

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;

uniform float lightLevel;

void main() {
    vec4 color = texture(diffuse, texCoord0);

    outColor = vec4(lightLevel, lightLevel, lightLevel, 1.0f) * color;
    outColor.a = color.a;
}
