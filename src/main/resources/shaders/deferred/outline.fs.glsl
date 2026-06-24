#version 460 core

in vec2 uv;
out vec4 outColor;

uniform sampler2D sceneTexture;
uniform sampler2D objectTexture;

uniform vec4 outlineColor;
uniform float outlineThickness;

const int MAX_RADIUS = 4;

void main() {
    vec4 scene = texture(sceneTexture, uv);

    if (outlineThickness <= 0.0 || outlineColor.a <= 0.0) {
        outColor = scene;
        return;
    }

    ivec2 size = textureSize(objectTexture, 0);

    ivec2 pixel = clamp(
    ivec2(uv * vec2(size)),
    ivec2(0),
    size - 1
    );

    bool centerIsObject =
    texelFetch(objectTexture, pixel, 0).r > 0.5;

    float radius = clamp(
    outlineThickness,
    1.0,
    float(MAX_RADIUS)
    );

    float edge = 0.0;

    /*
     * Draw only outside the object.
     *
     * Object pixels remain unchanged. Background pixels become outline
     * pixels when they are close enough to the highlighted object.
     */
    if (!centerIsObject) {
        for (int y = -MAX_RADIUS; y <= MAX_RADIUS; ++y) {
            for (int x = -MAX_RADIUS; x <= MAX_RADIUS; ++x) {
                float distanceFromCenter = length(vec2(x, y));

                if (distanceFromCenter > radius) {
                    continue;
                }

                ivec2 samplePixel = clamp(
                pixel + ivec2(x, y),
                ivec2(0),
                size - 1
                );

                bool sampleIsObject =
                texelFetch(objectTexture, samplePixel, 0).r > 0.5;

                if (sampleIsObject) {
                    edge = 1.0;
                    break;
                }
            }

            if (edge > 0.0) {
                break;
            }
        }
    }

    float alpha = edge * outlineColor.a;

    outColor = vec4(
    mix(scene.rgb, outlineColor.rgb, alpha),
    scene.a
    );
}