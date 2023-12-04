#version 150 core

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;
uniform sampler2D layer;
uniform sampler2D mask;
uniform sampler2D emission;

uniform float lightLevel;
uniform bool useLight;

//base
uniform vec3 baseColor1;
uniform vec3 baseColor2;
uniform vec3 baseColor3;
uniform vec3 baseColor4;
uniform vec3 baseColor5;

//emi
uniform vec3 emiColor1;
uniform vec3 emiColor2;
uniform vec3 emiColor3;
uniform vec3 emiColor4;
uniform vec3 emiColor5;
uniform float emiIntensity1;
uniform float emiIntensity2;
uniform float emiIntensity3;
uniform float emiIntensity4;
uniform float emiIntensity5;


vec4 adjust(vec4 color) {
    color.r = clamp(color.r * 2, 0.0, 1.0);
    color.g = clamp(color.g * 2, 0.0, 1.0);
    color.b = clamp(color.b * 2, 0.0, 1.0);
    color.a = clamp(color.a * 2, 0.0, 1.0);

    return color;
}

vec3 applyEmission(vec3 base, vec3 emissionColor, float intensity) {
    return base + (emissionColor - base) * intensity;
}

vec4 getColor() {
    vec4 color = texture(diffuse, texCoord0);
    vec4 layerMasks = adjust(texture(layer, texCoord0));
    vec4 maskColor = adjust(texture(mask, texCoord0));

    vec3 base = mix(color.rgb, color.rgb * baseColor1, layerMasks.r);
    base = mix(base, color.rgb * baseColor2, layerMasks.g);
    base = mix(base, color.rgb * baseColor3, layerMasks.b);
    base = mix(base, color.rgb * baseColor4, layerMasks.a);
    base = mix(base, color.rgb * baseColor5, maskColor.r);

    base = mix(base, applyEmission(base, emiColor1, emiIntensity1), layerMasks.r);
    base = mix(base, applyEmission(base, emiColor2, emiIntensity2), layerMasks.g);
    base = mix(base, applyEmission(base, emiColor3, emiIntensity3), layerMasks.b);
    base = mix(base, applyEmission(base, emiColor4, emiIntensity4), layerMasks.a);
    base = mix(base, applyEmission(base, emiColor5, emiIntensity5), maskColor.r);

    return vec4(base, color.a);
}

void main() {
    outColor = getColor();

    if(useLight) outColor.xyz *= max(texture(emission, texCoord0).r, lightLevel);
}
