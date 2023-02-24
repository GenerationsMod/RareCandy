#version 330 core
#pragma optionNV(strict on)

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 texcoords;
layout (location = 2) in vec3 inNormal;

out vec2 outTexCoords;
out vec3 toLightVector;
out vec3 toCameraVector;
out vec3 outNormal;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

uniform vec3 lightPosition;

void main() {
    outTexCoords = texcoords;
    vec4 outPos = vec3(modelMatrix * vec4(inPos, 1.0));
    outNormal = mat3(modelMatrix) * inNormal;
    toLightVector = lightPosition - outPos.xyz;
    toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - outPos.xyz;

    gl_Position = projectionMatrix * viewMatrix * outPos;
}