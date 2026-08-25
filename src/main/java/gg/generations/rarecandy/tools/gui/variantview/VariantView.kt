//package gg.generations.rarecandy.tools.gui.variantview
//
//import gg.generations.rarecandy.pokeutils.IModelConfig
//import gg.generations.rarecandy.renderer.animation.ITransform
//import gg.generations.rarecandy.renderer.components.MultiRenderObject
//import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler
//import gg.generations.rarecandy.renderer.textures.ITexture
//import gg.generations.rarecandy.tools.ImGuiImageViewer
//import gg.generations.rarecandy.tools.gui.PokeUtilsGui
//import gg.generations.rarecandy.tools.gui.RareCandyCanvas.BaseMultiRenderObject
//import gg.generations.rarecandy.tools.gui.TextureSlot
//import gg.generations.rarecandy.tools.gui.VariantTransformStore
//import gg.generations.rarecandy.tools.gui.variantview.NormalizedRectEditor.Companion.clamp
//import imgui.ImGui
//import imgui.ImVec2
//import java.util.function.Consumer
//
//class VariantView(private val pokeUtilsGui: PokeUtilsGui) {
//    private val rectEditor = NormalizedRectEditor()
//    private val areaMin = ImVec2()
//    private val areaMax = ImVec2()
//
//    fun render() {
//        val canvas = pokeUtilsGui.canvas
//
//        ImGui.begin("Variant")
//
//        val model = canvas.loadedModel ?: run {
//            ImGui.textDisabled("No mesh selected.")
//            ImGui.end()
//            return
//        }
//        val config = canvas.config
//        val meshId = canvas.selected?.meshId ?: return
//        val variantId = canvas.selected?.variantId ?: return
//
//        val variant = model.getVariant(meshId, variantId)
//        val material = model.materials[variant.material]
//        val images = model.images
//
//        var changed = false
//
//        if (ImGui.beginTable("variantTextures", TEXTURE_PREVIEW_COLUMNS)) {
//            for (slot in TextureSlot.ALL) {
//                ImGui.tableNextColumn()
//                ImGui.pushID(slot.ordinal)
//                ImGui.text(slot.label)
//
//                val textureId = material.images[slot.ordinal]
//                val texture = images.getLayerTexture(textureId)
//
//                changed = changed or drawSlot(
//                    model, config, meshId, variantId, slot, texture, variant.transform[slot.ordinal]
//                )
//
//                ImGui.popID()
//            }
//            ImGui.endTable()
//        }
//
//        if (changed) queueVariantRebuild()
//
//        ImGui.end()
//    }
//
//    private fun drawSlot(
//        model: BaseMultiRenderObject,
//        config: IModelConfig,
//        meshId: Int,
//        variantId: Int,
//        slot: TextureSlot,
//        texture: ITexture,
//        resolved: ITransform
//    ): Boolean {
//        ImGuiImageViewer.drawTexture(texture, TEXTURE_PREVIEW_SIZE, TEXTURE_PREVIEW_SIZE)
//        ImGui.getItemRectMin(areaMin)
//        ImGui.getItemRectMax(areaMax)
//
//        rectEditor.run(areaMin, areaMax, resolved.scale, resolved.offset)
//
//        var commit = rectEditor.released or rectEditor.render()
//
//        if (!commit) return false
//
//        clamp(rectEditor.scale, rectEditor.offset)
//        return VariantTransformStore.write(
//            VariantTransformStore.editable(model, config, meshId, variantId, slot, resolved),
//            rectEditor.scale,
//            rectEditor.offset
//        )
//    }
//
//    private fun queueVariantRebuild() {
//        val canvas = pokeUtilsGui.canvas
//        canvas.loadedModel?.onUpdate({
//            ModelObjectCompiler.rebuildVariants(
//                it,
//                canvas.config
//            )
//        })
//        pokeUtilsGui.handler.markDirty()
//    }
//
//    companion object {
//        private const val TEXTURE_PREVIEW_SIZE = 256
//        private const val TEXTURE_PREVIEW_COLUMNS = 2
//
//        private fun isValidSelection(model: BaseMultiRenderObject, meshId: Int, variantId: Int): Boolean {
//            return meshId >= 0 && meshId < model.variantRelationships.size && variantId >= 0 && variantId < model.variantRelationships[meshId].size
//        }
//    }
//}