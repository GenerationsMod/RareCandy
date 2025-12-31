#version 420 core

in vec2 texCoord0;
in vec4 vertexColor;
in float vertexDistance;

out vec4 outColor;

uniform vec4 ColorModulator;

//fog
layout(std140, binding = 0) uniform Fog {
    vec4 FogColor;
    float FogStart;
    float FogEnd;
    int FogShape;
};

uniform sampler2D tex;

uniform sampler2D lightmap;

uniform ivec2 light;

uniform vec3 tint;

#define MINECRAFT_LIGHT_POWER   (0.6)
#define MINECRAFT_AMBIENT_LIGHT (0.4)

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

// ===== Lighting Method =====

vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
    return texture(lightMap, (vec2(uv) + 0.5) / 16.0);
}

in vec3 fragViewDir;
in vec3 worldPos;

void main() {
    outColor = texture(solid, texCoord0) * ColorModulator;

    if (outColor.a < 0.004) {
        discard;
    }

    outColor.rgb *= tint;

    outColor *= vertexColor;
    // Sample Minecraft's light level from the lightmap texture
    vec4 minecraftLight = minecraft_sample_lightmap(lightmap, light);
    outColor *= mix(minecraftLight, vec4(1, 1, 1, 1), texture(images, vec3(texCoord0, emission)).r);

    outColor = linear_fog(outColor, vertexDistance, FogStart, FogEnd, FogColor);
}