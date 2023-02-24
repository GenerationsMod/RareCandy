#version 330 core
#pragma optionNV(strict on)
#define MAX_BONES 220

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 texcoords;
layout (location = 2) in vec3 inNormal;
layout(location = 3) in vec4 inJoints;
layout(location = 4) in vec4 inWeights;

out vec2 outTexCoords;
out vec3 outPos;
out vec3 outNormal;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 boneTransforms[MAX_BONES];

mat4 getBoneTransform() {
    mat4 boneTransform =
    boneTransforms[uint(inJoints.x)] * inWeights.x + // Bone 1 Transform (Bone Transform * Weight)
    boneTransforms[uint(inJoints.y)] * inWeights.y + // Bone 2 Transform (Bone Transform * Weight)
    boneTransforms[uint(inJoints.z)] * inWeights.z + // Bone 3 Transform (Bone Transform * Weight)
    boneTransforms[uint(inJoints.w)] * inWeights.w ; // Bone 4 Transform (Bone Transform * Weight)
    return boneTransform;
}

void main() {
    mat4 transformedModelMatrix = modelMatrix * getBoneTransform();

    outTexCoords = texcoords;
    outPos = vec3(transformedModelMatrix * vec4(inPos, 1.0));
    outNormal = mat3(transformedModelMatrix) * inNormal;

    gl_Position = projectionMatrix * viewMatrix * vec4(outPos, 1.0);
}