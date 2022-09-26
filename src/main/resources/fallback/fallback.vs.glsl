uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;

void main() {
    mat4 worldSpace = projectionMatrix * viewMatrix;
    vec4 worldPosition = modelMatrix * vec4(inPosition, 1.0);

    gl_Position = worldSpace * worldPosition;
}