#version 450
#define MAX_BONES 200

layout(location = 0) in vec3 positions;
layout(location = 1) in vec2 texcoords;
layout(location = 2) in vec3 normals;
layout(location = 3) in vec4 tangents;
layout(location = 3) in vec4 joints;
layout(location = 4) in vec4 weights;

out vec2 texCoord0;
out vec3 toLightVector; // TODO: take mc's light direction uniforms and normalise and inverse them to create this
out vec3 toCameraVector;
out vec3 normal;

uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform vec3 lightPosition;

uniform mat4 boneTransforms[MAX_BONES];

void main() {
    mat4 skinMatrix =
    boneTransforms[uint(joints.x)] * weights.x + // Bone 1 Transform (Bone Transform * Weight)
    boneTransforms[uint(joints.y)] * weights.y + // Bone 2 Transform (Bone Transform * Weight)
    boneTransforms[uint(joints.z)] * weights.z + // Bone 3 Transform (Bone Transform * Weight)
    boneTransforms[uint(joints.w)] * weights.w ; // Bone 4 Transform (Bone Transform * Weight)

    vec3 outPosition = (skinMatrix * vec4(position, 1.0)).xyz;
    mat4 worldSpace = projectionMatrix * viewMatrix;
    vec4 worldPosition = modelMatrix * vec4(outPosition, 1.0);

    texCoord0 = vec2(inTexCoords.x, inTexCoords.y);
    gl_Position = worldSpace * worldPosition;
    normal = (modelMatrix * vec4(inNormal, 0.0)).xyz;
    toLightVector = lightPosition - vec3(worldPosition.x, -5.0, worldPosition.z);
    toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;
}