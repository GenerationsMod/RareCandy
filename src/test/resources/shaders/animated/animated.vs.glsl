#version 450
#define MAX_BONES 200

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoords;
layout(location = 2) in vec3 inNormal;
layout(location = 3) in vec3 inTangent;
layout(location = 3) in vec4 joints;
layout(location = 4) in vec4 weights;

out vec2 texCoord0;
out vec3 toLightVector;
out vec3 toCameraVector;
out vec3 normal;

uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform vec3 lightPosition;

uniform mat4 gBones[MAX_BONES];

mat4 getBoneTransform() {
    mat4 boneTransform =
    gBones[uint(joints.x)] * weights.x + // Bone 1 Transform (Bone Transform * Weight)
    gBones[uint(joints.y)] * weights.y + // Bone 2 Transform (Bone Transform * Weight)
    gBones[uint(joints.z)] * weights.z + // Bone 3 Transform (Bone Transform * Weight)
    gBones[uint(joints.w)] * weights.w ; // Bone 4 Transform (Bone Transform * Weight)
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
    texCoord0 = vec2(inTexCoords.x, inTexCoords.y);
    mat4 worldSpace = projectionMatrix * viewMatrix;
    mat4 modelTransform = modelMatrix * getBoneTransform();
    vec4 worldPosition = modelTransform * vec4(inPosition + getAnimatedPosition(worldSpace), 1.0);

    gl_Position = worldSpace * worldPosition;
    normal = (modelTransform * vec4(inNormal, 0.0)).xyz;
    toLightVector = lightPosition - vec3(worldPosition.x, -5.0, worldPosition.z);
    toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;
}