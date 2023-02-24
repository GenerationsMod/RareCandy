#version 450 core

in vec2 texCoord0;
in vec3 normal;
in vec3 toLightVector;
in vec3 toCameraVector;

out vec4 outColor;

uniform sampler2D diffuse;

uniform vec3 LIGHT_color;
uniform float LIGHT_shineDamper;
uniform float LIGHT_reflectivity;

const float AMBIENT_LIGHT = 0.6f;

void main() {
    vec4 color = texture(diffuse, texCoord0);
    if (color.a < 0.1) discard;
    outColor = color;
}
