package gg.generations.rarecandy.renderer.loading

import gg.generations.rarecandy.pokeutils.IMaterialReference
import gg.generations.rarecandy.pokeutils.IModelConfig
import gg.generations.rarecandy.pokeutils.IVariantDetails

class Names(
    val meshes: Array<String>,
    val variants: Array<String>,
    val images: Array<String>,
    val materials: Array<String>,
) {
    companion object {
        fun from(config: IModelConfig, imgs: Array<String>? = null): Names {
            val aliases = config.aliases.orEmpty()
            fun expand(mesh: String) = aliases[mesh]?.ifEmpty { null } ?: listOf(mesh)

            val meshes = mutableSetOf<String>()
            val variants = mutableSetOf<String>()
            val materials = mutableSetOf<String>()

            fun collect(mesh: String, details: IVariantDetails?) {
                if (details == null) return
                meshes += expand(mesh)
                details.material?.let(materials::add)
            }

            config.defaultVariant.forEach(::collect)
            config.variants.forEach { (variant, parent) ->
                variants += variant
                parent.details?.forEach(::collect)
            }

            val renderFirst = config.meshesToRenderFirst.flatMap(::expand).toSet()
            val (first, rest) = meshes.partition { it in renderFirst }

            val lookup = config.materials
            val images = materials.flatMapTo(LinkedHashSet()) {
                resolveImages(it, lookup, HashSet()).filterNotNull()
            }

            return Names(
                (first + rest).toTypedArray(),
                variants.toTypedArray(),
                imgs ?: images.toTypedArray(),
                materials.toTypedArray()
            )
        }

        private fun resolveImages(
            name: String,
            materials: Map<String, IMaterialReference>,
            seen: MutableSet<String>,
        ): List<String?> {
            val reference = materials[name]
            if (reference == null || !seen.add(name)) return EMPTY

            val own = reference.images
                ?.let { listOf(it.diffuse, it.layer, it.mask, it.emission) }
                ?: EMPTY
            val inherited = reference.parent
                ?.let { resolveImages(it, materials, seen) }
                ?: EMPTY

            seen -= name
            return own.zip(inherited) { child, parent -> parent ?: child }
        }

        private val EMPTY = listOf<String?>(null, null, null, null)
    }
}