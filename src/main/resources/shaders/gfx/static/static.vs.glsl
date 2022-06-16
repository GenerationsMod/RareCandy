#version 450
layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;
layout(location = 2) in vec3 inNormal;

out vec2 texCoord0;
out vec3 toLightVector;
out vec3 toCameraVector;
out vec3 normal;

uniform mat4 MC_view;
uniform mat4 MC_model;
uniform mat4 MC_projection;
uniform vec3 LIGHT_pos;

void main() {
    texCoord0 = inTexCoord;

    mat4 worldSpace = MC_projection * MC_view;
    vec4 worldPosition = MC_model * vec4(inPosition, 1.0);

    gl_Position = worldSpace * worldPosition;
    normal = (MC_model * vec4(inNormal, 0.0)).xyz;
    toLightVector = LIGHT_pos - worldPosition.xyz;
    toCameraVector = (inverse(MC_view) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;
}