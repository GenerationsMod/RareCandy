#version 330 core
#pragma optionNV(strict on)
#define MAX_BONES 220

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 texcoords;
layout (location = 2) in vec3 inNormal;
layout(location = 3) in vec4 inJoints;
layout(location = 4) in vec4 inWeights;

out vec3 outReflection;
out vec3 outRefraction;
out float outFresnel;
out vec2 outTexCoords;
out vec4 outStarCoords;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec3 camPos;
uniform mat4 boneTransforms[MAX_BONES];

// Indices of refraction
const float Air = 1.0;
const float Glass = 1.51714;

// Air to glass ratio of the indices of refraction (Eta)
const float Eta = Air / Glass;

// see http://en.wikipedia.org/wiki/Refractive_index Reflectivity
const float R0 = ((Air - Glass) * (Air - Glass)) / ((Air + Glass) * (Air + Glass));

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
    boneTransforms[uint(inJoints.w)] * inWeights.w ; // Bone 4 Transform (Bone Transform * Weight)
    return boneTransform;
}

void main() {
    mat4 transformedModelMatrix = modelMatrix * getBoneTransform();
    vec4 outPos = transformedModelMatrix * vec4(inPos, 1.0);
    vec3 incident = normalize(vec3(outPos - vec4(camPos, 1.0)));
    vec3 normal = (mat3(transformedModelMatrix) * inNormal);

    outRefraction = refract(incident, normal, Eta);
    outReflection = reflect(incident, normal);

    // see http://en.wikipedia.org/wiki/Schlick%27s_approximation
    outFresnel = R0 + (1.0 - R0) * pow((1.0 - dot(-incident, normal)), 5.0);

    outTexCoords = texcoords;
    gl_Position = projectionMatrix * viewMatrix * outPos;
    outStarCoords = projectionFromPos(gl_Position);
}