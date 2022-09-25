uniform mat4 MC_view;
uniform mat4 MC_model;
uniform mat4 MC_projection;

void main() {
    mat4 worldSpace = MC_projection * MC_view;
    vec4 worldPosition = MC_model * vec4(inPosition, 1.0);

    gl_Position = worldSpace * worldPosition;
}