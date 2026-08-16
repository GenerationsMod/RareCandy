#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D inAlbedo;
uniform sampler2D inNormal;
uniform sampler2D inEmissive;

#lib:light

void main() {
    outColor = texture(inAlbedo, uv) * getVertexColor(texture(inNormal, uv).xyz);

    float emission = texture(inEmissive, uv).r;

    outColor = applyLight(outColor, int(emission * 15));
}