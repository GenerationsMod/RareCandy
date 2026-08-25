package gg.generations.rarecandy.pokeutils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import gg.generations.rarecandy.codec.QUAT
import gg.generations.rarecandy.codec.VEC3
import gg.generations.rarecandy.codec.codec
import gg.generations.rarecandy.codec.obj
import gg.generations.rarecandy.codec.put
import gg.generations.rarecandy.codec.read
import gg.generations.rarecandy.pokeutils.util.JomlConstants
import org.joml.Quaternionf
import org.joml.Vector3f

data class ISkeletalTransform(
    val position: Vector3f,
    val rotation: Quaternionf) {

    fun scale(scale: Float): ISkeletalTransform {
        position.div(scale)
        return this
    }

    companion object {
        val CODEC = codec({
            obj().also {
                it.put("position", VEC3, DEFAULT.position)
                it.put("rotation", QUAT, DEFAULT.rotation)
            }
        }, {
            it.obj().let {
                ISkeletalTransform(
                    it.read("position", VEC3),
                    it.read("rotation", QUAT)
                )
            }
        })

        val DEFAULT: ISkeletalTransform = ISkeletalTransform(JomlConstants.VECTOR3F_ZERO, JomlConstants.QUATERIONF_ZERO);
    }
}
