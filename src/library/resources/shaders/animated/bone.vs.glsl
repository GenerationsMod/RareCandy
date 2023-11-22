#version 150 core
#extension GL_ARB_explicit_uniform_location : enable
#extension GL_ARB_explicit_attrib_location : enable

#define MAX_BONES 220

layout(location = 0) in int boneId;

uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;

uniform mat4 boneTransforms[MAX_BONES];

mat4 getBoneTransform() {
    return boneTransforms[boneId];
}

void main() {
    mat4 worldSpace = projectionMatrix * viewMatrix;
    mat4 modelTransform = modelMatrix;// * getBoneTransform();
    vec4 worldPosition = modelTransform * vec4(0, boneId, 0, 1.0);
    gl_Position = worldSpace * worldPosition;
}