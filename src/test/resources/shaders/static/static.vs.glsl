out vec2 texCoord0;
out vec3 toLightVector;
out vec3 toCameraVector;
out vec3 normal;

uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform vec3 lightPosition;

void main() {
    texCoord0 = inTexCoords;

    mat4 worldSpace = projectionMatrix * viewMatrix;
    vec4 worldPosition = modelMatrix * vec4(inPosition, 1.0);

    gl_Position = worldSpace * worldPosition;
    normal = (modelMatrix * vec4(inNormal, 0.0)).xyz;
    toLightVector = lightPosition - worldPosition.xyz;
    toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;
}