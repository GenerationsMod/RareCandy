#version 330 core
#pragma optionNV(strict on)

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 texcoords;
layout (location = 2) in vec3 inNormal;

out vec3 outReflection;
out vec3 outRefraction;
out float outFresnel;
out vec2 outTexCoords;
out vec4 outStarCoords;
out vec3 outNormal;
out vec3 outPos;
out vec3 incident;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec3 camPos;

vec4 projectionFromPos(vec4 position) {
    vec4 projection = position * 2;
    projection.xy = vec2(projection.x + projection.w, projection.y + projection.w);
    projection.zw = position.zw;
    return projection;
}

void main() {
    outPos = (modelMatrix * vec4(inPos, 1.0)).xyz;
    vec3 normal = (mat3(modelMatrix) * inNormal);

    vec3 camPos = vec3(inverse(viewMatrix)[3]);
    vec3 cameraForward = vec3(viewMatrix[0][2], viewMatrix[1][2], viewMatrix[2][2]);
    incident = normalize(outPos - camPos - cameraForward);

    outTexCoords = texcoords;
    outNormal = mat3(modelMatrix) * inNormal;
    gl_Position = projectionMatrix * viewMatrix * vec4(outPos, 1.0);
    outStarCoords = projectionFromPos(gl_Position);
}