#version 460 core

out vec4 outColor;

uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;
uniform mat4 viewProjectionMatrix;
uniform vec2 viewportSize;
uniform vec3 cameraPosition;

const float MAJOR_SPACING = 1.0;
const float MINOR_SPACING = MAJOR_SPACING / 8.0;
const float GRID_OFFSET = 0.5;
const float LINE_WIDTH = 0.65;
const float AXIS_WIDTH = 1.15;
const float FADE_START = 8.0;
const float FADE_END = 64.0;

const vec4 MINOR_COLOR = vec4(0.18, 0.24, 0.28, 0.18);
const vec4 MAJOR_COLOR = vec4(0.30, 0.38, 0.44, 0.28);
const vec4 X_AXIS_COLOR = vec4(0.85, 0.24, 0.20, 0.55);
const vec4 Z_AXIS_COLOR = vec4(0.24, 0.48, 0.90, 0.55);

vec3 unproject(float clipDepth) {
    vec2 ndc = (gl_FragCoord.xy / viewportSize) * 2.0 - 1.0;
    vec4 view = inverseProjectionMatrix * vec4(ndc, clipDepth, 1.0);
    view /= view.w;

    vec4 world = inverseViewMatrix * view;
    return world.xyz;
}

float gridLine(vec2 worldPosition, float scale, float width) {
    if (width <= 0.0) {
        return 0.0;
    }

    vec2 coord = (worldPosition - vec2(GRID_OFFSET)) / scale;
    vec2 derivative = max(fwidth(coord), vec2(0.0001));
    vec2 grid = abs(fract(coord - 0.5) - 0.5) / derivative;
    float closestLine = min(grid.x, grid.y);
    return 1.0 - smoothstep(width, width + 1.0, closestLine);
}

float axisLine(float distanceToAxis, float width) {
    if (width <= 0.0) {
        return 0.0;
    }

    float derivative = max(fwidth(distanceToAxis), 0.0001);
    float line = abs(distanceToAxis) / derivative;
    return 1.0 - smoothstep(width, width + 1.0, line);
}

void main() {
    vec3 nearPoint = unproject(-1.0);
    vec3 farPoint = unproject(1.0);
    vec3 ray = farPoint - nearPoint;

    if (abs(ray.y) < 0.000001) {
        discard;
    }

    float t = -nearPoint.y / ray.y;
    if (t <= 0.0 || t >= 1.0) {
        discard;
    }

    vec3 worldPosition = nearPoint + ray * t;
    vec4 clipPosition = viewProjectionMatrix * vec4(worldPosition, 1.0);
    float ndcDepth = clipPosition.z / clipPosition.w;
    if (ndcDepth < -1.0 || ndcDepth > 1.0) {
        discard;
    }
    gl_FragDepth = ndcDepth * 0.5 + 0.5;

    float minorMask = gridLine(worldPosition.xz, MINOR_SPACING, LINE_WIDTH);
    float majorMask = gridLine(worldPosition.xz, MAJOR_SPACING, LINE_WIDTH);
    float xAxisMask = axisLine(worldPosition.z, AXIS_WIDTH);
    float zAxisMask = axisLine(worldPosition.x, AXIS_WIDTH);

    vec3 color = MINOR_COLOR.rgb;
    float alpha = minorMask * MINOR_COLOR.a;

    float majorAlpha = majorMask * MAJOR_COLOR.a;
    if (majorAlpha > alpha) {
        color = MAJOR_COLOR.rgb;
        alpha = majorAlpha;
    }

    float xAxisAlpha = xAxisMask * X_AXIS_COLOR.a;
    if (xAxisAlpha > alpha) {
        color = X_AXIS_COLOR.rgb;
        alpha = xAxisAlpha;
    }

    float zAxisAlpha = zAxisMask * Z_AXIS_COLOR.a;
    if (zAxisAlpha > alpha) {
        color = Z_AXIS_COLOR.rgb;
        alpha = zAxisAlpha;
    }

    float fade = 1.0 - smoothstep(FADE_START, FADE_END, distance(cameraPosition, worldPosition));
    alpha *= fade;

    if (alpha <= 0.001) {
        discard;
    }

    outColor = vec4(color, alpha);
}
