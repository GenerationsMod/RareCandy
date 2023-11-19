#version 150 core

#define ambientLight 0.6f

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;
uniform sampler2D layer;

uniform float lightLevel;

//base
uniform vec3 baseColor1;
uniform vec3 baseColor2;
uniform vec3 baseColor3;
uniform vec3 baseColor4;

//emi
uniform vec3 emiColor1;
uniform vec3 emiColor2;
uniform vec3 emiColor3;
uniform vec3 emiColor4;
uniform float emiIntensity1;
uniform float emiIntensity2;
uniform float emiIntensity3;
uniform float emiIntensity4;

vec3 blendMultiply(vec3 base, vec3 blend) {
    return base*blend;
}

vec3 blendMultiply(vec3 base, vec3 blend, float opacity) {
    return (blendMultiply(base, blend) * opacity + base * (1.0 - opacity));
}


vec4 getColor() {
    vec3 color = texture(diffuse, texCoord0).xyz;
    vec4 layer = texture(layer, texCoord0);

    vec3 base = blendMultiply(color, baseColor1, layer.r);
    base = blendMultiply(base, baseColor2, layer.g);
    base = blendMultiply(base, baseColor3, layer.b);
    base = blendMultiply(base, baseColor4, layer.a);

    vec3 emission = blendMultiply(color, blendMultiply(vec3(1.0), emiColor1, emiIntensity1), layer.r);
    emission = blendMultiply(emission, blendMultiply(vec3(1.0), emiColor2, emiIntensity2), layer.g);
    emission = blendMultiply(emission, blendMultiply(vec3(1.0), emiColor3, emiIntensity3), layer.b);
    emission = blendMultiply(emission, blendMultiply(vec3(1.0), emiColor4, emiIntensity4), layer.a);

    return vec4(base, 1.0);
}

void main() {
    vec4 color = getColor();

//    if (color.a < 0.01) discard;

    outColor = vec4(lightLevel, lightLevel, lightLevel, 1.0f) * color;
}
