#version 460 // Or higher

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 fragViewDir;
in vec3 worldPos;
flat in int drawId;

out vec4 outColor;

uniform vec4 ColorModulator;
uniform vec4 tint;

uniform bool renderTranslucent;

#lib:structs
#lib:paradox
#lib:light
#lib:fog
#lib:material
#lib:terastal

void main() {
    Transform transform = transforms[drawId];

    if(!transform.shouldRender) {
        discard;
    }

    Variant variant = variants[transform.variant];

    Material material = materials[variant.material];

    if(renderTranslucent != material.translucent) discard;

    outColor = getMaterialColor(texCoord0, material, variant) * ColorModulator * tint;

    if(teraActive) {
        outColor.rgb = calculateTersaalizationEffect(outColor.rgb);
    } else if(material.useLight) {
        outColor *= applyLight(outColor, getEmission(texCoord0, material));
    }

    outColor = linear_fog(outColor, vertexDistance, FogStart, FogEnd, FogColor);
}
