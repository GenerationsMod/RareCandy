#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
    vec4 sampleColor = texture(textureSampler, fragTexCoord);

    fragColor = sampleColor;
}