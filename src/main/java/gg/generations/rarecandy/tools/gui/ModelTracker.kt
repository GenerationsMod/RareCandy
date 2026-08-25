package gg.generations.rarecandy.tools.gui

import gg.generations.rarecandy.pokeutils.IMaterialReference
import gg.generations.rarecandy.pokeutils.IModelConfig
import gg.generations.rarecandy.pokeutils.IVariantDetails
import gg.generations.rarecandy.pokeutils.IVariantParent
import gg.generations.rarecandy.pokeutils.resource.ResourceReader
import gg.generations.rarecandy.renderer.components.InstanceDetails
import gg.generations.rarecandy.renderer.components.MultiRenderObject
import gg.generations.rarecandy.renderer.loading.DRAW_INFO_ENTRY_BYTES
import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler
import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler.installAnimations
import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler.installGeometry
import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler.installMaterials
import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler.installVariants
import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler.readAnimationResources
import gg.generations.rarecandy.renderer.loading.Names
import gg.generations.rarecandy.renderer.rendering.RareCandy
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import gg.generations.rarecandy.renderer.storage.SSBOBuffer
import gg.generations.rarecandy.tools.gui.RareCandyCanvas.BaseMultiRenderObject
import imgui.ImGui
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

object ModelTracker {
    var config: IModelConfig? = null
    private var names: Names? = null
    private val materialState = MapState(-1, ::IMaterialReference)
    private val defaultVariantState = MapState(-1, ::IVariantDetails)
    private val variantState = MapState(-1, ::IVariantParent)
    private val variantsState = mutableMapOf<String, MapState<IVariantDetails>>()

    var model: MultiRenderObject? = null
    var instance: AnimatedObjectInstance? = null
    private val translation = Vector3f()
    private val rotation = Quaternionf()

    var scale: Float
        get() = model?.scale ?: 1.0f
        set(value) {
            model?.run {
                scale = value
                updateTransform()
            }
        }


    fun resetTransform() {
        translation.zero()
        rotation.identity()
        updateTransform()
    }

    private fun updateTransform() {
        instance?.modelMatrix()?.run {
            identity()
            translation(translation)
            rotate(rotation)
            scale(scale)
        }
        instance?.normalMatrix()?.run {
            identity()
            rotate(rotation)
            scale(scale)
        }
    }

    fun update(time: Double) {
        instance?.also { instance ->
            instance.modelMatrix().identity()
                .translate(translation)
                .rotation(rotation)
                .scale(this.scale)
            instance.normalMatrix().identity().rotation(rotation).scale(scale)
            instance.use()

            AnimationState.update(time)

            model?.also {
                AnimationState.animation?.let { animation ->
                    animation.getFrameTransform(AnimationState.seconds, instance.transforms)
                } ?: run {
                    instance.transforms.forEach { it.identity() }
                }
            }
        }
    }

    fun update(renderer: RareCandy, reader: ResourceReader) {
        val images = reader.fileNames.filter { it.endsWith(".png") }.toTypedArray()
        val config = IModelConfig.from(reader).also { this.config = it }
        val names = Names.from(config, images).also { this.names = it }

        val model = BaseMultiRenderObject(names).also {
            this.model?.close()
            model = it
        }

        this.model?.close()

        model.images = ModelObjectCompiler.readImages(reader, names.images, config.resolution ?: 1024, true)

        installMaterials(model, config, IMaterialReference::process)
        installVariants(model, config)
        model.scale = config.scale

        val resources = readAnimationResources(reader)
        val skeleton = installGeometry(model, reader, config)
        installAnimations(model, skeleton, resources, config)

        model.instance = SSBOBuffer(InstanceDetails.size)
        model.drawInfo = SSBOBuffer(maxOf(1, model.meshes.size) * DRAW_INFO_ENTRY_BYTES)

        resetTransform()

        config.scale.takeIf { it > 0f }?.run {
            model.scale = this
        }

        val variant = model.availableVariants.firstOrNull()?.let { model.variantNameToId[it] } ?: -1

        this.names = names
        this.model = model

        AnimationState.animationNames = ModelTracker.model?.animationNameToId?.keys?.toTypedArray()

        instance = AnimatedObjectInstance(Matrix4f(), Matrix3f(), variant).also { renderer.add<AnimatedObjectInstance>(model, it) }.also { it.use() }
    }

    fun render(): MutableSet<UpdateType> {
        val dirty = mutableSetOf<UpdateType>()

        val config = config ?: return mutableSetOf()
        val names = names ?: return mutableSetOf()

        ImGui.begin("Config")

        config.scale.render("Scale") { config.scale = it }.ifTrue { dirty += UpdateType.Scale }

        config.materials.render("Materials", materialState) { key, value -> value.render(key, names) }.ifTrue {
            dirty += UpdateType.Names
            dirty += UpdateType.Material
        }

        config.defaultVariant.render("Default Variants", defaultVariantState) { key, value -> value.render(key, names) }.ifTrue { dirty += UpdateType.Names }
        config.variants.render("Variant", variantState) { key, value -> value.render(key, names) }

        if(dirty.contains(UpdateType.Names)) {
            this.names = Names.from(config)
        }

        ImGui.end()

        return dirty
    }

    enum class UpdateType {
        Scale, Names, Material
    }
}