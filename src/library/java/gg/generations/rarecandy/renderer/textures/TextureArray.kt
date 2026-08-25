package gg.generations.rarecandy.renderer.textures

import org.lwjgl.opengl.*
import java.io.IOException
import java.lang.AutoCloseable
import java.nio.ByteBuffer


/**
 * Fixed-size texture array with explicit layer filling.
 * Views for each layer are created at construction.
 */
class TextureArray(val width: Int, val height: Int, @JvmField val type: ITexture.Type, layers: Int, useViews: Boolean) :
    AutoCloseable {

    // allocate array
    val id: Int = GL11C.glGenTextures()
    val layerCount: Int = layers
    private val layerViews: List<ITexture>

    constructor(width: Int, height: Int, layers: Int, useViews: Boolean) : this(
        width,
        height,
        ITexture.Type.RGBA8,
        layers,
        useViews
    )

    init {
        GL11C.glBindTexture(GL30C.GL_TEXTURE_2D_ARRAY, this.id)
        GL42C.glTexStorage3D(GL30C.GL_TEXTURE_2D_ARRAY, 1, type.internalFormat, width, height, layers)
        GL11C.glTexParameteri(GL30C.GL_TEXTURE_2D_ARRAY, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT)
        GL11C.glTexParameteri(GL30C.GL_TEXTURE_2D_ARRAY, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT)
        GL11C.glTexParameteri(GL30C.GL_TEXTURE_2D_ARRAY, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST)
        GL11C.glTexParameteri(GL30C.GL_TEXTURE_2D_ARRAY, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST)
        GL11C.glBindTexture(GL30C.GL_TEXTURE_2D_ARRAY, 0)

        if (useViews) {
            layerViews = (0..<layers).map { TextureArrayLayer(this, it) }.toList()
        } else {
            layerViews = mutableListOf()
        }
    }

    /**
     * Copies the contents of an existing ITexture into a specific layer.
     * Dimensions must match exactly.
     */
    fun fillLayer(layerIndex: Int, src: ITexture) {
        if(layerIndex in 0..layerCount)
        checkLayerIndex(layerIndex)

        GL43C.glCopyImageSubData(
            src.id, GL11C.GL_TEXTURE_2D, 0, 0, 0, 0,
            this.id, GL30C.GL_TEXTURE_2D_ARRAY, 0, 0, 0, layerIndex,
            src.width, src.height, 1
        )
    }

    /**
     * Uploads raw RGBA8 pixel data into a specific layer.
     * Buffer size must exactly match width*height*4.
     */
    fun fillLayer(layerIndex: Int, pixels: ByteBuffer) {
        checkLayerIndex(layerIndex)
        require(pixels.remaining() == width * height * 4) { "Pixel buffer size mismatch for layer $layerIndex" }

        GL11C.glBindTexture(GL30C.GL_TEXTURE_2D_ARRAY, this.id)
        GL12C.glTexSubImage3D(
            GL30C.GL_TEXTURE_2D_ARRAY,
            0,
            0, 0, layerIndex,
            width, height, 1,
            GL11C.GL_RGBA,
            GL11C.GL_UNSIGNED_BYTE,
            pixels
        )
        GL11C.glBindTexture(GL30C.GL_TEXTURE_2D_ARRAY, 0)
    }

    /** Returns the ITexture view for a specific layer.  */
    fun getLayerTexture(layerIndex: Int): ITexture {
        checkLayerIndex(layerIndex)
        return layerViews.get(layerIndex)
    }

    @Throws(IOException::class)
    override fun close() {
        for (view in layerViews) {
            view.delete()
        }
        GL11C.glDeleteTextures(this.id)
    }

    private fun checkLayerIndex(layerIndex: Int) {
        require(layerIndex in 0..<this.layerCount) { "Invalid layer index: $layerIndex" }
    }

    private data class TextureArrayLayer(
        override val width: Int,
        override val height: Int,
        override val id: Int = GL11.glGenTextures(),
        override val type: ITexture.Type = ITexture.Type.RGBA8,
        val layer : Int
    ) : ITexture {
        constructor(array : TextureArray, layer: Int) : this(array.width, array.height, layer = layer)

        init {
            GL43C.glTextureView(
                id,
                GL11C.GL_TEXTURE_2D,
                this.id,
                GL11C.GL_RGBA8,
                0, 1,
                layer, 1
            )
        }

        override fun delete() = GL11C.glDeleteTextures(id)

        override fun toString(): String = "TextureArrayLayer[$layer]"
    }
}
