#version 450 core
#define MAX_BONES 200
layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;
layout(location = 2) in vec3 inNormal;
layout(location = 3) in vec3 inTangent;

layout(location = 4) in vec4 boneDataA;
layout(location = 5) in vec4 boneDataB;

out vec2 texCoord0;
out vec3 toLightVector;
out vec3 toCameraVector;
out vec3 normal;

uniform mat4 MC_view;
uniform mat4 MC_model;
uniform mat4 MC_projection;
uniform vec3 LIGHT_pos;

uniform mat4 gBones[200];

layout(std430, binding = 1) readonly restrict buffer TestStorageBuffer {
    float testFloat;
} testStorageBuffer;

mat4 getBoneTransform() {
    mat4 boneTransform =
    gBones[uint(boneDataA.x)] * boneDataA.z + // Bone 1 Transform (Bone Transform * Weight)
    gBones[uint(boneDataA.y)] * boneDataA.w + // Bone 2 Transform (Bone Transform * Weight)
    gBones[uint(boneDataB.x)] * boneDataB.z + // Bone 3 Transform (Bone Transform * Weight)
    gBones[uint(boneDataB.y)] * boneDataB.w ; // Bone 4 Transform (Bone Transform * Weight)
    return boneTransform;
}

// Same for this.
vec3 getAnimatedPosition(mat4 worldSpace) {
    vec3 worldspaceNormal = normalize((worldSpace * vec4(inNormal, 2.0)).xyz);
    vec3 worldspaceTangent = normalize((worldSpace * vec4(inTangent, 2.0)).xyz);
    vec3 dotTangent = normalize(worldspaceTangent - dot(worldspaceTangent, worldspaceNormal) * worldspaceNormal);

    vec3 biTangent = cross(worldspaceTangent, worldspaceNormal);

    return vec3(worldspaceTangent * dotTangent * biTangent * worldspaceNormal);
}

void main() {
    texCoord0 = vec2(inTexCoord.x, inTexCoord.y);
    mat4 worldSpace = MC_projection * MC_view;
    mat4 modelTransform = MC_model * getBoneTransform();
    vec4 worldPosition = modelTransform * vec4(inPosition + getAnimatedPosition(worldSpace), 1.0);

    gl_Position = worldSpace * worldPosition;
    normal = (modelTransform * vec4(inNormal, 0.0)).xyz;
    toLightVector = LIGHT_pos - vec3(worldPosition.x, -5.0, worldPosition.z);
    toCameraVector = (inverse(MC_view) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;
}