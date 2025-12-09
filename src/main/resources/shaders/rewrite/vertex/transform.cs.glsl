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
    mat4 boneTransforms[MAX_BONES];
};

layout(std430, binding = 0) readonly buffer SrcBuffer {
    SourceVertex src[];
};

layout(std430, binding = 1) writeonly buffer DstBuffer {
    TargetVertex dst[];
};

SourceVertex decodeVertex(uint index) {
    SourceVertex v;
    uint base = index;

    uint p0 = data[base + 0];
    int qx = int(p0 & 0xFFFFu);
    int qy = int((p0 >> 16) & 0xFFFFu);
    qx = (qx << 16) >> 16;
    qy = (qy << 16) >> 16;

    uint p1 = data[base + 1];
    int qz = int(p1 & 0xFFFFu);
    qz = (qz << 16) >> 16;
    v.position = vec3(qx, qy, qz) * 0.001;

    uint uvPacked = data[base + 2];
    v.texcoord = unpackHalf2x16(uvPacked);

    int packedNormal = int(data[base + 3]);
    int nx =  (packedNormal       & 0x3FF);
    int ny = ((packedNormal >>10) & 0x3FF);
    int nz = ((packedNormal >>20) & 0x3FF);
    nx = (nx << 22) >> 22;
    ny = (ny << 22) >> 22;
    nz = (nz << 22) >> 22;
    v.normal = vec3(float(nx), float(ny), float(nz)) / 511.0;

    uint bIds = data[base + 4];v.joints = uvec4((bIds      ) & 0xFFu, (bIds >>  8) & 0xFFu, (bIds >> 16) & 0xFFu, (bIds >> 24) & 0xFFu);

    uint bW = data[base + 5];
    v.weights = vec4(float((bW      ) & 0xFFu), float((bW >>  8) & 0xFFu), float((bW >> 16) & 0xFFu), float((bW >> 24) & 0xFFu)) / 255.0;

    return v;
}

mat4 getBoneTransform(uvec4 joints, vec4 weights) {
    return
    boneTransforms[joints.x] * weights.x +
    boneTransforms[joints.y] * weights.y +
    boneTransforms[joints.z] * weights.z +
    boneTransforms[joints.w] * weights.w;
}

void main() {
    uint idx = gl_GlobalInvocationID.x;
    if (idx >= vertexCount) return;

    SourceVertex src = decodeVertex(idx);
    TargetVertex dst;

    vec4 pos = vec4(src.position, 1.0) * getBoneTransform(src.joints, src.weights);

    dst.position = pos.xyz;
    dst.texcoord = src.texcoord;
    dst.normal = src.normal;
}

