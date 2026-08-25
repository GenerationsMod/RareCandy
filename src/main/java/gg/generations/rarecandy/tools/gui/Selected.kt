package gg.generations.rarecandy.tools.gui

import gg.generations.rarecandy.renderer.components.MultiRenderObject
import gg.generations.rarecandy.renderer.rendering.ObjectInstance

object Selected {
    var variant: String? = null
        set(variant) {
            field = variant
            this.variantId = -1

            if (variant == null || model == null) return

            this.variantId = model!!.variantNameToId.getOrDefault(variant, -1)
            if (variantId != -1 && instance != null) {
                instance!!.setVariant(variantId)
            }
        }
    var variantId: Int = -1
        private set
    var mesh: String? = null
        set(mesh) {
            field = mesh
            this.meshId = -1

            if (mesh == null || model == null) return

            this.meshId = model!!.meshNameToId.getOrDefault(mesh, -1)
        }
    var meshId: Int = -1
        private set

    private val model: MultiRenderObject?
        get() = ModelTracker.model

    private val instance: ObjectInstance?
        get() = ModelTracker.instance
}
