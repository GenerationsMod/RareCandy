vec4 process(vec2 uv) {
    vec4 inColor = getColor(uv);
    float grayscale = 0.2126 * inColor.r + 0.7152 * inColor.g + 0.0722 * inColor.b;

    return vec4(vec3(grayscale), inColor.a);
}