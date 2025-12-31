#version 430
layout(local_size_x = 16, local_size_y = 16) in;

const vec2 outputSize = vec2(1024);

layout(rgba8, binding = 0) uniform image2D solidTex;
layout(rgba8, binding = 1) uniform image2D litTex;

uniform sampler2D diffuse;
uniform sampler2D emission;
uniform sampler2D layer;
uniform sampler2D mask;
uniform sampler2D paradoxTexture;

layout(std140, binding = 0) uniform Material {
    vec3 baseColor1;
    vec3 baseColor2;
    vec3 baseColor3;
    vec3 baseColor4;
    vec3 baseColor5;
    vec3 emiColor1;
    vec3 emiColor2;
    vec3 emiColor3;
    vec3 emiColor4;
    vec3 emiColor5;
    float emiIntensity1;
    float emiIntensity2;
    float emiIntensity3;
    float emiIntensity4;
    float emiIntensity5;
    int colorMethod;
    int effect;
    bool paradox;
};

//Layered
vec4 adjust(vec4 color) {
    return clamp(color * 2, 0, 1);
}

float adjustScalar(float color) {
    return clamp(color * 2, 0.0, 1.0);
}

vec3 applyEmission(vec3 base, vec3 emissionColor, float intensity) {
    return base + (emissionColor - base) * intensity;
}

vec4 getColor(sampler2D sampler, vec2 pixel) {
    return texture(sampler, pixel);
}

vec4 layered(vec2 pixel) {
    vec4 color = texture(diffuse, pixel);
    vec4 layerMasks = adjust(texture(layer, pixel));
    float maskColor = adjustScalar(texture(mask, pixel).r);

    vec3 base = mix(color.rgb, color.rgb * baseColor1, layerMasks.r);
    base = mix(base, color.rgb * baseColor2, layerMasks.g);
    base = mix(base, color.rgb * baseColor3, layerMasks.b);
    base = mix(base, color.rgb * baseColor4, layerMasks.a);
    base = mix(base, color.rgb * baseColor5, maskColor);

    base = mix(base, applyEmission(base, emiColor1, emiIntensity1), layerMasks.r);
    base = mix(base, applyEmission(base, emiColor2, emiIntensity2), layerMasks.g);
    base = mix(base, applyEmission(base, emiColor3, emiIntensity3), layerMasks.b);
    base = mix(base, applyEmission(base, emiColor4, emiIntensity4), layerMasks.a);
    base = mix(base, applyEmission(vec3(0), emiColor5, emiIntensity5), maskColor);

    return vec4(base, color.a);
}
//

//Masked
vec4 masked(vec2 pixel) {
    vec4 color = texture(diffuse , pixel);
    float maskColor = texture(mask, pixel).r;
    float emiAlpha = texture(emission, pixel).r * color.a;

    color.rgb = mix(color.rgb, color.rgb * baseColor1, maskColor);
    return color;
}
//

//Galaxy
const float darkenFactor = 0.3;

vec4 galaxy(vec4 color) {
    float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));

    const vec3 lightGradientColor1 = vec3(0.2, 0.0, 0.3);
    const vec3 lightGradientColor2 = vec3(0.6, 0.1, 0.7);
    const float gradientThreshold = 0.5;

    vec3 gradientColor = mix(
    lightGradientColor1,
    lightGradientColor2,
    smoothstep(gradientThreshold, 1.0, brightness)
    );

    color.rgb *= darkenFactor;
    color.rgb = mix(
    color.rgb,
    gradientColor,
    smoothstep(gradientThreshold, 1.0, brightness)
    );

    return color;
}
//
//Pastel
vec4 pastel(vec4 inColor, vec2 uv) {
    vec2 wrappedUV = fract(uv * 5.0);

    float gradient = sin(wrappedUV.x * 3.14159) * sin(wrappedUV.y * 3.14159);

    gradient = (gradient + 1.0) * 0.5;

    vec3 pastelBlue = vec3(0.8, 0.9, 1.0);
    vec3 pastelPink = vec3(1.0, 0.8, 0.9);

    vec3 pastelColor = mix(pastelBlue, pastelPink, gradient);

    return vec4(mix(inColor.rgb, pastelColor, 0.5), inColor.a);
}
//
//Shadow
vec4 shadow(vec4 inColor) {
    float grayscale = 0.2126 * inColor.r + 0.7152 * inColor.g + 0.0722 * inColor.b;

    vec3 baseColor = vec3(grayscale);

    vec2 wrappedUV = fract(texCoord0 * 5.0);
    float gradient = sin(wrappedUV.x * 3.14159) * sin(wrappedUV.y * 3.14159);

    gradient = (gradient + 1.0) * 0.5;

    vec3 deepPurpleBlue = vec3(0.1, 0.1, 0.2);
    vec3 darkerShade = vec3(0.05, 0.05, 0.1);

    vec3 shadowColor = mix(deepPurpleBlue, darkerShade, gradient);

    vec3 finalColor = mix(baseColor, shadowColor, 0.7);

    finalColor = finalColor * 0.9;

    finalColor = clamp(finalColor, 0.0, 1.0);

    return vec4(finalColor, inColor.a);
}
//
//Sketch
vec4 sketch(vec4 inColor) {
    float grayscale = 0.2126 * inColor.r + 0.7152 * inColor.g + 0.0722 * inColor.b;

    float luminanceDx = dFdx(grayscale);
    float luminanceDy = dFdy(grayscale);
    float edgeFactor = length(vec2(luminanceDx, luminanceDy));

    float outline = 1.0 - smoothstep(0.02, 0.05, edgeFactor);
    vec3 edgeColor = vec3(0.0);

    vec3 finalColor = mix(vec3(grayscale), edgeColor, outline);

    return vec4(finalColor, inColor.a);
}
//

//Vintage
vec4 vintage(vec4 inColor) {
    float grayscale = 0.2126 * inColor.r + 0.7152 * inColor.g + 0.0722 * inColor.b;

    return vec4(vec3(grayscale), inColor.a);
}
//

vec4 baseColor(vec2 samplerPixel) {

    if(colorMethod == 0) return texture(diffuse, samplerPixel);
    else if(colorMethod == 1) return layered(samplerPixel);
    else if(colorMethod == 2) return masked(samplerPixel);
    else return texture(diffuse, samplerPixel);
}

vec4 process(vec4 color, vec2 uv) {
    if(effect == 0) return color;
    else if(effect == 1) return galaxy(color);
    else if(effect == 2) return pastel(color, uv);
    else if(effect == 3) return shadow(color);
    else if(effect == 4) return sketch(color);
    else if(effect == 5) return vintage(color);
    else return color;
}

void main() {
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
    vec2 samplerPixel = (storePixel + 0.5) / outputSize;

    vec4 color = baseColor(samplerPixel);

    color = process(color, samplerPixel);

    if(paradox) {
       color.rgb = mix(color.rgb, vec3(1.0), texture(paradoxTexture, samplerPixel).r);
    }

    float emiAlpha = texture(emission, samplerPixel).r * color.a;

    imageStore(solidTex, storePixel, color);
    imageStore(litTex,   storePixel, vec4(color.rgb, emiAlpha));
}

