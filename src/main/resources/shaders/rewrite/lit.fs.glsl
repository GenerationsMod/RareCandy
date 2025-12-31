#version 420 core

in float vertexDistance;
in vec2 texCoord0;

out vec4 outColor;

uniform vec4 ColorModulator;
uniform sampler2D tex;
uniform vec3 tint;

//fog
layout(std140, binding = 0) uniform Fog {
    vec4 FogColor;
    float FogStart;
    float FogEnd;
    int FogShape;
};

vec4 linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
    if (vertexDistance <= fogStart) {
        return inColor;
    }

    float fogValue = vertexDistance < fogEnd ? smoothstep(fogStart, fogEnd, vertexDistance) : 1.0;
    return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a);
}

float linear_fog_fade(float vertexDistance, float fogStart, float fogEnd) {
    if (vertexDistance <= fogStart) {
        return 1.0;
    } else if (vertexDistance >= fogEnd) {
        return 0.0;
    }

    return smoothstep(fogEnd, fogStart, vertexDistance);
}


void main() {
    outColor = texture(tex, texCoord0) * ColorModulator;

    if (outColor.a < 0.004) {
        discard;
    }

    outColor.rgb *= tint;

    outColor = linear_fog(outColor, vertexDistance, FogStart, FogEnd, FogColor);
}