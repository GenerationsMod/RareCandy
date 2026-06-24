#version 460 // Or higher

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord;
in vec3 fragViewDir;
in vec3 worldPos;
flat in int drawId;

out vec4 outColor;

uniform vec4 ColorModulator;
uniform vec4 tint;
uniform bool debug;

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

    if(debug) {
        outColor = vec4(1.0);
    }
}
