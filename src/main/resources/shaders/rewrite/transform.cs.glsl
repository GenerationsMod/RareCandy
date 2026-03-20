#version 430
layout(local_size_x = 256, local_size_y = 1) in;

struct SourceVertex {
    vec3 position;
    vec2 texcoord;
    vec3 normals;
    vec4 tangents;
    uvec4 joints;
    vec4 weights;
};

struct TargetVertex {
    vec3 position;
    vec2 texcoord;
    vec3 normal;
    vec3 tangent;
    vec3 bitangent;
};

struct DrawCmd {
    uint baseIndex;
    uint indexCount;
};

struct Instance {
    mat4 modelMatrix;
    mat3 normalMatrix;
    mat4 boneTransforms[220];
};

struct Variant {
    int material;
    int effect;
    bool paradox;
};

struct Transform {
    vec2 scale;
    vec2 offset;
    int variant;
};

uniform int variantSize;
uniform int instanceId;

layout(std430, binding = 0) readonly  buffer SrcBuffer       { SourceVertex src[]; };
layout(std430, binding = 1) readonly  buffer IndexBuffer     { uint indices[]; };
layout(std430, binding = 2) readonly  buffer DrawCommands    { DrawCmd cmd[]; };
layout(std430, binding = 3) readonly  buffer InstanceBuffer  { Instance instances[]; };
layout(std430, binding = 4) readonly  buffer TransformBuffer { Transform transforms[]; };
layout(std430, binding = 5) writeonly buffer DstBuffer       { TargetVertex dst[]; };

mat4 getBoneTransform(Instance instance, uvec4 joints, vec4 weights) {
    mat4[] bone = instance.boneTransforms;

    return
        bone[joints.x] * weights.x +
        bone[joints.y] * weights.y +
        bone[joints.z] * weights.z +
        bone[joints.w] * weights.w;
}

void main() {
    uint local = gl_GlobalInvocationID.x;
    uint meshId = gl_GlobalInvocationID.y;

    DrawCmd c = cmd[meshId];
    if (local >= c.indexCount) return;

    uint idx = c.baseIndex + local;

    SourceVertex src = src[indices[idx]];
    TargetVertex outV;

    Instance instance = instances[instanceId];

    vec4 pos = getBoneTransform(instance, src.joints, src.weights) * vec4(src.position, 1.0) * instance.modelMatrix;

    Transform variant = transforms[instanceId * variantSize + meshId];

    outV.position = pos.xyz;
    outV.texcoord = src.texcoord * variant.scale + variant.offset;
    outV.normal = normalize(instance.normalMatrix * src.normals);
    outV.tangent = normalize(instance.normalMatrix * src.tangents.xyz);
    outV.tangent =  normalize(outV.tangent - dot(outV.tangent, outV.normal) * outV.normal);
    outV.bitangent = cross(outV.normal, outV.tangent) * outV.tangent;

    dst[idx] = outV;
}