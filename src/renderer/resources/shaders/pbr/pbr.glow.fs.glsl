#version 450 core

in vec2 outTexCoords;

out vec4 outColor;

uniform sampler2D diffuse;

void main() {
    outColor = texture(diffuse, outTexCoords);
}
