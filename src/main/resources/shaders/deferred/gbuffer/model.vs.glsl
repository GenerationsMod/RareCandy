#version 460
#define MAX_BONES 220

out vec2 texCoord;
out vec3 worldNormal;
flat out int drawId;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

#lib:structs
#lib:vertex

void main() {
    TargetVertex v = getVertex();
    drawId = gl_BaseInstance;

    mat4 worldSpace = projectionMatrix * viewMatrix;
    vec4 worldPosition = vec4(v.position, 1.0);

    gl_Position = worldSpace * worldPosition;
    texCoord = v.texCoord;
    worldNormal = normalize(v.normal);
}
