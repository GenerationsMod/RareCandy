package gg.generations.rarecandy.tools

import gg.generations.rarecandy.renderer.launch.OpenGL
import gg.generations.rarecandy.renderer.pipeline.Pipelines
import gg.generations.rarecandy.renderer.pipeline.compute.ComputePipeline
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline
import gg.generations.rarecandy.renderer.pipeline.util.Scope
import gg.generations.rarecandy.renderer.pipeline.util.TextureIdSupplier
import gg.generations.rarecandy.renderer.pipeline.util.UniformUploadContext
import gg.generations.rarecandy.renderer.textures.BlankTexture
import gg.generations.rarecandy.renderer.textures.ITexture
import gg.generations.rarecandy.tools.gui.render
import imgui.ImGui
import imgui.type.ImFloat
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL42

class ComputeShaderDemo : AppBase("Compute Shader Test", 1024, 1024, OpenGL()) {
    private var computeProgram: ComputePipeline? = null
    private var quadProgram: TraditionalPipeline? = null

    // Example control uniforms
    private var time = 0f
    private val intensity = ImFloat(1f)
    private val color = Vector3f(1f, 0f, 0f)
    private val color2 = Vector3f(0f, 1f, 0f)

    private var target: BlankTexture? = null

    public override fun initGL() {
        // --- Texture ---
        target = BlankTexture(ITexture.Type.RGBA8, 1024, 1024, ITexture.ComputeAccess.WRITE_ONLY)

        // --- Compute Shader ---
        val computeSrc = """
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
            
            """.trimIndent()
        computeProgram = Pipelines.compute(computeSrc)
            .autoFloat(Scope.GLOBAL, "u_time") { time }
            .autoFloat(Scope.GLOBAL, "u_intensity") { intensity.get() }
            .autoVec3(Scope.GLOBAL, "u_color") { color }
            .autoVec3(Scope.GLOBAL, "u_color_2") { color2 }
            .autoImage2D(Scope.GLOBAL, "destTex", 0) { target }
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
                    return target!!.id
                }
            }).build()
    }

    override fun renderGui() {
        color.render("Color 1")
        color2.render("Color 2")

        intensity.render("Intensity", 0f, 1f)

        ImGui.begin("derp")
        ImGui.image(target!!.id.toLong(), 512f, 512f) // show the 1024x1024 image

        ImGui.end()
    }

    override fun render() {
        time += 0.016f

        computeProgram!!.useProgram()
        computeProgram!!.bindGlobal()

        // --- Run compute ---
        computeProgram!!.dispatch(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT, (width + 15) / 16, (height + 15) / 16, 1)

        // --- Draw quad ---
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT)
        quadProgram!!.useProgram()
        quadProgram!!.bindGlobal()

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, target!!.id)
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ComputeShaderDemo().run()
        }
    }
}
