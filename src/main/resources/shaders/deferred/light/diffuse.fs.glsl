#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D inAlbedo;
uniform sampler2D inNormal;
uniform sampler2D inDepth;

uniform vec3 lightPos;
uniform vec3 lightColor;
uniform float lightRange;
uniform vec3 ambientColor;
uniform float shininess;

uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;

#lib:projection

void main() {
    float depth = texture(inDepth, uv).r;
    vec4 albedo = texture(inAlbedo, uv);
    vec3 rawNormal = texture(inNormal, uv).xyz;

    // no surface here - pass the buffer through instead of shading a null normal
    if (depth >= 1.0 || dot(rawNormal, rawNormal) < 1e-6) {
        outColor = albedo;
        return;
    }

    vec3 pos = worldPosFromDepth(uv, depth, inverseProjectionMatrix, inverseViewMatrix);

    vec3 N = normalize(rawNormal);
    vec3 L = normalize(lightPos - pos);
    vec3 V = normalize(inverseViewMatrix[3].xyz - pos);
    vec3 H = normalize(L + V);

    vec3 base = albedo.rgb;
    vec3 light = lightColor;
    vec3 amb = ambientColor;

    float att = pow(max(1.0 - distance(lightPos, pos) / max(lightRange, 1e-4), 0.0), 2.0);

    float ndotl = max(dot(N, L), 0.0);

    float s = max(shininess, 1.0);
    float spec = pow(max(dot(N, H), 0.0), s) * (s + 8.0) / 8.0;

    vec3 color = amb * base
    + att * ndotl * light * base
    + att * ndotl * spec * light;

    outColor = vec4(color, albedo.a);
}