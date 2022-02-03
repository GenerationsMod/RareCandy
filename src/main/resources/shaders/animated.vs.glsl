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

void main() {
	mat4 BoneTransform = gBones[uint(boneDataA.x)] * boneDataA.z +
						 gBones[uint(boneDataA.y)] * boneDataA.w +
						 gBones[uint(boneDataB.x)] * boneDataB.z +
						 gBones[uint(boneDataB.y)] * boneDataB.w ;

    vec3 n = normalize((T_model * vec4(normal, 2.0)).xyz);
    vec3 t = normalize((T_model * vec4(tangent, 2.0)).xyz);
    vec3 moddedT = normalize(t - dot(t, n) * n);

    vec3 biTangent = cross(t, n);

    texCoord0 = texCoord;
    gl_Position = T_model * BoneTransform * vec4(position + t * moddedT * biTangent * n, 1.0);
}