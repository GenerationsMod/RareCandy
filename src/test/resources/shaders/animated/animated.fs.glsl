#version 450 core

in vec2 texCoord0;
in vec3 normal;
in vec3 toLightVector;
in vec3 toCameraVector;

out vec4 outColor;

uniform sampler2D diffuse;

uniform int LIGHT_color;
uniform float LIGHT_shineDamper;
uniform float LIGHT_reflectivity;

const float AMBIENT_LIGHT = 0.6f;

vec3 intToColor() {
    return vec3((LIGHT_color >> 16 & 255) / 255f, (LIGHT_color >> 8 & 255) / 255f, (LIGHT_color & 255) / 255f);
}

void main() {
    vec4 color = texture2D(diffuse, texCoord0);
    if(color.a < 0.1) {
        discard;
    }

    vec3 unitNormal = normalize(normal);
    vec3 unitLightVector = normalize(toLightVector);
    vec3 lightDir = -unitLightVector;
    vec3 unitToCameraVector = normalize(toCameraVector);
    vec3 lightColor = intToColor();

    // Diffuse Lighting
    float rawDiffuse = dot(unitNormal, unitLightVector);
    float diffuse = max(rawDiffuse, AMBIENT_LIGHT);
    vec3 coloredDiffuse = diffuse * lightColor;

    // Specular Lighting
    vec3 reflectedLightDir = reflect(lightDir, unitNormal);
    float rawSpecularFactor = dot(reflectedLightDir, unitToCameraVector);
    float specularFactor = max(rawSpecularFactor, 0.0f);
    float dampedFactor = pow(specularFactor, LIGHT_shineDamper);
    vec3 finalSpecular = dampedFactor * LIGHT_reflectivity * lightColor;

    outColor = color;
}
