#version 450
#define MAX_BONES 200
in vec3 position;
in vec2 texCoord;
in vec3 normal;
in vec3 tangent;

in vec4 boneDataA;
in vec4 boneDataB;

out vec2 texCoord0;
out mat3 tbnMatrix;

uniform mat4 T_model;

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

vec3 getAnimatedPosition() {
    vec3 n = normalize((T_model * vec4(normal, 2.0)).xyz);
    vec3 t = normalize((T_model * vec4(tangent, 2.0)).xyz);
    vec3 moddedT = normalize(t - dot(t, n) * n);

    vec3 biTangent = cross(t, n);

    return vec3(t * moddedT * biTangent * n);
}

void main() {
    texCoord0 = texCoord;
    mat4 boneTransform = getBoneTransform();
    vec3 animatedPos = getAnimatedPosition();
    gl_Position = T_model * boneTransform * vec4(position + animatedPos, 1.0);
}