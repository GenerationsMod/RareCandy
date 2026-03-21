package gg.generations.framebuffertest.second;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

public final class FlatColorShader extends GLShaderProgram {
    private static final String VERTEX = """
            #version 430 core

            layout(location = 0) in vec3 aPosition;

            uniform mat4 uMvp;

            void main() {
                gl_Position = uMvp * vec4(aPosition, 1.0);
            }
            """;

    private static final String FRAGMENT = """
            #version 430 core

            out vec4 fragColor;
            uniform vec4 uColor;

            void main() {
                fragColor = uColor;
            }
            """;

    private final int colorLocation;
    private final int mvpLocation;
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public FlatColorShader() {
        super(VERTEX, FRAGMENT);
        colorLocation = uniformLocation("uColor");
        mvpLocation = uniformLocation("uMvp");
    }

    public void setColor(float r, float g, float b, float a) {
        glUniform4f(colorLocation, r, g, b, a);
    }

    public void setMvp(Matrix4f mvp) {
        matrixBuffer.clear();
        mvp.get(matrixBuffer);
        glUniformMatrix4fv(mvpLocation, false, matrixBuffer);
    }
}