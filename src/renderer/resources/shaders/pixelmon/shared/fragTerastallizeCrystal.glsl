#version 330 core
#pragma optionNV(strict on)

in vec2 outTexCoords;
in vec4 outStarCoords;
in vec3 outNormal;
in vec3 outPos;
in vec3 incident;

out vec4 outColor;

uniform samplerCube cubemap;
uniform sampler2D diffuse;
uniform sampler2D normalMap;

uniform float underlyingTexCoordMix;

// Indices of refraction
const float Air = 1.0;
const float Crystal = 1.333;
const float Eta = Air / Crystal;
const float R0 = ((Air - Crystal) * (Air - Crystal)) / ((Air + Crystal) * (Air + Crystal));

vec3 getNormalFromMap() {
    vec3 tangentNormal = texture(normalMap, outTexCoords).xyz * 4.0 - 1.0;

    vec3 Q1  = dFdx(outPos);
    vec3 Q2  = dFdy(outPos);
    vec2 st1 = dFdx(outTexCoords);
    vec2 st2 = dFdy(outTexCoords);

    vec3 N   = normalize(outNormal);
    vec3 T  = normalize(Q1*st2.t - Q2*st1.t);
    vec3 B  = -normalize(cross(N, T));
    mat3 TBN = mat3(T, B, N);

    return normalize(TBN * tangentNormal);
}

void main() {
    vec3 normal = getNormalFromMap();
    vec3 outRefraction = refract(incident, normal, Eta);
    vec3 outReflection = reflect(incident, normal);

    // see http://en.wikipedia.org/wiki/Schlick%27s_approximation
    float outFresnel = R0 + (1.0 - R0) * pow((1.0 - dot(-incident, normal)), 5.0);

    vec4 underlyingColor = texture(diffuse, outTexCoords);

    vec4 refractionColor = texture(cubemap, normalize(outRefraction)) * 1.2;
    vec4 reflectionColor = texture(cubemap, normalize(outReflection)) * 5;

    outColor = mix(mix(refractionColor, reflectionColor, outFresnel), underlyingColor, underlyingTexCoordMix);
}
