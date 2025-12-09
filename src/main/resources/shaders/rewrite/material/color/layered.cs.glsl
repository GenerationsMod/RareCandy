 #version 430
layout(local_size_x = 16, local_size_y = 16) in;

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

layout(rgba8, binding = 0) uniform image2D solidTex;

vec4 adjust(vec4 color) {
    return clamp(color * 2, 0, 1);
}

float adjustScalar(float color) {
    return clamp(color * 2, 0.0, 1.0);
}

vec3 applyEmission(vec3 base, vec3 emissionColor, float intensity) {
    return base + (emissionColor - base) * intensity;
}

 vec4 getColor(sampler2D sampler, ivec2 storePixel) {
     return texture(sampler, (storePixel + 0.5) / textureSize(sampler, 0));
 }

 void main() {
    ivec2 storePixel = ivec2(gl_GlobalInvocationID.xy);

    vec4 color = getColor(diffuse, storePixel);
    vec4 layerMasks = adjust(getColor(layer, storePixel));
    float maskColor = adjustScalar(texture(mask, storePixel).r);

    vec3 base = mix(color.rgb, color.rgb * baseColor1, layerMasks.r);
    base = mix(base, color.rgb * baseColor2, layerMasks.g);
    base = mix(base, color.rgb * baseColor3, layerMasks.b);
    base = mix(base, color.rgb * baseColor4, layerMasks.a);
    base = mix(base, color.rgb * baseColor5, maskColor);

    base = mix(base, applyEmission(base, emiColor1, emiIntensity1), layerMasks.r);
    base = mix(base, applyEmission(base, emiColor2, emiIntensity2), layerMasks.g);
    base = mix(base, applyEmission(base, emiColor3, emiIntensity3), layerMasks.b);
    base = mix(base, applyEmission(base, emiColor4, emiIntensity4), layerMasks.a);
    base = mix(base, applyEmission(vec3(0), emiColor5, emiIntensity5), maskColor);

    imageStore(solidTex, storePixel, vec4(base, color.a));
}
