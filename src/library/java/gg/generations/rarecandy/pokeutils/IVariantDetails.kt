package gg.generations.rarecandy.pokeutils

import gg.generations.rarecandy.codec.BOOL
import gg.generations.rarecandy.codec.STRING
import gg.generations.rarecandy.codec.codec
import gg.generations.rarecandy.codec.obj
import gg.generations.rarecandy.codec.putIfNotNull
import gg.generations.rarecandy.codec.readOrNull
import gg.generations.rarecandy.renderer.animation.ITransformSet

data class IVariantDetails(
    var material: String?,
    var effect: String?,
    var paradox: Boolean?,
    var hide: Boolean?,
    var transform: ITransformSet?) {

    constructor(): this(null, null, null, null, null)

    companion object {
        val CODEC = codec({ set ->
            obj().also {
                it.putIfNotNull("material", STRING, set.material)
                it.putIfNotNull("effect", STRING, set.effect)
                it.putIfNotNull("paradox", BOOL, set.paradox)
                it.putIfNotNull("hide", BOOL, set.hide)
                it.putIfNotNull("transform", ITransformSet.CODEC, set.transform)
            }
        }, {
            it.obj().let {
                IVariantDetails(
                    it.readOrNull("material", STRING),
                    it.readOrNull("effect", STRING),
                    it.readOrNull("paradox", BOOL),
                    it.readOrNull("hide", BOOL),
                    it.readOrNull("transform", ITransformSet.CODEC)
                )
            }
        })
    }
}
