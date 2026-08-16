#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D inAlbedo;
uniform sampler2D inDepth;
uniform mat4 inverseProjectionMatrix;

#lib:fog
#lib:utils

void main() {
    float depth = texture(inDepth, uv).r;
    vec3 pos = viewPosFromDepth(uv, depth, inverseProjectionMatrix);

    float vertexDistance = fog_distance(pos, FogShape);

    outColor = linear_fog(texture(inAlbedo, uv), vertexDistance, FogStart, FogEnd, FogColor);
}

