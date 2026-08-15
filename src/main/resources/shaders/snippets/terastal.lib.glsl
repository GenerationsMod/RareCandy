#define TERA_LIGHT_DIRECT   vec3(0.3f, 0.9f, 0.0f)
#define TERATYPE_TINT       vec3(0.161, 0.502, 0.937)

#define FACET_RES           8.0
#define SHIMMER_BANDS       5.0
#define GAMMA_CORRECTION    1.05
#define SHIMMER_STRENGTH    0.8
#define IRIDESCENCE_STRENGTH 0.2

uniform vec3 teraTint;
uniform bool teraActive;

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