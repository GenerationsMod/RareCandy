#version 460 core

uniform sampler2D inNormal;

out vec2 uv;

const vec2 POSITIONS[3] = vec2[](
        vec2(-1.0, -1.0),
        vec2( 3.0, -1.0),
        vec2(-1.0,  3.0)
);

const vec2 UVS[3] = vec2[](
        vec2(0.0, 0.0),
        vec2(2.0, 0.0),
        vec2(0.0, 2.0)
);

void main() {
    gl_Position = vec4(POSITIONS[gl_VertexID], 0.0, 1.0);
    uv = UVS[gl_VertexID];
}
