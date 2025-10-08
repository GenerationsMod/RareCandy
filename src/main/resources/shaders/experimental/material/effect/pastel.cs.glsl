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
    vec2 wrappedUV = fract(texCoord0 * 5.0);

    float gradient = sin(wrappedUV.x * 3.14159) * sin(wrappedUV.y * 3.14159);

    gradient = (gradient + 1.0) * 0.5;

    vec3 pastelBlue = vec3(0.8, 0.9, 1.0);
    vec3 pastelPink = vec3(1.0, 0.8, 0.9);

    vec3 pastelColor = mix(pastelBlue, pastelPink, gradient);

    return vec4(mix(inColor.rgb, pastelColor, 0.5), inColor.a);
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

