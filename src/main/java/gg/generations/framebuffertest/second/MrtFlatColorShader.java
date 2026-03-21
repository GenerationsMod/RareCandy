package gg.generations.framebuffertest.second;

import static org.lwjgl.opengl.GL20.glUniform4f;

public final class MrtFlatColorShader extends GLShaderProgram {
    private static final String VERTEX = """
            #version 430 core

            layout(location = 0) in vec2 aPosition;

            void main() {
                gl_Position = vec4(aPosition, 0.0, 1.0);
            }
            """;

    private static final String FRAGMENT = """
            #version 430 core

            layout(location = 0) out vec4 outColor0;
            layout(location = 1) out vec4 outColor1;

            uniform vec4 uColor0;
            uniform vec4 uColor1;

            void main() {
                outColor0 = uColor0;
                outColor1 = uColor1;
            }
            """;

    private final int color0Location;
    private final int color1Location;

    public MrtFlatColorShader() {
        super(VERTEX, FRAGMENT);
        color0Location = uniformLocation("uColor0");
        color1Location = uniformLocation("uColor1");
    }

    public void setColors(
            float r0, float g0, float b0, float a0,
            float r1, float g1, float b1, float a1
    ) {
        glUniform4f(color0Location, r0, g0, b0, a0);
        glUniform4f(color1Location, r1, g1, b1, a1);
    }
}