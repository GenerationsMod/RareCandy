#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D inAlbedo;
uniform sampler2D inNormal;
uniform sampler2D inEmissive;
uniform sampler2D inDepth;

uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;


#lib:utils

ivec2 adjustLight(ivec2 lightCoord) {
    vec3 pos = worldPosFromDepth(uv, texture(inDepth, uv).r, inverseProjectionMatrix, inverseViewMatrix) + 0.5;
    pos = vec3(floor(pos.x), floor(pos.y), floor(pos.z));
    int dist = int(length(pos));

    ivec2 lit = ivec2(lightCoord);
    lit.y = clamp(15 - dist, 0, 15);
    return lit;
}


#lib:light


void main() {
    outColor = texture(inAlbedo, uv) * getVertexColor(texture(inNormal, uv).xyz);

    float emission = texture(inEmissive, uv).r;

    outColor = applyLight(outColor, int(emission * 15));
}