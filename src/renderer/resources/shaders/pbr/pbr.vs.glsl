#version 330 core

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 texcoords;
layout (location = 2) in vec3 inNormal;

out vec2 outTexCoords;
out vec3 outPos;
out vec3 outNormal;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

void main() {
    outTexCoords = texcoords;
    outPos = vec3(modelMatrix * vec4(inPos, 1.0));
    outNormal = mat3(modelMatrix) * inNormal;

    gl_Position = projectionMatrix * viewMatrix * vec4(outPos, 1.0);
}