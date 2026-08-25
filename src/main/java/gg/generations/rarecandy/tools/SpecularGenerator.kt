package gg.generations.rarecandy.tools

import gg.generations.rarecandy.pokeutils.resource.JarResourceReader
import gg.generations.rarecandy.pokeutils.resource.ResourceLocator
import gg.generations.rarecandy.renderer.launch.OpenGL
import gg.generations.rarecandy.renderer.pipeline.Pipelines
import gg.generations.rarecandy.renderer.pipeline.ShaderSource
import gg.generations.rarecandy.renderer.pipeline.SnippetFinder
import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline
import gg.generations.rarecandy.renderer.pipeline.util.Scope
import gg.generations.rarecandy.renderer.pipeline.util.TextureIdSupplier
import gg.generations.rarecandy.renderer.pipeline.util.TextureSupplier
import gg.generations.rarecandy.renderer.pipeline.util.UniformUploadContext
import gg.generations.rarecandy.renderer.textures.BlankTexture
import gg.generations.rarecandy.renderer.textures.ITexture
import gg.generations.rarecandy.renderer.textures.Texture
import imgui.ImGui
import imgui.type.ImFloat
import org.joml.Vector2i
import org.lwjgl.opengl.GL42
import java.io.IOException
import java.nio.file.Path

class SpecularGenerator : AppBase("Specular Generator", 1024, 1024, OpenGL()) {
    private val size = Vector2i()

    private var computeProgram: ComputePipeline? = null
    private var quadProgram: TraditionalPipeline? = null

    // Example control uniforms
    private val time = 0f
    private val scale = ImFloat(1.0f)

    private var normal: BlankTexture? = null
    private var specular: BlankTexture? = null
    var texture: Texture? = null
    private var isDirty = false

    private val name = "histy_ref"


    public override fun initGL() {
        // --- Texture ---


        try {
            texture = Texture(Texture.read(ResourceLocator.of(Path.of("")).getFile(name + ".png"), 4))
            texture!!.init()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        specular =
            BlankTexture(ITexture.Type.RGBA8, texture!!.width, texture!!.height, ITexture.ComputeAccess.WRITE_ONLY)
        normal = BlankTexture(ITexture.Type.RGBA8, texture!!.width, texture!!.height, ITexture.ComputeAccess.WRITE_ONLY)
        size.set(texture!!.width, texture!!.height)

        val computeSrc = """
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
            
            """.trimIndent()

        // --- Compute Shader ---
        val utils = SnippetFinder.create(JarResourceReader("shaders/snippets", ShaderSource::class.java))
            .addSnippet("lib", "utils")

        val source = ShaderSource.create().finder(utils).compile(computeSrc)

        println(source)

        computeProgram = Pipelines.compute(source)
            .autoImage2D(Scope.GLOBAL, "normalTex", 0, TextureSupplier { ctx: UniformUploadContext? -> normal })
            .autoSampler2D(Scope.GLOBAL, "srcTex", 0, TextureIdSupplier { ctx: UniformUploadContext? -> texture!!.id })
            .autoImage2D(Scope.GLOBAL, "specularTex", 1, TextureSupplier { ctx: UniformUploadContext? -> specular })
            .build()


        // --- Quad Shader ---
        val quadVert = """
            #version 430
            out vec2 v_uv;
            void main() {
                vec2 pos = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
                v_uv = pos;
                gl_Position = vec4(pos * 2.0 - 1.0, 0, 1);
            }
            
            """.trimIndent()
        val quadFrag = """
            #version 430
            in vec2 v_uv;
            out vec4 fragColor;
            uniform sampler2D tex;
            void main() {
                fragColor = texture(tex, v_uv);
            }
            
            """.trimIndent()
        quadProgram = Pipelines.traditional(quadVert, quadFrag)
            .autoSampler2D(Scope.GLOBAL, "tex", 0, object : TextureIdSupplier {
                override fun get(ctx: UniformUploadContext?): Int {
                    return normal!!.id
                }
            }).build()
    }

    override fun renderGui() {
        ImGui.begin("Scale")

        if (ImGui.sliderFloat("Intensity", scale.getData(), 0f, 1f)) {
            isDirty = true
        }

        ImGui.end()

        ImGui.begin("images")
        ImGui.image(normal!!.id.toLong(), 512f, 512f)
        ImGui.end()
    }

    override fun render() {
        if (!isDirty) {
            computeProgram!!.useProgram()
            computeProgram!!.bindGlobal()

            computeProgram!!.dispatch(
                GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT,
                (size.x + 15) / 16,
                (size.y + 15) / 16,
                1
            )


            normal!!.printToTexture(name + "_normal.png")
            specular!!.printToTexture(name + "_specular.png")
            texture!!.printToTexture(name + "_albedo.png")
            isDirty = true
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpecularGenerator().run()
        }
    }
}
