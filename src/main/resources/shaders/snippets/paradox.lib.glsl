uniform sampler2D paradoxTexture;
uniform int frame;

float getParadoxIntensity(vec2 effectTexCoord) {
    effectTexCoord *= 4.0;
    effectTexCoord = fract(effectTexCoord);

    effectTexCoord *= 0.25;
    effectTexCoord.x += (frame % 4) / 4.0;
    effectTexCoord.y += (frame / 4) / 4.0;

    return clamp(texture(paradoxTexture, effectTexCoord).r * 2.0, 0.0, 1.0);
}
