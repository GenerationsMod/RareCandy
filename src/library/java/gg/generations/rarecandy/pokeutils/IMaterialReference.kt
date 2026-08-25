package gg.generations.rarecandy.pokeutils

import gg.generations.rarecandy.codec.*
import gg.generations.rarecandy.renderer.model.material.IMaterialImages
import gg.generations.rarecandy.renderer.model.material.IMaterialValues
import gg.generations.rarecandy.renderer.model.material.Material
import org.joml.Vector3f

data class IMaterialReference(
    var parent: String?,
    var shader: String?,
    var cull: CullType?,
    var blend: BlendType?,
    var images: IMaterialImages?,
    var values: IMaterialValues?
) {
    constructor() : this(null, null, null, null, null, null)

    companion object {
        val CODEC = codec<IMaterialReference>(
            { reference ->
                obj().also {
                    it.putIfNotNull("parent", STRING, reference.parent)
                    it.putIfNotNull("shader", STRING, reference.shader)
                    it.putIfNotEquals("cull", CullType.CODEC, { it == CullType.None }, reference.cull)
                    it.putIfNotEquals("blend", BlendType.CODEC, { it == BlendType.None }, reference.blend)
                    it.putIfNotEquals("images", IMaterialImages.CODEC, { it.isDefault }, reference.images)
                    it.putIfNotEquals("values", IMaterialValues.CODEC, { it.isDefault }, reference.values)
                }
            },
            {
                it.obj().let {
                    IMaterialReference(
                        it.readOrNull("parent", STRING),
                        it.readOrNull("shader", STRING),
                        it.readOrNull("cull", CullType.CODEC),
                        it.readOrNull("blend", BlendType.CODEC),
                        it.readOrNull("images", IMaterialImages.CODEC),
                        it.readOrNull("values", IMaterialValues.CODEC)
                    )
                }
            })

        operator fun <T> MutableSet<T>.plus(element: T): MutableSet<T> {
            this.add(element)
            return this
        }

        fun <T> resolve(name: String, seen: MutableSet<String>, references: Map<String, T>,
                        parentExtract: (T) -> String?, acc: T, block: T.(T) -> Unit): T {
            val own = references[name] ?: return acc
            val parentName = parentExtract(own)
            if (parentName != null && parentName !in seen) {
                resolve(parentName, seen + name, references, parentExtract, acc, block)
            }
            acc.block(own)

            return acc
        }

        fun process(
            base: String,
            references: Map<String, IMaterialReference>,
            imageNames: Array<String>
        ): Material {
            val reference = resolve(base, mutableSetOf(), references, { it.parent }, IMaterialReference()) { own ->
                parent = null
                shader = own.shader ?: shader
                cull   = own.cull   ?: cull
                blend  = own.blend  ?: blend

                own.images?.let { src ->
                    val dst = images ?: IMaterialImages().also { images = it }
                    dst.diffuse = src.diffuse ?: dst.diffuse
                    dst.layer = src.layer ?: dst.layer
                    dst.mask = src.mask ?: dst.mask
                    dst.emission = src.emission ?: dst.emission
                }

                own.values?.let { src ->
                    val dst = values ?: IMaterialValues().also { values = it }
                    dst.baseColor1 = src.baseColor1 ?: dst.baseColor1
                    dst.baseColor2 = src.baseColor2 ?: dst.baseColor2
                    dst.baseColor3 = src.baseColor3 ?: dst.baseColor3
                    dst.baseColor4 = src.baseColor4 ?: dst.baseColor4
                    dst.baseColor5 = src.baseColor5 ?: dst.baseColor5

                    dst.emiColor1 = src.emiColor1 ?: dst.emiColor1
                    dst.emiColor2 = src.emiColor2 ?: dst.emiColor2
                    dst.emiColor3 = src.emiColor3 ?: dst.emiColor3
                    dst.emiColor4 = src.emiColor4 ?: dst.emiColor4
                    dst.emiColor5 = src.emiColor5 ?: dst.emiColor5

                    dst.emiIntensity1 = src.emiIntensity1 ?: dst.emiIntensity1
                    dst.emiIntensity2 = src.emiIntensity2 ?: dst.emiIntensity2
                    dst.emiIntensity3 = src.emiIntensity3 ?: dst.emiIntensity3
                    dst.emiIntensity4 = src.emiIntensity4 ?: dst.emiIntensity4
                    dst.emiIntensity5 = src.emiIntensity5 ?: dst.emiIntensity5

                    dst.useLight = src.useLight ?: dst.useLight
                    dst.disableDepth = src.disableDepth ?: dst.disableDepth
                }
            }


            val images = reference.images?.toArray(imageNames) ?: IntArray(4, { -1 })

            var method = 0
            if (reference.shader != null) {
                method = when (reference.shader) {
                    "layered" -> 1
                    "masked" -> 2
                    else -> 0
                }
            }

            return Material(
                images,
                reference.values,
                reference.cull ?: CullType.None,
                reference.blend ?: BlendType.None,
                method
            )
        }
    }
}

