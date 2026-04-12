struct TargetVertex {
    vec3 position;
    vec2 texcoord;
    vec3 normal;
    vec3 tangent;
    vec3 bitangent;
};

struct SourceVertex {
    vec3 position;
    vec2 texcoord;
    vec3 normals;
    vec4 tangents;
    uvec4 joints;
    vec4 weights;
};

struct Variant {
    int material;
    int effect;
    bool paradox;
};

struct Material {
    int diffuse;
    int layer;
    int mask;
    int emission;
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
    bool useLight;

    int colorMethod;
    bool translucent;
};

struct Instance {
    mat4 modelMatrix;
    mat3 normalMatrix;
    mat4 boneTransforms[220];
};

struct Transform {
    vec2 scale;
    vec2 offset;
    int variant;
    int instance;
    bool shouldRender;
};

layout(std430, binding = 0) readonly  buffer VertexBuffer {
    SourceVertex src[];
};

layout(std430, binding = 1) readonly  buffer IndexBuffer {
    uint indices[];
};
layout(std430, binding = 2) readonly  buffer MeshOffsetBuffer {
    uint meshOffsets[];
};

layout(std430, binding = 3) readonly buffer VariantBuffer {
    Variant variants[];
};

layout(std430, binding = 4) readonly buffer MaterialBuffer {
    Material materials[];
};

layout(std430, binding = 5) readonly  buffer InstanceBuffer {
    Instance instances[];
};

layout(std140, binding = 6) readonly  buffer TransformBuffer {
    Transform transforms[];
};
