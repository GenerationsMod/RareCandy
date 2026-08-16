#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D inAlbedo;
uniform sampler2D blurred;

void main() {
    vec4 blur = texture(blurred, uv);
    vec3 base = texture(inAlbedo, uv).rgb;
    outColor = vec4(blur.rgb + base * (1.0 - blur.a), 1.0);
}

