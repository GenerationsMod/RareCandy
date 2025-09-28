package gg.generations.rarecandy.tools;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MandelbrotCompute {
    private long window;
    private int computeProgram, quadProgram;
    private int vao, tex;

    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double zoom = 3.0;

    private double lastMouseX, lastMouseY;
    private boolean dragging = false;

    public static void main(String[] args) {
        new MandelbrotCompute().run();
    }

    private void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        if (!glfwInit()) throw new IllegalStateException("Unable to init GLFW");
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(800, 600, "Mandelbrot Compute Shader", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        GL.createCapabilities();

        // Input callbacks
        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    dragging = true;
                    DoubleBuffer xBuf = BufferUtils.createDoubleBuffer(1);
                    DoubleBuffer yBuf = BufferUtils.createDoubleBuffer(1);
                    glfwGetCursorPos(window, xBuf, yBuf);
                    lastMouseX = xBuf.get(0);
                    lastMouseY = yBuf.get(0);
                } else if (action == GLFW_RELEASE) {
                    dragging = false;
                }
            }
        });

        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            zoom *= Math.pow(1.1, -yoffset);
        });

        // Shaders
        computeProgram = createComputeProgram(computeShaderSrc);
        quadProgram = createQuadProgram(vertexShaderSrc, quadFragmentShaderSrc);

        // Fullscreen quad VAO
        vao = glGenVertexArrays();

        // Texture
        tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();

            handleKeyboard();

            // Mouse drag panning
            if (dragging) {
                DoubleBuffer xBuf = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuf = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuf, yBuf);
                double x = xBuf.get(0);
                double y = yBuf.get(0);
                int[] w = new int[1], h = new int[1];
                glfwGetFramebufferSize(window, w, h);
                offsetX += (x - lastMouseX) * zoom / w[0];
                offsetY -= (y - lastMouseY) * zoom / h[0];
                lastMouseX = x;
                lastMouseY = y;
            }

            int[] width = new int[1];
            int[] height = new int[1];
            glfwGetFramebufferSize(window, width, height);

            // Resize texture if needed
            glBindTexture(GL_TEXTURE_2D, tex);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, width[0], height[0], 0, GL_RGBA, GL_FLOAT, (java.nio.ByteBuffer) null);
            glBindImageTexture(0, tex, 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);

            // Compute shader dispatch
            glUseProgram(computeProgram);
            glUniform1f(glGetUniformLocation(computeProgram, "zoom"), (float) zoom);
            glUniform2f(glGetUniformLocation(computeProgram, "offset"), (float) offsetX, (float) offsetY);
            glUniform2i(glGetUniformLocation(computeProgram, "resolution"), width[0], height[0]);

            glDispatchCompute((width[0] + 15) / 16, (height[0] + 15) / 16, 1);
            glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

            // Draw quad with texture
            glViewport(0, 0, width[0], height[0]);
            glClear(GL_COLOR_BUFFER_BIT);
            glUseProgram(quadProgram);
            glBindVertexArray(vao);
            glBindTexture(GL_TEXTURE_2D, tex);
            glDrawArrays(GL_TRIANGLES, 0, 3);

            glfwSwapBuffers(window);
        }
    }

    private void handleKeyboard() {
        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) offsetY -= zoom * 0.05;
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) offsetY += zoom * 0.05;
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) offsetX -= zoom * 0.05;
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) offsetX += zoom * 0.05;

        if (glfwGetKey(window, GLFW_KEY_EQUAL) == GLFW_PRESS) zoom *= 0.95; // + key zoom in
        if (glfwGetKey(window, GLFW_KEY_MINUS) == GLFW_PRESS) zoom *= 1.05; // - key zoom out

        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) { // reset
            offsetX = 0.0;
            offsetY = 0.0;
            zoom = 3.0;
        }
    }

    private void cleanup() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private int createComputeProgram(String src) {
        int shader = glCreateShader(GL_COMPUTE_SHADER);
        glShaderSource(shader, src);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException(glGetShaderInfoLog(shader));

        int program = glCreateProgram();
        glAttachShader(program, shader);
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE)
            throw new RuntimeException(glGetProgramInfoLog(program));

        glDeleteShader(shader);
        return program;
    }

    private int createQuadProgram(String vsrc, String fsrc) {
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, vsrc);
        glCompileShader(vs);
        if (glGetShaderi(vs, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException(glGetShaderInfoLog(vs));

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, fsrc);
        glCompileShader(fs);
        if (glGetShaderi(fs, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException(glGetShaderInfoLog(fs));

        int prog = glCreateProgram();
        glAttachShader(prog, vs);
        glAttachShader(prog, fs);
        glLinkProgram(prog);
        if (glGetProgrami(prog, GL_LINK_STATUS) == GL_FALSE)
            throw new RuntimeException(glGetProgramInfoLog(prog));

        glDeleteShader(vs);
        glDeleteShader(fs);
        return prog;
    }

    // --- Shaders ---
    private static final String computeShaderSrc = """
        #version 430
        layout(local_size_x = 16, local_size_y = 16) in;
        layout(rgba32f, binding = 0) uniform image2D img;
        uniform float zoom;
        uniform vec2 offset;
        uniform ivec2 resolution;

        void main() {
            ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
            if (pixel.x >= resolution.x || pixel.y >= resolution.y) return;

            float aspect = float(resolution.x) / float(resolution.y);
            vec2 uv = (vec2(pixel) / vec2(resolution)) - 0.5;
            vec2 c = vec2(uv.x * zoom * aspect + offset.x,
                          uv.y * zoom + offset.y);

            vec2 z = vec2(0.0);
            int i;
            for (i = 0; i < 10000; i++) {
                float x = z.x*z.x - z.y*z.y + c.x;
                float y = 2.0*z.x*z.y + c.y;
                if (x*x + y*y > 4.0) break;
                z = vec2(x, y);
            }
            float t = float(i) / 300.0;
            vec4 color = vec4(t, t*t, pow(t, 0.5), 1.0);
            imageStore(img, pixel, color);
        }
    """;

    private static final String vertexShaderSrc = """
        #version 330 core
        const vec2 verts[3] = vec2[3](vec2(-1,-1), vec2(3,-1), vec2(-1,3));
        out vec2 uv;
        void main() {
            gl_Position = vec4(verts[gl_VertexID], 0.0, 1.0);
            uv = (verts[gl_VertexID] + 1.0) * 0.5;
        }
    """;

    private static final String quadFragmentShaderSrc = """
        #version 330 core
        in vec2 uv;
        out vec4 fragColor;
        uniform sampler2D tex;
        void main() {
            fragColor = texture(tex, uv);
        }
    """;
}
