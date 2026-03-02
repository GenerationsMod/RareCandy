#version 330 core
#define MAX_BONES 220
#define MINECRAFT_LIGHT_POWER   (0.6)
#define MINECRAFT_AMBIENT_LIGHT (0.4)

layout(location = 0) in vec3 positions;
layout(location = 1) in vec2 texcoords;
layout(location = 2) in vec3 normals;
layout(location = 3) in vec4 tangents;
layout(location = 4) in vec4 joints;
layout(location = 5) in vec4 weights;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec3 fragViewDir;
out vec3 worldPos;
flat out vec3 fragNormal;
flat out vec3 fragTangent;
flat out vec3 fragBitangent;

uniform bool dynamicVertexColor;

uniform int FogShape;

uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat3 normalMatrix;
uniform mat4 projectionMatrix;
uniform vec2 uvOffset;
uniform vec2 uvScale;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

uniform mat4 boneTransforms[MAX_BONES];

mat4 getBoneTransform() {
    mat4 boneTransform =
    boneTransforms[uint(joints.x)] * weights.x + // Bone 1 Transform (Bone Transform * Weight)
    boneTransforms[uint(joints.y)] * weights.y + // Bone 2 Transform (Bone Transform * Weight)
    boneTransforms[uint(joints.z)] * weights.z + // Bone 3 Transform (Bone Transform * Weight)
    boneTransforms[uint(joints.w)] * weights.w ; // Bone 4 Transform (Bone Transform * Weight)
    return boneTransform;
}

float fog_distance(mat4 modelViewMat, vec3 pos, int shape) {
    if (shape == 0) {
        return length((modelViewMat * vec4(pos, 1.0)).xyz);
    } else {
        float distXZ = length((modelViewMat * vec4(pos.x, 0.0, pos.z, 1.0)).xyz);
        float distY = length((modelViewMat * vec4(0.0, pos.y, 0.0, 1.0)).xyz);
        return max(distXZ, distY);
    }
}

vec4 getVertexColor(vec3 litNormal) {
    if(dynamicVertexColor) return vec4(1);

    vec3 lightDir0 = normalize(Light0_Direction);
    vec3 lightDir1 = normalize(Light1_Direction);
    float light0 = max(0.0, dot(lightDir0, litNormal));
    float light1 = max(0.0, dot(lightDir1, litNormal));
    float lightAccum = min(1.0, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT);
    return vec4(lightAccum, lightAccum, lightAccum, 1);
}

void main() {
    mat4 boneTransform = getBoneTransform();
    mat4 modelTransform = modelMatrix * boneTransform;
    mat4 worldSpace = projectionMatrix * viewMatrix;
    mat4 modelView = viewMatrix * modelTransform;
    vec4 worldPosition = modelTransform * vec4(positions, 1.0);

    mat3 boneMatrix = mat3(boneTransform);
    vec3 skinnedNormal = normalize(boneMatrix * normals);
    vec3 skinnedTangent = normalize(boneMatrix * tangents.xyz);

    fragNormal    = normalize(normalMatrix * skinnedNormal);
    fragTangent   = normalize(normalMatrix * skinnedTangent);
    fragTangent   = normalize(fragTangent - dot(fragTangent, fragNormal) * fragNormal);
    fragBitangent = cross(fragNormal, fragTangent) * tangents.w;

    texCoord0 = (texcoords * uvScale) + uvOffset;
    gl_Position = projectionMatrix * modelView * vec4(positions, 1.0);
    vertexDistance = fog_distance(modelView, positions, FogShape);
    vertexColor = getVertexColor(fragNormal);
}
