#version 420 core

in vec2 texCoord0;
in vec4 vertexColor;
in float vertexDistance;

out vec4 outColor;

uniform vec4 ColorModulator;

//fog
layout(std140, binding = 0) uniform Fog {
    vec4 FogColor;
    float FogStart;
    float FogEnd;
    int FogShape;
};

uniform sampler2D solid;

uniform vec3 tint;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

vec4 linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
    if (vertexDistance <= fogStart) {
        return inColor;
    }

    float fogValue = vertexDistance < fogEnd ? smoothstep(fogStart, fogEnd, vertexDistance) : 1.0;
    return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a);
}

float linear_fog_fade(float vertexDistance, float fogStart, float fogEnd) {
    if (vertexDistance <= fogStart) {
        return 1.0;
    } else if (vertexDistance >= fogEnd) {
        return 0.0;
    }

    return smoothstep(fogEnd, fogStart, vertexDistance);
}

#define TERA_LIGHT_DIRECT   vec3(0.3f, 0.9f, 0.0f)
#define TERATYPE_TINT       vec3(0.161, 0.502, 0.937)

#define FACET_RES           8.0
#define SHIMMER_BANDS       5.0
#define GAMMA_CORRECTION    1.05
#define SHIMMER_STRENGTH    0.8
#define IRIDESCENCE_STRENGTH 0.2

uniform bool tera;
uniform vec3 teraTint;

in vec3 fragViewDir;
in vec3 worldPos;

vec3 calculateTersaalizationEffect(vec3 baseColor) {
    vec3 N = normalize(cross(dFdx(worldPos), dFdy(worldPos)));
    vec3 V = normalize(fragViewDir);

    float fresnel = pow(1.0 - max(dot(N, V), 0.0), 5.0);
    float lightResponse = max(dot(N, TERA_LIGHT_DIRECT), 0.0);

    vec3 faceted = normalize(floor(N * FACET_RES + 0.5) / FACET_RES);
    float facetResponse = pow(max(dot(faceted, V), 0.0), 6.0);

    float shimmer = fresnel * 0.5 + lightResponse * 0.3 + facetResponse * 0.8;
    shimmer = pow(clamp(shimmer, 0.0, 1.0), GAMMA_CORRECTION);

    shimmer = floor(shimmer * SHIMMER_BANDS) / SHIMMER_BANDS;

    float angleNoise = abs(sin(dot(N.xyz, vec3(12.9898, 78.233, 45.164)) * 43758.5453));
    shimmer += angleNoise * 0.02;

    // Adjusted iridescent base to be less blinding
    vec3 iridescent = vec3(0.9, 0.85, 0.8) + vec3(0.05, -0.02, 0.03) * sin(shimmer * 20.0);

    vec3 directionalTint = mix(teraTint, vec3(1.0), lightResponse);

    // --- Brightness control section ---
    float shimmerIntensity = clamp(shimmer * 0.85, 0.0, 0.9);  // Scale down max shimmer
    vec3 shimmerContribution = directionalTint * shimmerIntensity * SHIMMER_STRENGTH;

    vec3 iridescentContribution = iridescent * shimmerIntensity * IRIDESCENCE_STRENGTH;

    // Final composite
    vec3 result = baseColor + shimmerContribution + iridescentContribution;

    // Soft clamp to avoid overshooting extreme whites
    result = clamp(result, 0.0, 1.0);

    return result;
}

//////////////////////////////////////

void main() {
    outColor = texture(solid, texCoord0) * ColorModulator;

    if (outColor.a < 0.004) {
        discard;
    }

    outColor.rgb *= tint;

    if(tera) {
        outColor.rgb = calculateTersaalizationEffect(outColor.rgb);
    }

    outColor = linear_fog(outColor, vertexDistance, FogStart, FogEnd, FogColor);
}