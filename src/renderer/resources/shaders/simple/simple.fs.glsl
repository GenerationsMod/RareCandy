#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;
uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;

void main() {
    vec4 sampleColor = texture(textureSampler, fragTexCoord);

    // Overlay colors based on transparency
    vec4 resultColor = vec4(0.0);

    resultColor += color4 * sampleColor.w;
    resultColor += color2 * sampleColor.y;
    resultColor += color3 * sampleColor.z;
    resultColor += color1 * sampleColor.x;

    fragColor = resultColor;
}