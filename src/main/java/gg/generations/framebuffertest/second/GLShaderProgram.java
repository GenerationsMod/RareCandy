package gg.generations.framebuffertest.second;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class GLShaderProgram implements AutoCloseable {
    protected final int programId;

    public GLShaderProgram(String vertexSource, String fragmentSource) {
        int vertexShader = compile(GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compile(GL_FRAGMENT_SHADER, fragmentSource);

        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(programId);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            glDeleteProgram(programId);
            throw new IllegalStateException("Program link failed:\n" + log);
        }

        glDetachShader(programId, vertexShader);
        glDetachShader(programId, fragmentShader);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private static int compile(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException("Shader compile failed:\n" + log);
        }

        return shader;
    }

    public void use() {
        glUseProgram(programId);
    }

    public int uniformLocation(String name) {
        return glGetUniformLocation(programId, name);
    }

    @Override
    public void close() {
        glDeleteProgram(programId);
    }
}