const int SAMPLE_COUNT = 8;

const float OFFSETS[8] = float[8](
        -6.328357272092126,
        -4.378621204796657,
        -2.431625915613778,
        -0.4862426846689484,
        1.4588111840004858,
        3.4048471718931532,
        5.353083811756559,
        7
);

const float WEIGHTS[8] = float[8](
        0.027508406306604068,
        0.08940648616079577,
        0.18921490087565024,
        0.26088633929947086,
        0.2343989200518563,
        0.13722534949218246,
        0.052327012559001844,
        0.009032585254438357
);

const vec2 horizontal = vec2(1,0);
const vec2 vertical = vec2(0,1);

// blurDirection is:
//     vec2(1,0) for horizontal pass
//     vec2(0,1) for vertical pass
// The sourceTexture to be blurred MUST use linear filtering!
// pixelCoord is in [0..1]
vec4 blur(in sampler2D sourceTexture, vec2 blurDirection, vec2 pixelCoord)
{
    vec4 result = vec4(0.0);
    vec2 size = textureSize(sourceTexture, 0);
    for (int i = 0; i < SAMPLE_COUNT; ++i) {
        vec2 offset = blurDirection * OFFSETS[i] / size;
        float weight = WEIGHTS[i];
        result += texture(sourceTexture, pixelCoord + offset) * weight;
    }
    return result;
}