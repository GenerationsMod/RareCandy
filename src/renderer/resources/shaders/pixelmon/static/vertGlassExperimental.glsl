#version 330 core
#pragma optionNV(strict on)

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 texcoords;
layout (location = 2) in vec3 inNormal;

out vec3 outReflection;
out vec3 outRefraction;
out float outFresnel;
out vec2 outTexCoords;
out vec4 outStarCoords;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec3 camPos;

// Indices of refraction
const float Air = 1.0;
const float Glass = 1.51714;

// Air to glass ratio of the indices of refraction (Eta)
const float Eta = Air / Glass;

// see http://en.wikipedia.org/wiki/Refractive_index Reflectivity
const float R0 = ((Air - Glass) * (Air - Glass)) / ((Air + Glass) * (Air + Glass));

vec4 projectionFromPos(vec4 position) {
    vec4 projection = position * 2;
    projection.xy = vec2(projection.x + projection.w, projection.y + projection.w);
    projection.zw = position.zw;
    return projection;
}

void main() {
    vec4 outPos = modelMatrix * vec4(inPos, 1.0);
    vec3 incident = normalize(vec3(outPos - vec4(camPos, 1.0)));
    vec3 normal = (mat3(modelMatrix) * inNormal);

    outRefraction = refract(incident, normal, Eta);
    outReflection = reflect(incident, normal);

    // see http://en.wikipedia.org/wiki/Schlick%27s_approximation
    outFresnel = R0 + (1.0 - R0) * pow((1.0 - dot(-incident, normal)), 5.0);

    outTexCoords = texcoords;
    gl_Position = projectionMatrix * viewMatrix * outPos;
    outStarCoords = projectionFromPos(gl_Position);
}