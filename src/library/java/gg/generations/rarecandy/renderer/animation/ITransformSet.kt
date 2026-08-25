package gg.generations.rarecandy.renderer.animation

import gg.generations.rarecandy.codec.*
import gg.generations.rarecandy.pokeutils.IModelConfig

data class ITransformSet(
    var diffuse: ITransform?,
    var layer: ITransform?,
    var mask: ITransform?,
    var emission: ITransform?) {

    constructor() : this(null, null, null, null)

    val isUnit: Boolean
        get() = diffuse?.isUnit == true &&
                layer?.isUnit == true &&
                mask?.isUnit == true &&
                emission?.isUnit == true


    fun fillIn(filler: ITransformSet?): ITransformSet {
        val newDiffuse = diffuse ?: filler?.diffuse
        val newLayer = layer ?: filler?.layer
        val newMask = mask ?: filler?.mask
        val newEmission = emission ?: filler?.emission

        return ITransformSet(newDiffuse, newLayer, newMask, newEmission)
    }

    fun array(): Array<ITransform> {
        return arrayOf(
            diffuse ?: ITransform.DEFAULT,
            layer ?: ITransform.DEFAULT,
            mask ?: ITransform.DEFAULT,
            emission ?: ITransform.DEFAULT
        )
    }

    companion object {

        val CODEC: Codec<ITransformSet> = codec({ set ->
            obj().also {
                it.putIfNotNull("diffuse", ITransform.CODEC, set.diffuse)
                it.putIfNotNull("layer", ITransform.CODEC, set.layer)
                it.putIfNotNull("mask", ITransform.CODEC, set.mask)
                it.putIfNotNull("emission", ITransform.CODEC, set.emission)
            }
        }, {
            it.obj().let {
                ITransformSet(
                    it.readOrNull("diffuse", ITransform.CODEC),
                    it.readOrNull("layer", ITransform.CODEC),
                    it.readOrNull("mask", ITransform.CODEC),
                    it.readOrNull("emission", ITransform.CODEC)
                )
            }
        })

        @JvmField
        val SIZE: Int = ITransform.SIZE * 4

        @JvmField
        val DEFAULT: ITransformSet = ITransformSet(null, null, null, null)
    }
}


