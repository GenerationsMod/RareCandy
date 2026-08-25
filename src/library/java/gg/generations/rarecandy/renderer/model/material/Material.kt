package gg.generations.rarecandy.renderer.model.material

import gg.generations.rarecandy.pokeutils.BlendType
import gg.generations.rarecandy.pokeutils.CullType
import gg.generations.rarecandy.renderer.storage.SSBOBuffer
import java.io.Closeable

@JvmRecord
data class Material(
    val images: IntArray?,
    val values: IMaterialValues?,
    @JvmField val cullType: CullType?,
    @JvmField val blendType: BlendType?,
    val colorMethod: Int
) : Closeable {
    fun put(buffer: SSBOBuffer) {
        buffer.put(images!![0])
        buffer.put(images[1])
        buffer.put(images[2])
        buffer.put(images[3])
        (values ?: IMaterialValues.DEFAULT).put(buffer)
        buffer.put(colorMethod)
        buffer.put(if (this.blendType == BlendType.Regular) 1 else 0)
    }

    override fun close() {
    }

    fun disableDepth(): Boolean {
        return this.values?.disableDepth ?: false
    }
}
