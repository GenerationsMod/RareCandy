#version 430
layout(local_size_x = 16, local_size_y = 16) in;

uniform sampler2D paradoxMask;
uniform int frame;

layout(rgba8, binding = 0) uniform image2D solidTex;
layout(rgba8, binding = 1) uniform image2D litTex;

float getParadoxIntensity(vec2 effectTexCoord) {
    if (frame >= 0) {
        effectTexCoord *= 4.0;
        effectTexCoord = fract(effectTexCoord);

        effectTexCoord *= 0.25;
        effectTexCoord.x += (frame % 4) / 4.0;
        effectTexCoord.y += (frame / 4) / 4.0;
    }

    return clamp(texture(paradoxMask, effectTexCoord).r * 2.0, 0.0, 1.0);
}

void main() {
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);

    vec4 color = imageLoad(solidTex, pixel);
    vec4 lit = imageLoad(litTex, pixel);

    float intensity = getParadoxIntensity(uv);
    color.rgb = mix(color.rgb, vec3(1.0), intensity);
    lit.rgb = mix(lit.rgb, vec3(1.0), intensity);

    imageStore(solidTex, pixel, color);
    imageStore(litTex, pixel, lit);
}
