#version 450 core
#define MAX_BONES 200
in vec3 inPosition;
in vec2 inTexCoord;
in vec3 inNormal;
in vec3 inTangent;

in vec4 boneDataA;
in vec4 boneDataB;

out vec2 texCoord0;
out vec3 toLightVector;
out vec3 toCameraVector;
out vec3 normal;

uniform mat4 MC_view;
uniform mat4 MC_model;
uniform mat4 MC_projection;
uniform vec3 LIGHT_pos;

uniform mat4 gBones[200];

// Got no clue how this works, honestly.
mat4 getBoneTransform() {
    mat4 boneTransform =
    gBones[uint(boneDataA.x)] * boneDataA.z +
    gBones[uint(boneDataA.y)] * boneDataA.w +
    gBones[uint(boneDataB.x)] * boneDataB.z +
    gBones[uint(boneDataB.y)] * boneDataB.w ;
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
    texCoord0 = inTexCoord;
    mat4 worldSpace = MC_projection * MC_view;
    mat4 modelTransform = MC_model * getBoneTransform();
    vec4 worldPosition = modelTransform * vec4(inPosition + getAnimatedPosition(worldSpace), 1.0);

    gl_Position = worldSpace * worldPosition;
    normal = (modelTransform * vec4(inNormal, 0.0)).xyz;
    toLightVector = LIGHT_pos - worldPosition.xyz;
    toCameraVector = (inverse(MC_view) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;
}