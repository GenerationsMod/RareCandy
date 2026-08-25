package gg.generations.rarecandy.tools.gui

import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline
import gg.generations.rarecandy.renderer.pipeline.util.Scope
import gg.generations.rarecandy.renderer.textures.framebuffer.FrameBuffer
import gg.generations.rarecandy.renderer.textures.framebuffer.TextureTarget
import org.lwjgl.opengl.GL11C

data class FullscreenPass(
    private val base: () -> TraditionalPipeline.Builder,
    private val inputs: List<Input>,
    private val extraUniforms: (TraditionalPipeline.Builder) -> Unit,
    val enabled: () -> Boolean,
    val output: String?,
    val scale: Float
) {
    data class Input(val uniform: String, val source: Source)

    constructor(builder: Builder) : this(
        builder.base,
        builder.inputs,
        builder.extraUniforms,
        builder.enabled,
        builder.output,
        builder.scale)

    private var namedTargets: Map<String, FrameBuffer> = mapOf<String, FrameBuffer>()
    private var pipeline: TraditionalPipeline?
    private lateinit var gbuffer: FrameBuffer
    private lateinit var previous: FrameBuffer

    init {
        pipeline = compile()
    }

    fun reload() {
        var compiled = compile()
        pipeline?.destroy()
        pipeline = compiled
    }

    fun destroy() {
        pipeline?.destroy()
        pipeline = null
    }

    private fun compile(): TraditionalPipeline {
        val builder = base.invoke()

        // Unit is the declaration index, so no two inputs can collide.
        inputs.forEachIndexed { unit, input ->
            builder.autoSampler2D(Scope.GLOBAL, input.uniform, unit) { ctx -> resolve(input.source) }
        }

        extraUniforms.invoke(builder)
        return builder.build()
    }

    private fun resolve(source: Source): Int {
        return when (source) {
            is Source.Attachment -> gbuffer.getColorAttachment(source.index)?.id ?: 0
            is Source.Depth -> gbuffer.getDepthTexture().id
            is Source.Previous -> previous.getColorAttachment(0)?.id ?: 0
            is Source.Named -> ITextureLoader.instance().getTexture(source.key).id
            is Source.Pass -> {
                val buffer = namedTargets.get(source.key) ?: throw IllegalStateException("Pass reads '${source.key}' but no earlier enabled pass writes it")
                buffer.getColorAttachment(0)?.id ?: 0
            }

            else -> 0
        }
    }

    fun render(gbuffer: FrameBuffer, previous: FrameBuffer, named: Map<String, FrameBuffer>, target: FrameBuffer) {
        this.gbuffer = gbuffer
        this.previous = previous
        this.namedTargets = named

        target.bindAndSetViewport()
        target.setDrawAll()
        draw()
    }

    fun renderToScreen(gBuffer: FrameBuffer, previous: FrameBuffer, named: Map<String, FrameBuffer>, width: Int, height: Int) {
        this.previous = previous
        this.namedTargets = named

        FrameBuffer.unbindDraw()
        GL11C.glViewport(0, 0, width, height)
        draw()
    }

    private fun draw() {
        if(pipeline == null) return
        GL11C.glDisable(GL11C.GL_DEPTH_TEST)
        GL11C.glDisable(GL11C.GL_BLEND)


        pipeline!!.useProgram()
        pipeline!!.bindGlobal()
        GL11C.glDrawArrays(GL11C.GL_TRIANGLES, 0, 3)
    }

    class Builder(internal val base: () -> TraditionalPipeline.Builder) {
        internal val inputs = mutableListOf<Input>()
        internal var extraUniforms: (TraditionalPipeline.Builder) -> Unit = { }
        internal var enabled: () -> Boolean = { true }
        internal var output: String? = null
        internal var scale = 1.0f

        fun reads(uniform: String, source: Source): Builder {
            inputs.add(Input(uniform, source))
            return this
        }

        fun uniforms(consumer: (TraditionalPipeline.Builder) -> Unit): Builder {
            this.extraUniforms = consumer
            return this
        }

        fun enabledWhen(enabled: () -> Boolean): Builder {
            this.enabled = enabled
            return this
        }

        fun writesTo(key: String): Builder {
            this.output = key
            return this
        }

        fun scale(scale: Float): Builder {
            if (scale < 0.0f) throw IllegalArgumentException("Scale can't be less than 0, it was " + scale + "instead")
            this.scale = scale
            return this
        }

        fun build(): FullscreenPass = FullscreenPass(this)
    }

    companion object {
        fun of(base: () -> TraditionalPipeline.Builder): Builder = Builder(base)
    }
}