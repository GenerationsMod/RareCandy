#version 430
layout(local_size_x = 16, local_size_y = 16) in;
layout(rgba32f, binding = 0) uniform image2D img;
uniform float zoom;
uniform vec2 offset;
uniform ivec2 resolution;

layout(std430, binding = 0) buffer Geometry {
    struct Vertex {
        vec3 pos;
        vec3 normal;
        vec2 uv;
        vec4 boneIds;
        ivec4 boneWeights;
    };

    Vertex vertices[];
};

layout(std430, binding = 0) buffer Geometry {
    struct Vertex {
        vec3 pos;
        vec3 normal;
        vec2 uv;
        vec4 boneIds;
        ivec4 boneWeights;
    };

    Vertex vertices[];
};

layout(std430, binding = 1) buffer Meshes {
    struct Mesh {
        uint firstVertex;
        uint vertexCount;
    };

    Mesh meshes[];
};

struct Transform {
    vec2 scale;
    vec2 scale;
};

layout(std430, binding = 2) buffer Variants {
    struct Variant {
        bool hide;
        uint materialId;
        vec2 transform;
    };
};

void main() {
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
    if (pixel.x >= resolution.x || pixel.y >= resolution.y) return;

    float aspect = float(resolution.x) / float(resolution.y);
    vec2 uv = (vec2(pixel) / vec2(resolution)) - 0.5;
    vec2 c = vec2(uv.x * zoom * aspect + offset.x,
    uv.y * zoom + offset.y);

    vec2 z = vec2(0.0);
    int i;
    for (i = 0; i < 10000; i++) {
        float x = z.x*z.x - z.y*z.y + c.x;
        float y = 2.0*z.x*z.y + c.y;
        if (x*x + y*y > 4.0) break;
        z = vec2(x, y);
    }
    float t = float(i) / 300.0;
    vec4 color = vec4(t, t*t, pow(t, 0.5), 1.0);
    imageStore(img, pixel, color);
}