#version 430
layout(local_size_x = 16, local_size_y = 16) in;

layout(rgba8, binding = 0) uniform image2D solidTex;
layout(rgba8, binding = 1) uniform image2D litTex;

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

void main() {
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);

    vec4 color = imageLoad(solidTex, pixel);

    float emiAlpha = texture(images, vec3(samplerPixel, emission)).r * color.a;

    imageStore(solidTex, storePixel, vec4(color.rgb, emiAlpha));
    imageStore(litTex,   storePixel, vec4(color.rgb, emiAlpha));
}

