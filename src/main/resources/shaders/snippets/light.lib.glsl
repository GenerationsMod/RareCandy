#define MINECRAFT_LIGHT_POWER   (0.6)
#define MINECRAFT_AMBIENT_LIGHT (0.4)
const vec4 FULL_BRIGHT = vec4(1);

uniform sampler2D lightmap;
uniform ivec2 light;
uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
    return texture(lightMap, (vec2(uv) + 0.5) / 16.0);
}

vec4 getVertexColor(vec3 normal) {
    vec3 lightDir0 = normalize(Light0_Direction);
    vec3 lightDir1 = normalize(Light1_Direction);
    float light0 = max(0.0, dot(lightDir0, normal));
    float light1 = max(0.0, dot(lightDir1, normal));
    float lightAccum = min(1.0, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT);
    return vec4(lightAccum, lightAccum, lightAccum, 1);
}

vec4 applyLight(vec4 outColor, float emission) {
    outColor *= vertexColor;
    // Sample Minecraft's light level from the lightmap texture
    vec4 minecraftLight = minecraft_sample_lightmap(lightmap, light);

    outColor *= mix(minecraftLight, FULL_BRIGHT, emission);

    return outColor;
}