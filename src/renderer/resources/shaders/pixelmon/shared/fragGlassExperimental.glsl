#version 330 core
#pragma optionNV(strict on)

in vec3 outReflection;
in vec3 outRefraction;
in float outFresnel;
in vec2 outTexCoords;
in vec4 outStarCoords;

out vec4 outColor;

uniform samplerCube cubemap;
uniform sampler2D diffuse;
uniform sampler2D stars;
uniform float underlyingTexCoordMix;

void main() {
    vec4 underlyingColor = texture(diffuse, outTexCoords);
    vec4 starColor = textureProj(stars, outStarCoords);
    underlyingColor = mix(underlyingColor, starColor, starColor.a);

    vec4 refractionColor = texture(cubemap, normalize(outRefraction));
    vec4 reflectionColor = texture(cubemap, normalize(outReflection));

    outColor = mix(mix(refractionColor, reflectionColor, outFresnel), underlyingColor, underlyingTexCoordMix);
}
