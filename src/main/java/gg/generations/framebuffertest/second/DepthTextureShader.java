package gg.generations.framebuffertest.second;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;

public final class DepthTextureShader extends GLShaderProgram {
    private static final String VERTEX = """
            #version 430 core

            out vec2 uv;

            const vec2 POSITIONS[3] = vec2[](
                vec2(-1.0, -1.0),
                vec2( 3.0, -1.0),
                vec2(-1.0,  3.0)
            );

            const vec2 UVS[3] = vec2[](
                vec2(0.0, 0.0),
                vec2(2.0, 0.0),
                vec2(0.0, 2.0)
            );

            void main() {
                gl_Position = vec4(POSITIONS[gl_VertexID], 0.0, 1.0);
                uv = UVS[gl_VertexID];
            }
            """;

    private static final String FRAGMENT = """
            #version 430 core

            in vec2 uv;
            out vec4 fragColor;

            uniform sampler2D uDepth;
            uniform float uNear;
            uniform float uFar;

            float linearizeDepth(float depth) {
                float z = depth * 2.0 - 1.0;
                return (2.0 * uNear * uFar) / (uFar + uNear - z * (uFar - uNear));
            }

            void main() {
                float depth = texture(uDepth, uv).r;
                float linear = linearizeDepth(depth) / uFar;
                fragColor = vec4(vec3(clamp(linear, 0.0, 1.0)), 1.0);
            }
            """;

    private final int textureLocation;
    private final int nearLocation;
    private final int farLocation;

    public DepthTextureShader() {
        super(VERTEX, FRAGMENT);
        textureLocation = uniformLocation("uDepth");
        nearLocation = uniformLocation("uNear");
        farLocation = uniformLocation("uFar");
    }

    public void setTextureUnit(int unit) {
        glUniform1i(textureLocation, unit);
    }

    public void setNearFar(float near, float far) {
        glUniform1f(nearLocation, near);
        glUniform1f(farLocation, far);
    }
}