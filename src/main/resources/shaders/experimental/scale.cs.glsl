#version 430 core

layout(local_size_x = 16, local_size_y = 16) in;
uniform sampler2D inputImage;
layout(binding = 0, rgba8) writeonly uniform image2D outputImage;

void main() {
    ivec2 outCoord = ivec2(gl_GlobalInvocationID.xy);
    ivec2 outSize  = imageSize(outputImage);

    if (outCoord.x >= outSize.x || outCoord.y >= outSize.y)
    return;

    vec2 uv = (vec2(outCoord) + 0.5) / vec2(outSize);

    vec4 color = texture(inputImage, uv);

    imageStore(outputImage, outCoord, color);
}