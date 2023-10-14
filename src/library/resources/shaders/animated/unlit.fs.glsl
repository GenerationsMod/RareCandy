#version 450 core
#define ambientLight 0.6f

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;

void main() {
    vec4 color = texture2D(diffuse, texCoord0);
    outColor = color;
}
