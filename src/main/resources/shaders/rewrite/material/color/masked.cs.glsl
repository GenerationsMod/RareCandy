#version 430
layout(local_size_x = 16, local_size_y = 16) in;

layout(rgba8, binding = 0) uniform image2D solidTex;

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
    sampler2D diffuse;
    sampler2D emission;
    sampler2D layer;
    sampler2D mask;
};

vec4 getColor(sampler2D sampler, ivec2 storePixel) {
    return texture(sampler, storePixel / textureSize(sampler, 0));
}

void main() {
    ivec2 storePixel = ivec2(gl_GlobalInvocationID.xy);
    vec2 samplerPixel = storePixel / textureSize(images, 0).xy;

    vec4 color = getColor(diffuse , storePixel);
    float maskColor = getColor(mask, storePixel).r;
    float emiAlpha = getColor(emission, storePixel).r * color.a;

    color.rgb = mix(color.rgb, color.rgb * baseColor1, maskColor);

    imageStore(solidTex, storePixel, color);
}

