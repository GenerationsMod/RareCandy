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

vec4 process(vec4 inColor) {
    float grayscale = 0.2126 * inColor.r + 0.7152 * inColor.g + 0.0722 * inColor.b;

    float luminanceDx = dFdx(grayscale);
    float luminanceDy = dFdy(grayscale);
    float edgeFactor = length(vec2(luminanceDx, luminanceDy));

    float outline = 1.0 - smoothstep(0.02, 0.05, edgeFactor);
    vec3 edgeColor = vec3(0.0);

    vec3 finalColor = mix(vec3(grayscale), edgeColor, outline);

    return vec4(finalColor, inColor.a);
}

void main() {
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
    vec2 uv = (pixel + 0.5) / imageSize(solidTex).xy;

    vec4 color = imageLoad(solidTex, storePixel);
    color = process(color);

    float emiAlpha = texture(images, vec3(samplerPixel, emission)).r * color.a;

    imageStore(solidTex, pixel, vec4(color.rgb, emiAlpha));
    imageStore(litTex,   pixel, vec4(color.rgb, emiAlpha));
}