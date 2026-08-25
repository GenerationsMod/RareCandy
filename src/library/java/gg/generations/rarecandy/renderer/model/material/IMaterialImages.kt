package gg.generations.rarecandy.renderer.model.material

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import gg.generations.rarecandy.codec.STRING
import gg.generations.rarecandy.codec.codec
import gg.generations.rarecandy.codec.obj
import gg.generations.rarecandy.codec.putIfNotNull
import gg.generations.rarecandy.codec.readOrNull
import java.util.stream.Stream

data class IMaterialImages(
    var diffuse: String?,
    var layer: String?,
    var mask: String?,
    var emission: String?) {

    constructor(): this(null, null, null, null)

    fun stream(): Stream<String?> {
        return Stream.of<String?>(diffuse, emission, layer, mask)
    }

    val isDefault: Boolean
        get() = diffuse == null && layer == null && mask == null && emission == null

    fun toArray(imageNames: Array<String>): IntArray {
        return intArrayOf(
            imageNames.indexOf(diffuse),
            imageNames.indexOf(layer),
            imageNames.indexOf(mask),
            imageNames.indexOf(emission)
        )
    }
    companion object {
        val CODEC = codec<IMaterialImages>({ images ->
            obj().also {
                it.putIfNotNull("diffuse", STRING, images.diffuse)
                it.putIfNotNull("layer", STRING, images.layer)
                it.putIfNotNull("mask", STRING, images.mask)
                it.putIfNotNull("emission", STRING, images.emission)
            }
        }, {
            it.obj().let {
                IMaterialImages(
                    it.readOrNull("diffuse", STRING),
                    it.readOrNull("layer", STRING),
                    it.readOrNull("mask", STRING),
                    it.readOrNull("emission", STRING)
                )
            }
        })
    }
}
