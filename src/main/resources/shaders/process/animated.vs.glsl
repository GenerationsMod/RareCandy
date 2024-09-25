#version 150 core
#extension GL_ARB_explicit_uniform_location : enable
#extension GL_ARB_explicit_attrib_location : enable

#define MAX_BONES 220

layout(location = 0) in vec3 positions;
layout(location = 1) in vec2 texcoords;

out vec2 texCoord0;

void main() {
    texCoord0 = texcoords;
    gl_Position = vec4((texcoords.x-0.5)*2, -(texcoords.y - 0.5) * 2, 0.0, 1.0);
}