#version 430
layout(local_size_x = 256) in;

struct SourceVertex {
    vec3 position;
    vec2 texcoord;
    vec3 normal;
    uvec4 joints;
    vec4 weights;
};

struct TargetVertex {
    vec3 position;
    vec2 texcoord;
    vec3 normal;
};

layout(std140, binding = 0) uniform Instance {
    mat4 modelMatrix;
    mat4 boneTransforms[220];
};

uniform vec4 transform;
//uniform int offset;

layout(std430, binding = 0) readonly buffer SrcBuffer {
    SourceVertex src[];
};

layout(std430, binding = 1) readonly buffer IndexBuffer {
    int indices[];
};


layout(std430, binding = 2) writeonly buffer DstBuffer {
    TargetVertex dst[];
};

mat4 getBoneTransform(uvec4 joints, vec4 weights) {
    return
    boneTransforms[joints.x] * weights.x +
    boneTransforms[joints.y] * weights.y +
    boneTransforms[joints.z] * weights.z +
    boneTransforms[joints.w] * weights.w;
}

void main() {
    uint local = gl_GlobalInvocationID.x;
//    if (local >= count) return;

    uint idx = local;

    SourceVertex src = src[idx];
    TargetVertex outV;

    vec4 pos = getBoneTransform(src.joints, src.weights) * vec4(src.position, 1.0);

    outV.position = pos.xyz;
    outV.texcoord = src.texcoord * transform.xy + transform.zw;
    outV.normal   = src.normal;

    dst[idx] = outV;
}