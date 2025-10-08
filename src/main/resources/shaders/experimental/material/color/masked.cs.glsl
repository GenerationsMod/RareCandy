#version 430
layout(local_size_x = 16, local_size_y = 16) in;

uniform sampler2DArray images;
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
    int diffuse;
    int emission;
    int layer;
    int mask;
};

void main() {
    ivec2 storePixel = ivec2(gl_GlobalInvocationID.xy);
    vec2 samplerPixel = storePixel / textureSize(images, 0).xy;

    vec4 color = texture(images, vec3(samplerPixel, diffuse));
    float maskColor = texture(images, vec3(samplerPixel, mask)).r;
    float emiAlpha = texture(images, vec3(samplerPixel, emission)).r * color.a;

    color.rgb = mix(color.rgb, color.rgb * baseColor1, maskColor);

    imageStore(solidTex, storePixel, color);
}

