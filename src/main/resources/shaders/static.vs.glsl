#version 450
#define MAX_BONES 200
in vec3 position;
in vec2 texCoord;
in vec3 normal;

out vec2 texCoord0;

uniform mat4 MC_projection;
uniform mat4 MC_view;
uniform mat4 MC_model;

void main() {
    texCoord0 = texCoord;
    gl_Position = MC_projection * MC_view * MC_model * vec4(position, 1.0);
}