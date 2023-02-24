#version 330 core
#pragma optionNV(strict on)

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 texcoords;
layout (location = 2) in vec3 inNormal;

out vec2 outTexCoords;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

void main() {
    outTexCoords = texcoords;
    vec3 outPos = vec3(modelMatrix * vec4(inPos, 1.0));
    gl_Position = projectionMatrix * viewMatrix * vec4(outPos, 1.0);
}