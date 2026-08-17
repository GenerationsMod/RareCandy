#version 460 core

in vec2 uv;

layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outNormal;
layout(location = 2) out vec4 outEmission;
layout(location = 3) out float outObject;

uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform vec3 cameraPosition;

const float MAJOR_SPACING = 1.0;
const float MINOR_SPACING = MAJOR_SPACING / 8.0;
const float GRID_OFFSET = 0.5;
const float LINE_WIDTH = 0.65;
const float AXIS_WIDTH = 1.15;
const float FADE_START = 8.0;
const float FADE_END = 64.0;

const vec3 GROUND_COLOR = vec3(1.0);

const vec4 MINOR_COLOR = vec4(0.18, 0.24, 0.28, 0.18);
const vec4 MAJOR_COLOR = vec4(0.30, 0.38, 0.44, 0.28);
const vec4 X_AXIS_COLOR = vec4(0.85, 0.24, 0.20, 0.55);
const vec4 Z_AXIS_COLOR = vec4(0.24, 0.48, 0.90, 0.55);

#lib:utils

float lineMask(float pixelDistance, float width) {
    return 1.0 - smoothstep(width, width + 1.0, pixelDistance);
}

float gridLine(vec2 worldPosition, float scale, float width) {
    vec2 coord = (worldPosition - vec2(GRID_OFFSET)) / scale;
    vec2 derivative = max(fwidth(coord), vec2(0.0001));
    vec2 grid = abs(fract(coord - 0.5) - 0.5) / derivative;
    return lineMask(min(grid.x, grid.y), width);
}

float axisLine(float distanceToAxis, float width) {
    float derivative = max(fwidth(distanceToAxis), 0.0001);
    return lineMask(abs(distanceToAxis) / derivative, width);
}

vec4 weighted(vec4 color, float mask) {
    return vec4(color.rgb, color.a * mask);
}

vec4 strongest(vec4 a, vec4 b) {
    return b.a > a.a ? b : a;
}

void main() {
    vec3 nearPoint = worldPosFromDepth(uv, 0.0, inverseProjectionMatrix, inverseViewMatrix);
    vec3 farPoint = worldPosFromDepth(uv, 1.0, inverseProjectionMatrix, inverseViewMatrix);
    vec3 ray = farPoint - nearPoint;

    if (abs(ray.y) < 0.000001) {
        discard;
    }

    float t = -nearPoint.y / ray.y;
    if (t <= 0.0 || t >= 1.0) {
        discard;
    }

    vec3 worldPosition = nearPoint + ray * t;
    vec4 clipPosition = projectionMatrix * viewMatrix * vec4(worldPosition, 1.0);
    float ndcDepth = clipPosition.z / clipPosition.w;
    if (abs(ndcDepth) > 1.0) {
        discard;
    }
    gl_FragDepth = ndcDepth * 0.5 + 0.5;

    vec4 line = weighted(MINOR_COLOR, gridLine(worldPosition.xz, MINOR_SPACING, LINE_WIDTH));
    line = strongest(line, weighted(MAJOR_COLOR, gridLine(worldPosition.xz, MAJOR_SPACING, LINE_WIDTH)));
    line = strongest(line, weighted(X_AXIS_COLOR, axisLine(worldPosition.z, AXIS_WIDTH)));
    line = strongest(line, weighted(Z_AXIS_COLOR, axisLine(worldPosition.x, AXIS_WIDTH)));

    line.a *= 1.0 - smoothstep(FADE_START, FADE_END, distance(cameraPosition, worldPosition));

    outColor = vec4(mix(GROUND_COLOR, line.rgb, line.a), 1.0);
    outNormal = vec4(0.0, 1.0, 0.0, 1.0);
    outEmission = vec4(0, 0.0, 0.0, 0.0);
    outObject = 0.0;
}