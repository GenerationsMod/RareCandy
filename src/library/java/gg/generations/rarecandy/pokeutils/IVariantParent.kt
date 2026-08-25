package gg.generations.rarecandy.pokeutils

import gg.generations.rarecandy.codec.*

data class IVariantParent(
    var parent: String?,

    val details: MutableMap<String, IVariantDetails>) {

    constructor(): this(null, mutableMapOf())

    companion object {
        val CODEC: Codec<IVariantParent> = codec({
            obj().apply {
                putIfNotNull("parent", STRING, it.parent)
                putRest(IVariantDetails.CODEC, it.details)
            }
        }, {
            it.obj().let {
                IVariantParent(
                    it.readOrNull("parent", STRING),
                    it.readRestAsMutable(IVariantDetails.CODEC, "parent")
                )
            }
        })
    }
}
