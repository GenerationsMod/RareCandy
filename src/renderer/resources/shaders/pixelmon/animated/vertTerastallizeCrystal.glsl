#version 330 core
#pragma optionNV(strict on)
#define MAX_BONES 220

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 texcoords;
layout (location = 2) in vec3 inNormal;
layout (location = 3) in vec4 inJoints;
layout (location = 4) in vec4 inWeights;

out vec3 outReflection;
out vec3 outRefraction;
out float outFresnel;
out vec2 outTexCoords;
out vec4 outStarCoords;
out vec3 outNormal;
out vec3 outPos;
out vec3 incident;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 boneTransforms[MAX_BONES];

vec4 projectionFromPos(vec4 position) {
    vec4 projection = position * 2;
    projection.xy = vec2(projection.x + projection.w, projection.y + projection.w);
    projection.zw = position.zw;
    return projection;
}

mat4 getBoneTransform() {
    mat4 boneTransform =
    boneTransforms[uint(inJoints.x)] * inWeights.x + // Bone 1 Transform (Bone Transform * Weight)
    boneTransforms[uint(inJoints.y)] * inWeights.y + // Bone 2 Transform (Bone Transform * Weight)
    boneTransforms[uint(inJoints.z)] * inWeights.z + // Bone 3 Transform (Bone Transform * Weight)
    boneTransforms[uint(inJoints.w)] * inWeights.w; // Bone 4 Transform (Bone Transform * Weight)
    return boneTransform;
}

void main() {
    mat4 transformedModelMatrix = modelMatrix * getBoneTransform();
    outPos = (transformedModelMatrix * vec4(inPos, 1.0)).xyz;
    vec3 normal = (mat3(transformedModelMatrix) * inNormal);

    vec3 camPos = vec3(inverse(viewMatrix)[3]);
    vec3 cameraForward = vec3(viewMatrix[0][2], viewMatrix[1][2], viewMatrix[2][2]);
    incident = normalize(outPos - camPos - cameraForward);

    outTexCoords = texcoords;
    outNormal = mat3(transformedModelMatrix) * inNormal;
    gl_Position = projectionMatrix * viewMatrix * vec4(outPos, 1.0);
    outStarCoords = projectionFromPos(gl_Position);
}