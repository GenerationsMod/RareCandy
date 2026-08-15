package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.pokeutils.resource.JarResourceReader;
import gg.generations.rarecandy.pokeutils.resource.ResourceLocator;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.renderer.launch.OpenGL;
import gg.generations.rarecandy.renderer.pipeline.ShaderSource;
import gg.generations.rarecandy.renderer.pipeline.SnippetFinder;
import gg.generations.rarecandy.renderer.pipeline.util.TextureSupplier;
import gg.generations.rarecandy.renderer.pipeline.util.UniformUploadContext;
import gg.generations.rarecandy.renderer.textures.BlankTexture;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.pipeline.Pipelines;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.pipeline.util.Scope;
import gg.generations.rarecandy.renderer.pipeline.util.TextureIdSupplier;
import gg.generations.rarecandy.renderer.textures.Texture;
import gg.generations.rarecandy.tools.gui.imgui.ImVector3f;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImFloat;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SpecularGenerator extends AppBase {
    private long window;
    private Vector2i size = new Vector2i();

    private gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline computeProgram;
    private TraditionalPipeline quadProgram;

    // Example control uniforms
    private float time = 0f;
    private final ImFloat scale = new ImFloat(1.0f);
    private final ImVector3f color = new ImVector3f(1f, 0f, 0f);
    private final ImVector3f color2 = new ImVector3f(0, 1f, 0f);

    private BlankTexture normal;
    private BlankTexture specular;
    Texture texture;
    private boolean isDirty;

    private String name = "histy_ref";


    public SpecularGenerator() {
        super("Specular Generator", 1024, 1024, new OpenGL());
    }

    public static void main(String[] args) {
        new SpecularGenerator().run();
    }

    public void initGL() {
        // --- Texture ---


        try {
            texture = new Texture(Texture.read(ResourceLocator.of(Path.of("")).getFile(name + ".png"), 4));
            texture.init();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        specular = new BlankTexture(ITexture.Type.RGBA8, texture.width(), texture.height(), ITexture.ComputeAccess.WRITE_ONLY);
        normal = new BlankTexture(ITexture.Type.RGBA8, texture.width(), texture.height(), ITexture.ComputeAccess.WRITE_ONLY);
        size.set(texture.width(), texture.height());

        String computeSrc = """
            #version 430
            layout (local_size_x = 16, local_size_y = 16) in;

            layout (rgba8, binding = 0) uniform writeonly image2D normalTex;
            layout (rgba8, binding = 1) uniform writeonly image2D specularTex;
            uniform sampler2D srcTex;

            #lib:utils

            void main() {
                ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
                ivec2 size = imageSize(normalTex);
                
                imageStore(normalTex, pixel, vec4(normal(srcTex, -2.0, pixel, size), 1.0));
                imageStore(specularTex, pixel, vec4(vec3(luminance(srcTex, pixel, size)), 1.0));
            }
            """;
        // --- Compute Shader ---

        var utils = SnippetFinder.create(new JarResourceReader("shaders/snippets", ShaderSource.class)).addSnippet("lib", "utils");

        var source = ShaderSource.create().finder(utils).compile(computeSrc);

        System.out.println(source);

        computeProgram = Pipelines.compute(source)
                .autoImage2D(Scope.GLOBAL, "normalTex", 0, ctx -> normal)
                .autoSampler2D(Scope.GLOBAL, "srcTex", 0, ctx -> texture.id())
                .autoImage2D(Scope.GLOBAL, "specularTex", 1, ctx -> specular)
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
                        return normal.id();
                    }
                }).build();
    }

    @Override
    protected void renderGui() {


        ImGui.begin("Scale");

        if(ImGui.sliderFloat("Intensity", scale.getData(), 0f, 1f)) {
            isDirty = true;
        }

        ImGui.end();

        ImGui.begin("images");
        ImGui.image(normal.id(), 512, 512);
        ImGui.end();
    }

    @Override
    protected void render() {

        if(!isDirty) {

            computeProgram.useProgram();
            computeProgram.bindGlobal();

            computeProgram.dispatch(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT, (size.x + 15) / 16, (size.y + 15) / 16, 1);


            normal.printToTexture(name + "_normal.png");
            specular.printToTexture(name + "_specular.png");
            texture.printToTexture(name + "_albedo.png");
            isDirty = true;
        }
    }
}
