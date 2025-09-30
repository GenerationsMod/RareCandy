#version 430
layout(local_size_x = 16, local_size_y = 16) in;

layout(rgba8, binding = 0) uniform image2D solidTex;
layout(rgba8, binding = 1) uniform image2D litTex;

uniform sampler2DArray images;

layout(std140, binding = 0) uniform material {
    vec3 baseColor1;
    vec3 baseColor2;
    vec3 baseColor3;
    vec3 baseColor4;
    vec3 baseColor5;
    vec3 emiColor1;
    vec3 emiColor2;
    vec3 emiColor3;
    vec3 emiColor4;
    vec3 emiColor5;
    float emiIntensity1;
    float emiIntensity2;
    float emiIntensity3;
    float emiIntensity4;
    float emiIntensity5;
    int diffuse;
    int emission;
    int layer;
    int mask;
};

uniform sampler2D solidTexSampler;

const float edgeThreshold = 0.08;
const float blockSize = 0.0015;
const int bilateralIterations = 2;

const vec2 offsets[8] = vec2[](
vec2(-1.0, -1.0), vec2(0.0, -1.0), vec2(1.0, -1.0),
vec2(-1.0,  0.0),                  vec2(1.0,  0.0),
vec2(-1.0,  1.0), vec2(0.0,  1.0), vec2(1.0,  1.0)
);

float detectEdge(vec2 uv) {
    vec3 center = texture(solidTexSampler, uv).rgb;
    float centerIntensity = dot(center, vec3(0.2126, 0.7152, 0.0722));

    float diffSum = 0.0;
    for (int i = 0; i < 8; i++) {
        vec2 offsetUV = uv + offsets[i] * blockSize;
        vec3 neighbor = texture(solidTexSampler, offsetUV).rgb;
        float neighborIntensity = dot(neighbor, vec3(0.2126, 0.7152, 0.0722));
        diffSum += abs(centerIntensity - neighborIntensity);
    }

    return smoothstep(edgeThreshold - 0.02, edgeThreshold + 0.02, diffSum / 8.0);
}

vec3 bilateralFilter(vec2 uv) {
    vec3 centerColor = texture(solidTexSampler, uv).rgb;
    vec3 colorSum = vec3(0.0);
    float weightSum = 0.0;

    for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            vec2 offsetUV = uv + vec2(float(i), float(j)) * 0.003;
            vec3 sampleColor = texture(solidTexSampler, offsetUV).rgb;

            float spatialWeight = exp(-float(i * i + j * j) / (2.0 * 1.0));
            float colorWeight = exp(
            -dot(sampleColor - centerColor, sampleColor - centerColor) / (2.0 * 0.05)
            );

            float weight = spatialWeight * colorWeight;
            colorSum += sampleColor * weight;
            weightSum += weight;
        }
    }

    return colorSum / weightSum;
}

void main() {
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
    vec2 uv = (pixel + 0.5) / imageSize(solidTex);

    vec4 originalColor = texture(solidTexSampler, uv);

    float edge = detectEdge(uv);
    vec3 filtered = originalColor.rgb;

    for (int i = 0; i < bilateralIterations; i++) {
        filtered = bilateralFilter(uv);
    }

    vec3 color = mix(filtered, originalColor.rgb, edge);

    float emiAlpha = texture(images, vec3(uv, emission)).r * color.a;

    imageStore(solidTex, pixel, color);
    imageStore(litTex, pixel, vec4(color.rgb, emiAlpha));
}