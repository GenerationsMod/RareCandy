#version 150 core

#define ambientLight 0.6f

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;
uniform sampler2D layer;
uniform sampler2D mask;

uniform float lightLevel;

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

vec3 screenBlend(vec3 baseColor, vec3 blendColor, float mask, float intensity) {
    return 1.0 - (1.0 - baseColor) * (1.0 - blendColor * mask * intensity);
}

vec4 getColor() {
    vec3 color = texture(diffuse, texCoord0).xyz;
    vec4 layerMasks = adjust(texture(layer, texCoord0));
    vec4 maskColor = adjust(texture(mask, texCoord0));

    vec3 base = mix(vec3(1.0), baseColor1, layerMasks.r);
    base = mix(base, baseColor2, layerMasks.g);
    base = mix(base, baseColor3, layerMasks.b);
    base = mix(base, baseColor4, layerMasks.a);
    base = mix(base, baseColor5, maskColor.r);

    base = screenBlend(base, emiColor1, emiIntensity1, layerMasks.r);
    base = screenBlend(base, emiColor2, emiIntensity2, layerMasks.g);
    base = screenBlend(base, emiColor3, emiIntensity3, layerMasks.b);
    base = screenBlend(base, emiColor4, emiIntensity4, layerMasks.a);
    base = screenBlend(base, emiColor5, emiIntensity5, maskColor.r);

    return vec4(base, 1.0);
}

void main() {
    vec4 color = getColor();
    outColor = vec4(lightLevel, lightLevel, lightLevel, 1.0f) * color;
}
