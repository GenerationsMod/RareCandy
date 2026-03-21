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
    return
    instance.boneTransforms[joints.x] * weights.x +
    instance.boneTransforms[joints.y] * weights.y +
    instance.boneTransforms[joints.z] * weights.z +
    instance.boneTransforms[joints.w] * weights.w;
}

void main() {
    uint local = gl_GlobalInvocationID.x;
    uint meshId = gl_GlobalInvocationID.y;

    DrawCmd c = cmd[meshId];
    if (local >= c.indexCount) return;

    uint idx = c.baseIndex + local;

    SourceVertex inV = src[indices[idx]];
    TargetVertex outV;

    Instance instance = instances[instanceId];
    Transform variant = transforms[instanceId * variantSize + meshId];

    mat4 skin = getBoneTransform(instance, inV.joints, inV.weights);
    mat3 skin3 = mat3(skin);

    vec4 skinnedPos = skin * vec4(inV.position, 1.0);
    vec3 skinnedNormal = normalize(skin3 * inV.normals);
    vec3 skinnedTangent = normalize(skin3 * inV.tangents.xyz);

    vec4 worldPos = instance.modelMatrix * skinnedPos;

    outV.position = worldPos.xyz;
    outV.texcoord = inV.texcoord * variant.scale + variant.offset;

    outV.normal = normalize(instance.normalMatrix * skinnedNormal);
    outV.tangent = normalize(instance.normalMatrix * skinnedTangent);
    outV.tangent = normalize(outV.tangent - dot(outV.tangent, outV.normal) * outV.normal);
    outV.bitangent = cross(outV.normal, outV.tangent) * inV.tangents.w;

    dst[idx] = outV;
}