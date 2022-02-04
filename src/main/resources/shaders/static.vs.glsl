#version 450
#define MAX_BONES 200
in vec3 position;
in vec2 texCoord;
in vec3 normal;

out vec2 texCoord0;

uniform mat4 T_MVP;

void main() {
    texCoord0 = texCoord;
    gl_Position = T_MVP * vec4(position, 1.0);
}