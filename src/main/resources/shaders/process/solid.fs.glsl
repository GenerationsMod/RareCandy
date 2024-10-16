#version 150 core

#define ambientLight 0.6f

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;

void main() {
    outColor = texture(diffuse, texCoord0);
}
