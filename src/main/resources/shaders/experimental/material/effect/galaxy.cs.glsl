#version 430
layout(local_size_x = 16, local_size_y = 16) in;

const float darkenFactor = 0.3;

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

vec4 process(vec4 color) {
    float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));

    const vec3 lightGradientColor1 = vec3(0.2, 0.0, 0.3);
    const vec3 lightGradientColor2 = vec3(0.6, 0.1, 0.7);
    const float gradientThreshold = 0.5;

    vec3 gradientColor = mix(
        lightGradientColor1,
        lightGradientColor2,
        smoothstep(gradientThreshold, 1.0, brightness)
    );

    color.rgb *= darkenFactor;
        color.rgb = mix(
        color.rgb,
        gradientColor,
        smoothstep(gradientThreshold, 1.0, brightness)
    );

    return color;
}

void main() {
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
    vec2 uv = (pixel + 0.5) / imageSize(solidTex).xy;

    vec4 color = imageLoad(solidTex, storePixel);
    color = process(color);

    float emiAlpha = texture(images, vec3(uv, emission)).r * color.a;
    imageStore(solidTex, pixel, color);
    imageStore(litTex, pixel, vec4(color.rgb, emiAlpha));
}
