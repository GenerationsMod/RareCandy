package gg.generations.framebuffertest.second;

import static org.lwjgl.opengl.GL20.glUniform1i;

public final class ScreenTextureShader extends GLShaderProgram {
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

            uniform sampler2D uTexture;

            void main() {
                fragColor = texture(uTexture, uv);
            }
            """;

    private final int textureLocation;

    public ScreenTextureShader() {
        super(VERTEX, FRAGMENT);
        textureLocation = uniformLocation("uTexture");
    }

    public void setTextureUnit(int unit) {
        glUniform1i(textureLocation, unit);
    }
}