#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D inAlbedo;

#lib:guassian

void main() {
    outColor = blur(inAlbedo, horizontal, uv);
}

