#version 150 core

#define ambientLight 0.6f

#define size 100.0

in vec2 texCoord0;

out vec4 outColor;

uniform float lightLevel;

uniform float radius;

uniform bool render;

float smoothLightDropOff(float distance, float startDistance, float endDistance, float initialIntensity, float finalIntensity) {
    float dropOffFactor = smoothstep(startDistance, endDistance, distance);
    return mix(initialIntensity, finalIntensity, dropOffFactor);
}

void main() {
    if(render) discard;

    vec3 black = vec3(0.065, 0.305, 0.418);
    vec3 white = vec3(0.018, 0.162, 0.235);
    vec3 color = black;

    vec2 uv = 2.0 * texCoord0.xy;
    color = vec3(uv, 0.0);

    float boardSize = 50.0;
    uv = uv * boardSize;
    color = vec3(uv, 0.0);

    vec2 gridUv = fract(uv);
    vec2 gridId = floor(uv);
    color = vec3(gridUv, 0.0);
    color = vec3(gridId, 0.0);
    color = vec3(gridId * 0.2, 0.0);

    if (mod(gridId.x + gridId.y, 2.0) <= 0.01) {
        color = white;
    } else {
        color = black;
    }

    float halfRadius = radius/2/size;

    float pct = smoothLightDropOff(distance(texCoord0,vec2(0.5)), halfRadius, halfRadius+(2/size), lightLevel, 0.0);

    outColor = vec4(color, pct);
}