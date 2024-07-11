package gg.generations.rarecandy.renderer.rendering;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class ScreenRenderer {
    private int shaderProgram;
    private int quadVAO;
    private int framebufferTexture;

    public ScreenRenderer(int framebufferTexture) {
        this.framebufferTexture = framebufferTexture;

        // Compile shaders
        shaderProgram = createShaderProgram();

        // Setup quad vertices
        setupQuad();
    }

    private int createShaderProgram() {
        // Vertex shader source
        String vertexShaderSrc = "#version 330 core\n" +
                "layout(location = 0) in vec2 aPos;\n" +
                "layout(location = 1) in vec2 aTexCoord;\n" +
                "out vec2 TexCoord;\n" +
                "void main() {\n" +
                "    gl_Position = vec4(aPos.x, aPos.y, 0.0, 1.0);\n" +
                "    TexCoord = aTexCoord;\n" +
                "}\n";

        // Fragment shader source
        String fragmentShaderSrc = "#version 330 core\n" +
                "in vec2 TexCoord;\n" +
                "out vec4 FragColor;\n" +
                "uniform sampler2D framebufferTexture;\n" +
                "void main() {\n" +
                "    FragColor = texture(framebufferTexture, TexCoord);\n" +
                "}\n";

        // Compile vertex shader
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSrc);
        glCompileShader(vertexShader);
        checkCompileErrors(vertexShader, "VERTEX");

        // Compile fragment shader
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSrc);
        glCompileShader(fragmentShader);
        checkCompileErrors(fragmentShader, "FRAGMENT");

        // Link shaders
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        checkCompileErrors(shaderProgram, "PROGRAM");

        // Delete the shaders as they're linked into our program now and no longer necessary
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return shaderProgram;
    }

    private void checkCompileErrors(int shader, String type) {
        int[] success = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, success);
        if (success[0] == GL_FALSE) {
            int maxLength = glGetShaderi(shader, GL_INFO_LOG_LENGTH);
            String log = glGetShaderInfoLog(shader, maxLength);
            throw new RuntimeException("Shader compilation error of type " + type + ":\n" + log);
        }
    }

    private void setupQuad() {
        float[] vertices = {
                // Positions          // Texture coordinates
                -1.0f,  1.0f,         0.0f, 1.0f,  // Top left
                -1.0f, -1.0f,         0.0f, 0.0f,  // Bottom left
                1.0f, -1.0f,         1.0f, 0.0f,  // Bottom right
                1.0f,  1.0f,         1.0f, 1.0f   // Top right
        };

        int[] indices = {
                0, 1, 3,  // First triangle
                1, 2, 3   // Second triangle
        };

        // Create and bind VAO
        quadVAO = glGenVertexArrays();
        glBindVertexArray(quadVAO);

        // Create and bind VBO
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Create and bind EBO
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Set vertex attribute pointers
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Unbind VAO
        glBindVertexArray(0);
    }

    public void render() {
        // Use the shader program
        glUseProgram(shaderProgram);

        // Set framebuffer texture as uniform
        int texLoc = glGetUniformLocation(shaderProgram, "framebufferTexture");
        glUniform1i(texLoc, 0); // Use texture unit 0

        // Bind VAO
        glBindVertexArray(quadVAO);

        // Draw quad
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        // Unbind VAO
        glBindVertexArray(0);
    }

    public void close() {
        // Cleanup resources
        glDeleteVertexArrays(quadVAO);
        glDeleteProgram(shaderProgram);
    }
}