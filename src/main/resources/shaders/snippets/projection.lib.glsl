vec3 viewPosFromDepth(vec2 uv, float depth, mat4 inverseProjection) {
    vec4 clip = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewH = inverseProjection * clip;
    return viewH.xyz / viewH.w;
}

vec3 worldPosFromDepth(vec2 uv, float depth, mat4 inverseProjection, mat4 inverseView) {
    vec3 view = viewPosFromDepth(uv, depth, inverseProjection);
    return (inverseView * vec4(view, 1.0)).xyz;
}