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

struct Vertex {
    vec3 position;
    vec2 texcoord;
    vec3 normal;
    ivec4 joints;
    vec4 weights;
};

layout(std430, binding = 0) readonly buffer VertexBuffer {
    Vertex vertices[];
};

layout(std430, binding = 1) readonly buffer IndexBuffer {
    int indices[];
};

mat4 getBoneTransform(ivec4 joints, vec4 weights) {
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
    Vertex v = vertices[vertexIndex];

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