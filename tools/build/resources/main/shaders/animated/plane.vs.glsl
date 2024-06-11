#version 150 core
#extension GL_ARB_explicit_uniform_location : enable
#extension GL_ARB_explicit_attrib_location : enable

layout(location = 0) in vec3 positions;
layout(location = 1) in vec2 texcoords;

out vec2 texCoord0;

uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;

void main() {
    mat4 worldSpace = projectionMatrix * viewMatrix;
    mat4 modelTransform = modelMatrix;
    vec4 worldPosition = modelTransform * vec4(positions, 1.0);

    texCoord0 = texcoords;
    gl_Position = worldSpace * worldPosition;

}