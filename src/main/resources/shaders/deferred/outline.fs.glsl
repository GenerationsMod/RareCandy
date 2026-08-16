#version 460 core

in vec2 uv;
out vec4 outColor;

uniform vec2 size;
uniform float lineWidth;

uniform sampler2D inAlbedo;
uniform sampler2D inObject;



void main() {
    vec2 d = (vec2(1) / size) * lineWidth;

    #define GET_LUM(u, v) texture(inObject, vec2(uv.x + d.x * u, uv.y + d.y * v)).r

    float center = GET_LUM(0, 0);
    float right = GET_LUM(1, 0);
    float top = GET_LUM(0, -1);
    float topRight = GET_LUM(1, -1);

    float dT  = abs(center - top);
    float dR  = abs(center - right);
    float dTR = abs(center - topRight);

    float delta = 0.0;
    delta = max(delta, dT);
    delta = max(delta, dR);
    delta = max(delta, dTR);

    vec4 outline = vec4(delta, delta, delta, 1.0);
    vec4 albedo  = texture(inAlbedo, uv);

    outColor = mix(albedo, vec4(1.0), delta);

}