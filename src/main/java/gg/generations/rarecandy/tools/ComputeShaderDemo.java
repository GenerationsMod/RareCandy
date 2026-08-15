package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.renderer.launch.OpenGL;
import gg.generations.rarecandy.renderer.pipeline.util.UniformUploadContext;
import gg.generations.rarecandy.renderer.textures.BlankTexture;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.pipeline.Pipelines;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.Scope;
import gg.generations.rarecandy.renderer.pipeline.util.TextureIdSupplier;
import gg.generations.rarecandy.tools.gui.imgui.ImVector3f;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ComputeShaderDemo extends AppBase {
    private long window;
    private int width = 1024, height = 1024;

    private gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline computeProgram;
    private TraditionalPipeline quadProgram;

    // Example control uniforms
    private float time = 0f;
    private final float[] intensity = new float[]{1f};
    private final ImVector3f color = new ImVector3f(1f, 0f, 0f);
    private final ImVector3f color2 = new ImVector3f(0, 1f, 0f);

    private BlankTexture target;

    public ComputeShaderDemo() {
        super("Compute Shader Test", 1024, 1024, new OpenGL());
    }

    public static void main(String[] args) {
        new ComputeShaderDemo().run();
    }

    public void initGL() {
        // --- Texture ---
        target = new BlankTexture(ITexture.Type.RGBA8, 1024, 1024, ITexture.ComputeAccess.WRITE_ONLY);

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
                .autoFloat(Scope.GLOBAL, "u_time", ctx -> time)
                .autoFloat(Scope.GLOBAL, "u_intensity", ctx -> intensity[0])
                .autoVec3(Scope.GLOBAL, "u_color", ctx -> color.getValue())
                .autoVec3(Scope.GLOBAL, "u_color_2", ctx -> color2.getValue())
                .autoImage2D(Scope.GLOBAL, "destTex", 0, ctx -> target)
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
                    public int get(UniformUploadContext ctx) {
                        return target.id();
                    }
                }).build();
    }

    @Override
    protected void renderGui() {
        color.render("Color 1");
        color2.render("Color 2");

        if(ImGui.sliderFloat("Intensity", intensity, 0f, 1f)) {

        }


//        ImGui.setNextWindowPos(200, 300, ImGuiCond.Always);
//        ImGui.setNextWindowSize(512, 512, ImGuiCond.Always);
        ImGui.begin("derp");
        ImGui.image(target.id(), 512, 512); // show the 1024x1024 image

        ImGui.end();
    }

    @Override
    protected void render() {
        time += 0.016f;

        computeProgram.useProgram();
        computeProgram.bindGlobal();

        // --- Run compute ---
        computeProgram.dispatch(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT, (width + 15) / 16, (height + 15) / 16, 1);

        // --- Draw quad ---
        glClear(GL_COLOR_BUFFER_BIT);
        quadProgram.useProgram();
        quadProgram.bindGlobal();

        glBindTexture(GL_TEXTURE_2D, target.id());
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }
}
