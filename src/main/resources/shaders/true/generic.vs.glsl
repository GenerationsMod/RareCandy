#version 460
#define MAX_BONES 220
#define MINECRAFT_LIGHT_POWER   (0.6)
#define MINECRAFT_AMBIENT_LIGHT (0.4)

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord;
out vec3 fragViewDir;
out vec3 worldPos;
flat out int drawId;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

#lib:structs
#lib:fog
#lib:vertex
#lib:light

void main() {
    TargetVertex v = getVertex();
    drawId = gl_BaseInstance;

    mat4 worldSpace = projectionMatrix * viewMatrix;
    vec4 worldPosition = vec4(v.position, 1.0);

    texCoord = v.texCoord;
    gl_Position = worldSpace * worldPosition;
    vertexDistance = fog_distance(v.position, FogShape);
    vertexColor = getVertexColor(v.normal);

    fragViewDir = normalize(-(viewMatrix * worldPosition).xyz);
    worldPos = worldPosition.xyz;
}