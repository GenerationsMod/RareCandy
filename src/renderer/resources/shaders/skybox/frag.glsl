#version 330 core
#pragma optionNV(strict on)

in vec3 outTextureDir;

out vec4 outColor;

uniform samplerCube cubemap;

void main() {
    outColor = texture(cubemap, outTextureDir);
}
