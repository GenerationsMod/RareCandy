#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D inAlbedo;
uniform sampler2D inObject;

void main() {
    outColor = mix(vec4(0), texture(inAlbedo, uv), texture(inObject, uv).r);
}

