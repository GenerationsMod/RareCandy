#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D inAlbedo;
uniform sampler2D inNormal;
uniform sampler2D inEmissive;

ivec2 adjustLight(ivec2 lightCoord) {
    return ivec2(lightCoord);
}

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



vec4 applyLight(vec4 outColor, int emission) {

    ivec2 localLight = adjustLight(light); // adjustLight should be implmented in the fragment shader its used in.
    localLight.g = max(localLight.g, emission);

    vec4 minecraftLight = minecraft_sample_lightmap(lightmap, localLight);

    outColor *= minecraftLight;

    return outColor;
}

void main() {
    outColor = texture(inAlbedo, uv) * getVertexColor(texture(inNormal, uv).xyz);

    float emission = texture(inEmissive, uv).r;

    outColor = applyLight(outColor, int(emission * 15));
}