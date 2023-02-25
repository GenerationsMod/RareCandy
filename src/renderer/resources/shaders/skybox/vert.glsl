#version 330 core
#pragma optionNV(strict on)

layout (location = 0) in vec3 inPos;

out vec3 outTextureDir;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {
    outTextureDir = inPos;
    gl_Position = projectionMatrix * viewMatrix * vec4(inPos, 1.0);
}