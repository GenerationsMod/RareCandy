vec2 getUV(ivec2 uv, ivec2 size) {
    return clamp((vec2(uv) + 0.5) / vec2(size), vec2(0), vec2(1));
}

float luminance(vec4 color) {
    return dot(color.rgb, vec3(0.299, 0.587, 0.114));
}

float luminance(sampler2D tex, ivec2 pixel, ivec2 size) {
    return luminance(texture(tex, getUV(pixel, size)));
}

vec2 sobelFilter(sampler2D inputTexture, ivec2 coord, ivec2 size) {
    #define GET_LUM(offset) luminance(inputTexture, coord + offset, size)

    float n0 = GET_LUM(ivec2(-1, -1));
    float n1 = GET_LUM(ivec2( 0, -1));
    float n2 = GET_LUM(ivec2( 1, -1));
    float n3 = GET_LUM(ivec2(-1,  0));
    // n4 (center) is skipped
    float n5 = GET_LUM(ivec2( 1,  0));
    float n6 = GET_LUM(ivec2(-1,  1));
    float n7 = GET_LUM(ivec2( 0,  1));
    float n8 = GET_LUM(ivec2( 1,  1));

    #undef GET_LUM

    float sobel_edge_h = n2 + (2.0 * n5) + n8 - (n0 + (2.0 * n3) + n6);
    float sobel_edge_v = n0 + (2.0 * n1) + n2 - (n6 + (2.0 * n7) + n8);

    return vec2(sobel_edge_h, sobel_edge_v);
}

vec3 normal(sampler2D tex, float scale, ivec2 pixel, ivec2 size) {
    vec2 color = sobelFilter(tex, pixel, size);

    vec3 v = vec3(color.xy * scale, 1.0);

    v = normalize(v);
    return (v + 1.0) / 2.0;
}

float intensity(sampler2D tex, ivec2 pixel, ivec2 size, float contrast, float brightness, float power) {
    return clamp(contrast * pow(luminance(tex, pixel, size), power) + brightness, 0.0, 1.0);
}

vec3 viewPosFromDepth(vec2 uv, float depth, mat4 inverseProjection) {
    vec4 clip = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewH = inverseProjection * clip;
    return viewH.xyz / viewH.w;
}

vec3 worldPosFromDepth(vec2 uv, float depth, mat4 inverseProjection, mat4 inverseView) {
    vec3 view = viewPosFromDepth(uv, depth, inverseProjection);
    return (inverseView * vec4(view, 1.0)).xyz;
}
