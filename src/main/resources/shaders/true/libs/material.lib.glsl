const vec2 outputSize = vec2(1024.0);
const vec4 dark = vec4(0.0);
const vec4 bright = vec4(1.0);

uniform sampler2DArray images;

vec4 getColor(int layer, vec2 uv, vec4 fallback) {
    if(layer < 0) return fallback;
    return texture(images, vec3(uv, layer));
}

vec4 adjust(vec4 color) {
    return clamp(color * 2.0, 0.0, 1.0);
}

float adjustScalar(float color) {
    return clamp(color * 2.0, 0.0, 1.0);
}

vec3 applyEmission(vec3 base, vec3 emissionColor, float intensity) {
    return base + (emissionColor - base) * intensity;
}

vec4 layered(vec2 uv, Material material) {
    vec4 color = getColor(material.diffuse, uv, bright);
    vec4 layerMasks = adjust(getColor(material.layer, uv, dark));
    float maskColor = adjustScalar(getColor(material.mask, uv, dark).r);

    vec3 base = mix(color.rgb, color.rgb * material.baseColor1, layerMasks.r);
    base = mix(base, color.rgb * material.baseColor2, layerMasks.g);
    base = mix(base, color.rgb * material.baseColor3, layerMasks.b);
    base = mix(base, color.rgb * material.baseColor4, layerMasks.a);
    base = mix(base, color.rgb * material.baseColor5, maskColor);

    base = mix(base, applyEmission(base, material.emiColor1, material.emiIntensity1), layerMasks.r);
    base = mix(base, applyEmission(base, material.emiColor2, material.emiIntensity2), layerMasks.g);
    base = mix(base, applyEmission(base, material.emiColor3, material.emiIntensity3), layerMasks.b);
    base = mix(base, applyEmission(base, material.emiColor4, material.emiIntensity4), layerMasks.a);
    base = mix(base, applyEmission(vec3(0.0), material.emiColor5, material.emiIntensity5), maskColor);

    return vec4(base, color.a);
}

vec4 masked(vec2 uv, Material material) {
    vec4 color = getColor(material.diffuse, uv, bright);
    float maskColor = getColor(material.mask, uv, dark).r;
    color.rgb = mix(color.rgb, color.rgb * material.baseColor1, maskColor);
    return color;
}

vec4 baseColor(vec2 uv, Material material) {
    if(material.colorMethod > 0) {
        if (material.colorMethod == 1) return layered(uv, material);
        else if (material.colorMethod == 2) return masked(uv, material);
    }

    return getColor(material.diffuse, uv, bright);
}

// Galaxy
const float darkenFactor = 0.3;

vec4 galaxy(vec4 color) {
    float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));

    const vec3 lightGradientColor1 = vec3(0.2, 0.0, 0.3);
    const vec3 lightGradientColor2 = vec3(0.6, 0.1, 0.7);
    const float gradientThreshold = 0.5;

    vec3 gradientColor = mix(
    lightGradientColor1,
    lightGradientColor2,
    smoothstep(gradientThreshold, 1.0, brightness));

    color.rgb *= darkenFactor;
    color.rgb = mix(
    color.rgb,
    gradientColor,
    smoothstep(gradientThreshold, 1.0, brightness));

    return color;
}

// Pastel
vec4 pastel(vec4 inColor, vec2 uv) {
    vec2 wrappedUV = fract(uv * 5.0);

    float gradient = sin(wrappedUV.x * 3.14159) * sin(wrappedUV.y * 3.14159);
    gradient = (gradient + 1.0) * 0.5;

    vec3 pastelBlue = vec3(0.8, 0.9, 1.0);
    vec3 pastelPink = vec3(1.0, 0.8, 0.9);

    vec3 pastelColor = mix(pastelBlue, pastelPink, gradient);

    return vec4(mix(inColor.rgb, pastelColor, 0.5), inColor.a);
}

// Shadow
vec4 shadow(vec4 inColor, vec2 uv) {
    float grayscale = dot(inColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 base = vec3(grayscale);

    vec2 wrappedUV = fract(uv * 5.0);
    float gradient = sin(wrappedUV.x * 3.14159) * sin(wrappedUV.y * 3.14159);
    gradient = (gradient + 1.0) * 0.5;

    vec3 deepPurpleBlue = vec3(0.1, 0.1, 0.2);
    vec3 darkerShade    = vec3(0.05, 0.05, 0.1);

    vec3 shadowColor = mix(deepPurpleBlue, darkerShade, gradient);

    vec3 finalColor = mix(base, shadowColor, 0.7);
    finalColor = clamp(finalColor * 0.9, 0.0, 1.0);

    return vec4(finalColor, inColor.a);
}

// Sketch (portable Sobel on diffuse luminance)
float luminanceAt(int layer, vec2 uv) {
    return dot(getColor(layer, uv, bright).rgb, vec3(0.2126, 0.7152, 0.0722));
}

vec3 sketchRGB(int layer, vec2 uv) {
    vec2 texel = 1.0 / vec2(textureSize(images, 0).xy);

    float tl = luminanceAt(layer, uv + texel * vec2(-1.0, -1.0));
    float  t = luminanceAt(layer, uv + texel * vec2( 0.0, -1.0));
    float tr = luminanceAt(layer, uv + texel * vec2( 1.0, -1.0));
    float  l = luminanceAt(layer, uv + texel * vec2(-1.0,  0.0));
    float  c = luminanceAt(layer, uv + texel * vec2( 0.0,  0.0));
    float  r = luminanceAt(layer, uv + texel * vec2( 1.0,  0.0));
    float bl = luminanceAt(layer, uv + texel * vec2(-1.0,  1.0));
    float  b = luminanceAt(layer, uv + texel * vec2( 0.0,  1.0));
    float br = luminanceAt(layer, uv + texel * vec2( 1.0,  1.0));

    float gx = (-1.0 * tl) + ( 1.0 * tr)
    + (-2.0 *  l) + ( 2.0 *  r)
    + (-1.0 * bl) + ( 1.0 * br);

    float gy = (-1.0 * tl) + (-2.0 *  t) + (-1.0 * tr)
    + ( 1.0 * bl) + ( 2.0 *  b) + ( 1.0 * br);

    float edge = length(vec2(gx, gy));
    float outline = 1.0 - smoothstep(0.10, 0.25, edge);

    vec3 baseGray = vec3(c);
    vec3 edgeColor = vec3(0.0);

    return mix(baseGray, edgeColor, outline);
}

// Vintage
vec4 vintage(vec4 inColor) {
    float grayscale = dot(inColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    return vec4(vec3(grayscale), inColor.a);
}

vec4 process(Material material, vec4 color, vec2 uv, int effect) {
    if (effect > 0) {
        if (effect == 1) return galaxy(color);
        else if (effect == 2) return pastel(color, uv);
        else if (effect == 3) return shadow(color, uv);
        else if (effect == 4) return vec4(sketchRGB(material.diffuse, uv), color.a);
        else if (effect == 5) return vintage(color);
    }

    return color;
}

vec4 getMaterialColor(vec2 uv, Material material, Variant variant) {
    vec4 color = baseColor(uv, material);
    color = process(material, color, uv, variant.effect);

    if (variant.paradox) {
        color.rgb = mix(color.rgb, vec3(1.0), getParadoxIntensity(uv));
    }

    return color;
}

float getEmission(vec2 uv, Material material) {
    return getColor(material.emission, uv, dark).r;
}
