#version 150 core

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D backgroundAlb;
uniform sampler2D effectMask;

uniform vec3 backgroundColor;
uniform vec3 effectColor;

uniform int frame;
//uniform ivec2 gridSize; implment if needed later on

void main() {
    outColor = texture(backgroundAlb, texCoord0);
    outColor.xyz *= backgroundColor;

    vec2 effectTexCoord = vec2(texCoord0);
    effectTexCoord *= 0.25;
    effectTexCoord.x += (frame % 4)/4f;
    effectTexCoord.y +=  (frame/4)/4f;

    outColor.xyz = mix(outColor.xyz, effectColor, texture(effectMask, effectTexCoord).r);
}
