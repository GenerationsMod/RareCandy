package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.BlankTexture;
import gg.generations.rarecandy.renderer.loading.ITexture;
import gg.generations.rarecandy.renderer.pipeline.Pipelines;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.Scope;
import gg.generations.rarecandy.renderer.pipeline.util.TextureIdSupplier;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.tools.gui.imgui.ImVector3f;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiCond;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ComputeShaderDemo {
    private long window;
    private int width = 1024, height = 1024;

    private gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline computeProgram;
    private TraditionalPipeline quadProgram;

    // Example control uniforms
    private float time = 0f;
    private float[] intensity = new float[]{1f};
    private ImVector3f color = new ImVector3f(1f, 0f, 0f);
    private ImVector3f color2 = new ImVector3f(0, 1f, 0f);

    private ImGuiImplGlfw imguiGlfw;
    private ImGuiImplGl3 imguiGl3;
    private BlankTexture target;

    public static void main(String[] args) {
        new ComputeShaderDemo().run();
    }

    public void run() {
        initWindow();
        initImGui();
        initGL();
        loop();
        cleanup();
    }

    private void initWindow() {
        if (!glfwInit()) throw new IllegalStateException("Unable to init GLFW");
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        window = glfwCreateWindow(1024, 1024, "Compute Shader Demo", NULL, NULL);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
    }

    private void initImGui() {
        ImGui.createContext();
        imguiGlfw = new ImGuiImplGlfw();
        imguiGl3 = new ImGuiImplGl3();
        imguiGlfw.init(window, true);
        imguiGl3.init("#version 430");
    }

    private void initGL() {
        // --- Texture ---
        target = new BlankTexture(ITexture.Type.RGBA_BYTE, 1024, 1024, ITexture.ComputeAccess.WRITE_ONLY);

        // --- Compute Shader ---
        String computeSrc = """
            #version 430
            layout (local_size_x = 16, local_size_y = 16) in;

            layout (rgba8, binding = 0) uniform writeonly image2D destTex;

            uniform float u_time;
            uniform float u_intensity;
            uniform vec3 u_color;
            uniform vec3 u_color_2;

            void main() {
                ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
                vec2 uv = pixel / vec2(imageSize(destTex));
                float v = 0.5 + 0.5 * sin(u_time + (20.0 * u_intensity) * length(uv - 0.5));
                vec3 col = mix(u_color, u_color_2, v);
                imageStore(destTex, pixel, vec4(col, 1.0));
            }
            """;
        computeProgram = Pipelines.compute(computeSrc)
                .autoFloat(Scope.GLOBAL, "u_time", (instance, object) -> time)
                .autoFloat(Scope.GLOBAL, "u_intensity", (instance, object) -> intensity[0])
                .autoVec3(Scope.GLOBAL, "u_color", (instance, object) -> color.getValue())
                .autoVec3(Scope.GLOBAL, "u_color_2", (instance, object) -> color2.getValue())
                .autoImage2D(Scope.GLOBAL, "destTex", 0, (instance, object) -> target)
                .build();

        // --- Quad Shader ---
        String quadVert = """
            #version 430
            out vec2 v_uv;
            void main() {
                vec2 pos = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
                v_uv = pos;
                gl_Position = vec4(pos * 2.0 - 1.0, 0, 1);
            }
            """;
        String quadFrag = """
            #version 430
            in vec2 v_uv;
            out vec4 fragColor;
            uniform sampler2D tex;
            void main() {
                fragColor = texture(tex, v_uv);
            }
            """;
        quadProgram = Pipelines.traditional(quadVert, quadFrag)
                .autoSampler2D(Scope.GLOBAL, "tex", 0, new TextureIdSupplier() {
                    @Override
                    public int get(ObjectInstance instance, RenderObject object) {
                        return target.getId();
                    }
                }).build();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            time += 0.016f;

            computeProgram.useProgram();
            computeProgram.bindGlobal(null, null);

            // --- Run compute ---
            computeProgram.dispatch(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT, (width + 15) / 16, (height + 15) / 16, 1);

            // --- Draw quad ---
            glClear(GL_COLOR_BUFFER_BIT);
            quadProgram.useProgram();
            quadProgram.bindGlobal(null, null);

            glBindTexture(GL_TEXTURE_2D, target.getId());
            glDrawArrays(GL_TRIANGLES, 0, 3);

            // --- UI ---
            imguiGlfw.newFrame();
            imguiGl3.newFrame();
            ImGui.newFrame();

            color.render("Color 1");
            color2.render("Color 2");

            if(ImGui.sliderFloat("Intensity", intensity, 0f, 1f)) {

            }


            ImGui.setNextWindowPos(200, 300, ImGuiCond.Always);
            ImGui.setNextWindowSize(512, 512, ImGuiCond.Always);
            ImGui.begin("derp");
            ImGui.image(target.getId(), 512, 512); // show the 1024x1024 image

            ImGui.end();

            ImGui.render();
            imguiGl3.renderDrawData(ImGui.getDrawData());

            glfwSwapBuffers(window);
        }
    }

    private static final float[] colorArray = new float[3];

    private int compileProgram(String vert, String frag) {
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, vert);
        glCompileShader(vs);
        if (glGetShaderi(vs, GL_COMPILE_STATUS) == 0)
            throw new RuntimeException(glGetShaderInfoLog(vs));

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, frag);
        glCompileShader(fs);
        if (glGetShaderi(fs, GL_COMPILE_STATUS) == 0)
            throw new RuntimeException(glGetShaderInfoLog(fs));

        int prog = glCreateProgram();
        glAttachShader(prog, vs);
        glAttachShader(prog, fs);
        glLinkProgram(prog);
        if (glGetProgrami(prog, GL_LINK_STATUS) == 0)
            throw new RuntimeException(glGetProgramInfoLog(prog));

        glDeleteShader(vs);
        glDeleteShader(fs);
        return prog;
    }

    private void cleanup() {
        imguiGl3.shutdown();
        imguiGlfw.shutdown();
        ImGui.destroyContext();
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}
