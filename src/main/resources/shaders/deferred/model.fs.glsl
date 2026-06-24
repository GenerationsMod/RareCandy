#version 460

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord;
in vec3 fragViewDir;
in vec3 worldPos;
in vec3 worldNormal;
flat in int drawId;

layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outNormal;
layout(location = 2) out float outObject;

uniform vec4 ColorModulator;
uniform vec4 tint;
uniform int Selected;

#lib:structs
#lib:paradox
#lib:light
#lib:fog
#lib:material
#lib:terastal

void main() {
    DrawInfo transform = drawInfos[drawId];

    Variant variant = variants[transform.variant];
    Material material = materials[variant.material];

    outColor = getMaterialColor(texCoord, material, variant) * ColorModulator * tint;

    if(teraActive) {
        outColor.rgb = calculateTersaalizationEffect(outColor.rgb);
    } else if(material.useLight) {
        outColor = applyLight(outColor, getEmission(texCoord, material, variant));
    }

    outColor = linear_fog(outColor, vertexDistance, FogStart, FogEnd, FogColor);
    outNormal = vec4(normalize(worldNormal) * 0.5 + 0.5, outColor.a);
    if(transform.mesh == Selected) outObject = float(transform.mesh + 1);
}
