#version 150 core

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D backgroundAlb;
uniform sampler2D effectMask;

uniform vec3 backgroundColor;
uniform vec3 effectColor;

void main() {
    outColor = texture(backgroundAlb, texCoord0);
    outColor.xyz *= backgroundColor;

    vec2 effectTexCoord = vec2(texCoord0);

    outColor.xyz = mix(outColor.xyz, effectColor, texture(effectMask, effectTexCoord).r);
}
