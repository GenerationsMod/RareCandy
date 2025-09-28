package gg.generations.rarecandy.tools;// LWJGL 3 - OpenGL 4.3+ Vertex Pulling + glMultiDrawArraysIndirect (Fully Annotated Line-by-Line)

import gg.generations.rarecandy.renderer.model.GenericSSBO;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils; // Allocate NIO buffers
import org.lwjgl.opengl.GL; // Init OpenGL bindings

import java.nio.IntBuffer; // For draw command data

import static org.lwjgl.glfw.GLFW.*; // GLFW window/input
import static org.lwjgl.opengl.GL43.*; // OpenGL 4.3 core API

public class VertexPullingTest { // Entry class

    static final String VERT = """ 
        // Vertex shader source
        #version 460 core
        #extension GL_ARB_shader_draw_parameters : require

        layout(std430, binding = 0) buffer VertexBuffer { // SSBO for vertex positions
            vec3 positions[]; // Array of vec3 vertex positions
        };
        
        layout(std430, binding = 1) buffer ColorBuffer { // SSBO for instance matrices
            vec4 colors[]; // Array of mat4 model transforms
        };


        layout(std430, binding = 2) buffer InstanceBuffer { // SSBO for instance matrices
            mat4 models[]; // Array of mat4 model transforms
        };

        uniform mat4 viewProj; // Combined view-projection matrix
        
        out vec4 color;
        
        void main() { // Shader entry
            vec3 pos = positions[gl_VertexID]; // Pull vertex from SSBO
            mat4 model = models[gl_InstanceID + gl_BaseInstance]; // Pull model matrix using extension
            gl_Position = viewProj * model * vec4(pos, 1.0); // Transform position
            color = colors[gl_VertexID];
        }
    """; // End vertex shader

    static final String FRAG = """ 
        // Fragment shader source
        #version 460 core // GLSL version 430
                #extension GL_ARB_shader_draw_parameters : require
        in vec4 color;
        out vec4 FragColor; // Output color
        void main() { // Shader entry
        FragColor = color; // Test varying outputs
        }
    """; // End fragment shader

    public static void main(String[] args) { // Program entry
        if (!glfwInit()) throw new RuntimeException("GLFW init failed"); // Init GLFW

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4); // Request OpenGL 4.x
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5); // Request OpenGL x.3
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Core profile

        long win = glfwCreateWindow(800, 800, "Vertex Pulling", 0, 0); // Make window
        if (win == 0L) throw new RuntimeException("Window creation failed"); // Fail check
        glfwMakeContextCurrent(win); // Activate context
        glfwSwapInterval(1); // Enable VSync
        GL.createCapabilities(); // Load OpenGL

        glViewport(0, 0, 800, 800); // Set viewport
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f); // Background color

        int dummyVAO = glGenVertexArrays(); // Create dummy VAO (required)
        glBindVertexArray(dummyVAO); // Bind VAO

        var projection = new Matrix4f().ortho2D(-1, 1, -1, 1);
        Pipeline pipeline = new Pipeline.Builder().shader(VERT, FRAG)
                .supplyUniform("viewProj", ctx -> ctx.uniform().uploadMat4f(projection))
                .build();

        var vertices = new Vector3f[]{ // Triangle vertex positions
                new Vector3f(-0.5f, -0.5f, 0f),
                new Vector3f(0.5f, -0.5f, 0f),
                new Vector3f(0f,    0.5f, 0f)
        };

        var vertexSSBO = new GenericSSBO<>(0, 3, GL_STATIC_DRAW, SSBOUtils.VECTOR3F);
        vertexSSBO.upload(vertices);
        vertexSSBO.bind();

        var colors = new Vector4f[]{
                new Vector4f(0.0f, 0.0f, 1.0f, 1.0f), // Vertex 0 = Blue
                new Vector4f(0.0f, 1.0f, 0.0f, 1.0f), // Vertex 1 = Green
                new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)  // Vertex 2 = Red
        };

        var colorsSSBO = new GenericSSBO<>(1, 3, GL_STATIC_DRAW, SSBOUtils.VECTOR4F);
        colorsSSBO.upload(colors);
        colorsSSBO.bind();

        var instanceSSBO = new GenericSSBO<>(2, 1, GL_DYNAMIC_DRAW, SSBOUtils.MATRIX4F);
        instanceSSBO.updateSubData(0, new Matrix4f().rotateZ((float) Math.toRadians(90)));
        instanceSSBO.bind();

        IntBuffer drawBuf = BufferUtils.createIntBuffer(4); // Indirect draw struct
        drawBuf.put(3); // vertexCount = 3
        drawBuf.put(1); // instanceCount = 1
        drawBuf.put(0); // firstVertex = 0
        drawBuf.put(0); // baseInstance = 0
        drawBuf.flip(); // Flip for upload

        int indirect = glGenBuffers(); // Create indirect buffer
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirect); // Bind it
        glBufferData(GL_DRAW_INDIRECT_BUFFER, drawBuf, GL_STATIC_DRAW); // Upload command

        while (!glfwWindowShouldClose(win)) { // Render loop
            glClear(GL_COLOR_BUFFER_BIT); // Clear frame

            pipeline.bind(null);

            pipeline.updateOtherUniforms(null, null);
            pipeline.updateTexUniforms(null, null);

            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, indirect); // Bind indirect
            glMultiDrawArraysIndirect(GL_TRIANGLES, 0, 1, 0); // Execute draw

            glfwSwapBuffers(win); // Display frame
            glfwPollEvents(); // Handle input
        }

        glfwTerminate(); // Clean up GLFW
    }

}
