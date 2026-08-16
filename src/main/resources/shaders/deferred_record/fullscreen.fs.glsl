#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D sceneTexture;

void main() {
    outColor = texture(sceneTexture, uv);
}