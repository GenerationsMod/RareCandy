#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D inAlbedo;
uniform vec3 lightColor;
uniform float lightIntensity;

void main() {
    outColor = vec4(lightColor * lightIntensity * texture(inAlbedo, uv).rgb, 1.0);
}