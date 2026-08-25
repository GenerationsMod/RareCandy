package gg.generations.rarecandy.renderer.model

import gg.generations.rarecandy.renderer.animation.ITransform
import gg.generations.rarecandy.renderer.animation.ITransformSet
import gg.generations.rarecandy.renderer.storage.SSBOBuffer

class Variant(
    val material: Int,
    val effect: Int,
    val paradox: Boolean,
    val hide: Boolean,
    val transform: Array<ITransform>
) {
    fun put(buffer: SSBOBuffer) {
        transform[0].upload(buffer)
        transform[1].upload(buffer)
        transform[2].upload(buffer)
        transform[3].upload(buffer)
        buffer.put(material)
        buffer.put(effect)
        buffer.put(paradox)
        buffer.put(0)
    }

    companion object {
        val SIZE: Int = ITransformSet.SIZE + Integer.BYTES * 4
    }
}
