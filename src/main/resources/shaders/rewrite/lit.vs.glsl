#version 430 core
#define MAX_BONES 220
#define MINECRAFT_LIGHT_POWER   (0.6)
#define MINECRAFT_AMBIENT_LIGHT (0.4)

out float vertexDistance;
out vec2 texCoord0;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

layout(std140, binding = 0) uniform Fog {
    vec4 FogColor;
    float FogStart;
    float FogEnd;
    int FogShape;
};

struct Vertex {
    vec3 position;
    vec2 texcoord;
    vec3 normal;
};

layout(std430, binding = 0) readonly buffer VertexBuffer {
    Vertex vertices[];
};

float fog_distance(vec3 pos, int shape) {
    if (shape == 0) {
        return length(pos);
    } else {
        float distXZ = length(pos.xz);
        float distY = abs(pos.y);
        return max(distXZ, distY);
    }
}

void main() {
    // Lookup vertex through index buffer
//    int vertexIndex = indices[gl_VertexID];
    Vertex v = vertices[gl_VertexID];

    mat4 worldSpace = projectionMatrix * viewMatrix;
    vec4 worldPosition = vec4(v.position, 1.0);

    texCoord0 = v.texcoord;
    gl_Position = worldSpace * worldPosition;
    vertexDistance = fog_distance(v.position, FogShape);
}