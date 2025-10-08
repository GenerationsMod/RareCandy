#version 430 core
#define MAX_BONES 220
#define MINECRAFT_LIGHT_POWER   (0.6)
#define MINECRAFT_AMBIENT_LIGHT (0.4)

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec3 fragNormal;
out vec3 fragViewDir;
out vec3 worldPos;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

uniform vec2 uvOffset;
uniform vec2 uvScale;

layout(std140, binding = 0) uniform Fog {
    vec4 FogColor;
    float FogStart;
    float FogEnd;
    int FogShape;
};

layout(std140, binding = 1) uniform Instance {
    mat4 modelMatrix;
    mat4 boneTransforms[MAX_BONES];
};

layout(std430, binding = 0) readonly buffer VertexBuffer {
    uint data[];
};

layout(std430, binding = 1) readonly buffer IndexBuffer {
    int indices[];
};

struct Vertex {
    vec3 position;
    vec2 texcoord;
    vec3 normal;
    uvec4 joints;
    vec4 weights;
};

Vertex decodeVertex(int index) {
    Vertex v;
    uint base = index * 6u;

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

float fog_distance(mat4 modelViewMat, vec3 pos, int shape) {
    if (shape == 0) {
        return length((modelViewMat * vec4(pos, 1.0)).xyz);
    } else {
        float distXZ = length((modelViewMat * vec4(pos.x, 0.0, pos.z, 1.0)).xyz);
        float distY = length((modelViewMat * vec4(0.0, pos.y, 0.0, 1.0)).xyz);
        return max(distXZ, distY);
    }
}

vec4 getVertexColor(vec3 normal) {
    vec3 lightDir0 = normalize(Light0_Direction);
    vec3 lightDir1 = normalize(Light1_Direction);
    float light0 = max(0.0, dot(lightDir0, normal));
    float light1 = max(0.0, dot(lightDir1, normal));
    float lightAccum = min(1.0, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT);
    return vec4(lightAccum, lightAccum, lightAccum, 1);
}

void main() {
    // Lookup vertex through index buffer
    int vertexIndex = indices[gl_VertexID];
    Vertex v = decodeVertex(vertexIndex);

    mat4 worldSpace = projectionMatrix * viewMatrix;
    mat4 modelTransform = modelMatrix * getBoneTransform(v.joints, v.weights);
    vec4 worldPosition = modelTransform * vec4(v.position, 1.0);

    texCoord0 = (v.texcoord * uvScale) + uvOffset;
    gl_Position = worldSpace * worldPosition;
    vertexDistance = fog_distance(worldSpace * modelTransform, v.position, FogShape);
    vertexColor = getVertexColor(v.normal);

    fragViewDir = normalize(-(viewMatrix * worldPosition).xyz);
    worldPos = worldPosition.xyz;
}