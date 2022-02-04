#version 450

in vec2 texCoord0;
out vec4 outColor;

uniform sampler2D diffuse;

void main() {
    vec4 color = texture2D(diffuse, texCoord0);
    if(color.a < 0.1) {
        discard;
    }

    outColor = color;
}
