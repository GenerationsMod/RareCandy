package gg.generations.rarecandy.tools.gui

import gg.generations.rarecandy.renderer.textures.framebuffer.FrameBuffer
import gg.generations.rarecandy.renderer.textures.framebuffer.FrameBuffer.Companion.soloAlbedo
import org.lwjgl.opengl.GL11C
import java.lang.AutoCloseable
import java.util.function.Consumer
import kotlin.math.max

class PassChain(width: Int, height: Int) : AutoCloseable {
    private val passes: MutableList<FullscreenPass> = mutableListOf()

    private val a: FrameBuffer
    private val b: FrameBuffer

    private var width: Int
    private var height: Int

    private val named = HashMap<String, FrameBuffer>()
    private val namedScales = HashMap<String, Float?>()

    init {
        this.width = max(1, width)
        this.height = max(1, height)

        a = soloAlbedo(this.width, this.height)
        b = soloAlbedo(this.width, this.height)
    }

    fun add(pass: FullscreenPass): PassChain {
        val key = pass.output!!

        require(!named.containsKey(key)) { "A pass with the key '" + key + "' already exists; each one must be unique." }

        namedScales.put(key, pass.scale)
        named.put(key, soloAlbedo(scaled(width, pass.scale), scaled(height, pass.scale)))

        passes.add(pass)
        return this
    }


    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height

        a.resize(max(1, width), max(1, height))
        b.resize(max(1, width), max(1, height))

        named.forEach { (key: String?, fb: FrameBuffer?) ->
            val scale: Float = namedScales.get(key)!!
            fb!!.resize(scaled(width, scale), scaled(height, scale))
        }
    }

    fun reload() {
        passes.forEach { it.reload() }
    }

    fun render(gbuffer: FrameBuffer, width: Int, height: Int) {
        val active = passes.stream().filter{ it.enabled.invoke() }.toList()

        if (active.isEmpty()) {
            gbuffer.blitToScreen(0, width, height, GL11C.GL_COLOR_BUFFER_BIT, GL11C.GL_NEAREST)
            return
        }

        var previous = gbuffer

        for (pass in active) {
            val target: FrameBuffer = (if (pass.output != null) named[pass.output] else if (previous == a) b else a)!!

            pass.render(gbuffer, previous, named, target)
            previous = target
        }

        active.last().renderToScreen(gbuffer, previous, named, width, height)
    }

    override fun close() {
        passes.forEach(FullscreenPass::destroy)
        a.close()
        b.close()
        named.values.forEach(Consumer { obj: FrameBuffer? -> obj!!.close() })
        named.clear()
    }

    companion object {
        private fun scaled(value: Int, scale: Float): Int {
            return max(1, Math.round(value * scale))
        }
    }
}