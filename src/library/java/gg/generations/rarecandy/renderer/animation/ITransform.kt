package gg.generations.rarecandy.renderer.animation

import gg.generations.rarecandy.codec.*
import gg.generations.rarecandy.pokeutils.util.JomlConstants
import gg.generations.rarecandy.renderer.storage.SSBOBuffer
import org.joml.Vector2f
import java.nio.ByteBuffer
import kotlin.Boolean
import kotlin.Int
import kotlin.apply
import kotlin.let

data class ITransform(
    val scale: Vector2f,
    val offset: Vector2f) {

    constructor() : this(Vector2f(1f), Vector2f())

    val isUnit: Boolean
        get() = offset.x == 0f && offset.y == 0f && scale.x == 1f && scale.y == 1f

    fun upload(buffer: SSBOBuffer) {
        buffer.put(scale)
        buffer.put(offset)
    }

    fun upload(buffer: ByteBuffer) {
        buffer.putFloat(scale.x()).putFloat(scale.y()).putFloat(offset.x()).putFloat(offset.y())
    }

    companion object {
        val DEFAULT: ITransform = ITransform()
        val CODEC: Codec<ITransform> = codec({
            arr(4).apply {
                add(it.scale.x)
                add(it.scale.y)
                add(it.offset.x)
                add(it.offset.y)
            }
        }, {
            it.arr(4).let {
                val pos = Vector2f(it.readFloat(0), it.readFloat(1))
                val scale = Vector2f(it.readFloat(2), it.readFloat(3));

                ITransform(pos, scale)
            }
        }).readAlso {
            obj().let {
                ITransform(
                    it.readOrNull("scale", VEC2) ?: Vector2f(),
                    it.readOrNull("offset", VEC2) ?: Vector2f(1f)
                )
            }
        }

        val SIZE: Int = Float.SIZE_BYTES * 4
    }
}
