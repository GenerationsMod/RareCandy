#version 450

layout(location = 0) in vec3 positions;
layout(location = 1) in vec2 texcoords;

out vec2 texCoord0;

uniform mat4 modelMatrix;

void main() {
    mat4 worldSpace = projectionMatrix * viewMatrix;
    vec4 worldPosition = modelMatrix * vec4(positions, 1.0);

    texCoord0 = texcoords;
    gl_Position = modelMatrix * vec4(positions, 1.0);
}