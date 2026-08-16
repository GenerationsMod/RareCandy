#version 460

in vec2 texCoord;
in vec3 worldNormal;
flat in int drawId;

layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outNormal;
layout(location = 2) out vec4 outEmission;
layout(location = 3) out float outObject;

uniform vec4 ColorModulator;
uniform vec4 tint;
uniform int Selected;

#lib:structs
#lib:paradox
#lib:material

void main() {
    DrawInfo transform = drawInfos[drawId];

    Variant variant = variants[transform.variant];
    Material material = materials[variant.material];

    outColor = getMaterialColor(texCoord, material, variant) * ColorModulator * tint;
    outNormal = vec4(normalize(worldNormal) * 0.5 + 0.5, 1.0);
    outEmission = vec4(material.useLight ? getEmission(texCoord, material, variant) : 1.0, 0.0, 0.0, 0.0);
    outObject = transform.mesh == Selected ? 1.0 : 0.0;
}