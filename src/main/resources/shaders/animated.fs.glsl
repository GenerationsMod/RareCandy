#version 450

in vec2 texCoord0;
out vec4 color;

uniform sampler2D diffuse;

void main() {
    color = texture2D(diffuse, texCoord0);
}
