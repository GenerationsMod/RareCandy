const int DIFFUSE = 0;
const int LAYER = 1;
const int MASK = 2;
const int EMISSION = 3;

struct TargetVertex {
    vec3 position;
    vec2 texCoord;
    vec3 normal;
    vec3 tangent;
    vec3 bitangent;
};

struct SourceVertex {
    vec3 position;
    vec2 texCoord;
    vec3 normals;
    vec4 tangents;
    uvec4 joints;
    vec4 weights;
};

struct Transform {
    vec2 scale;
    vec2 offset;
};

struct Variant {
    Transform[4] transforms;
    int material;
    int effect;
    bool paradox;
};

struct Material {
    int[4] images;
    vec3[5] baseColor;
    vec3[5] emiColor;
    float[5] emiIntensity;
    bool useLight;
    int colorMethod;
    bool translucent;
};

struct Instance {
    mat4 modelMatrix;
    mat3 normalMatrix;
    mat4 boneTransforms[220];
};

struct DrawInfo {
    int variant;
    int instance;
    int mesh;
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

layout(std140, binding = 6) readonly  buffer DrawInfoBuffer {
    DrawInfo drawInfos[];
};
