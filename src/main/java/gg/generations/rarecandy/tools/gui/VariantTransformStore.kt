//package gg.generations.rarecandy.tools.gui
//
//import gg.generations.rarecandy.pokeutils.IModelConfig
//import gg.generations.rarecandy.pokeutils.IVariantDetails
//import gg.generations.rarecandy.renderer.animation.ITransform
//import gg.generations.rarecandy.renderer.animation.ITransformSet
//import gg.generations.rarecandy.tools.gui.RareCandyCanvas.BaseMultiRenderObject
//import org.joml.Vector2f
//
//object VariantTransformStore {
//    fun editable(
//        model: BaseMultiRenderObject,
//        config: IModelConfig,
//        meshId: Int,
//        variantId: Int,
//        slot: TextureSlot,
//        resolved: ITransform
//    ): ITransform {
//        val target = target(model, config, variantId)
//        val meshName = model.names.meshes[meshId]
//
//        val details = target.details[meshName]
//        val transformSet = details?.transform
//        val existing = slot.of(transformSet)
//
//        if (existing != null) return existing
//
//        val created = copy(resolved)
//        target.details[meshName] = withTransforms(
//            details,
//            slot.with(transformSet, created),
//            if (target.defaultVariant) materialName(model, meshId, variantId) else null
//        )
//        return created
//    }
//
//    fun write(transform: ITransform, scale: Vector2f?, offset: Vector2f?): Boolean {
//        if (transform.scale == scale && transform.offset == offset) return false
//        transform.scale.set(scale)
//        transform.offset.set(offset)
//        return true
//    }
//
//    private fun target(
//        model: BaseMultiRenderObject,
//        config: IModelConfig,
//        variantId: Int
//    ): EditTarget {
//        val variantName = variantName(model, variantId)
//        val variants = config.variants
//
//        if (variantName == null || !variants.containsKey(variantName)) {
//            return EditTarget(config.defaultVariant, true)
//        }
//
//        val parent = variants.get(variantName)
//
//
//        if (parent != null && parent.details != null) {
//            return EditTarget(parent.details, false)
//        }
//
//        val details: MutableMap<String?, IVariantDetails?> = LinkedHashMap<String?, IVariantDetails?>()
//        variants.put(
//            variantName, IModelConfig.Factory.ACTIVE_FACTORY.createVariantParent(
//                if (parent != null) parent.parent else null,
//                details
//            )
//        )
//        return EditTarget(details, false)
//    }
//
//    private fun withTransforms(
//        details: IVariantDetails?,
//        transformSet: ITransformSet?,
//        fallbackMaterial: String?
//    ): IVariantDetails? {
//        var material = if (details != null) details.getMaterial else null
//        if (material == null) material = fallbackMaterial
//
//        return IModelConfig.Factory.ACTIVE_FACTORY.createVariantDetails(
//            material,
//            if (details != null) details.getEffect else null,
//            if (details != null) details.getParadox else null,
//            if (details != null) details.getHide else null,
//            transformSet
//        )
//    }
//
//    private fun materialName(
//        model: BaseMultiRenderObject,
//        meshId: Int,
//        variantId: Int
//    ): String? {
//        return model.names.materials.get(model.getVariant(meshId, variantId).material)
//    }
//
//    private fun variantName(model: BaseMultiRenderObject, variantId: Int): String? {
//        return if (variantId < model.names.variants.size)
//            model.names.variants.get(variantId)
//        else
//            null
//    }
//
//    private fun copy(transform: ITransform): ITransform {
//        return IModelConfig.Factory.ACTIVE_FACTORY.createTransform(
//            Vector2f(transform.scale),
//            Vector2f(transform.offset)
//        )
//    }
//
//    @JvmRecord
//    private data class EditTarget(val details: MutableMap<String, IVariantDetails>, val defaultVariant: Boolean)
//}