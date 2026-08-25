package gg.generations.rarecandy.renderer.components

import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.renderer.loading.Names
import gg.generations.rarecandy.renderer.loading.SbboOffset
import gg.generations.rarecandy.renderer.model.Variant
import gg.generations.rarecandy.renderer.model.material.Material
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline
import gg.generations.rarecandy.renderer.rendering.ObjectInstance
import gg.generations.rarecandy.renderer.rendering.RenderStage
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import gg.generations.rarecandy.renderer.storage.DrawBuffer
import gg.generations.rarecandy.renderer.storage.SSBOBuffer
import gg.generations.rarecandy.renderer.textures.TextureArray
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL43
import java.io.IOException
import java.util.*
import java.util.function.Consumer

/**
 * 
 */
abstract class MultiRenderObject(val names: Names) {

    @JvmField
    var meshNameToId = names.meshes.toIndexMap()
    var materialNameToId = names.materials.toIndexMap()
    @JvmField
    var variantNameToId: MutableMap<String, Int>
    @JvmField
    var imageNameToId: MutableMap<String, Int>

    var meshes: IntArray = IntArray(names.meshes.size)
    var materials: Array<Material>? = null
    var variants: Array<Variant>? = null
    var images: TextureArray? = null
    var variantRelationships: Array<IntArray>? = null
    var stageRelationships: Array<Array<RenderStage>>? = null

    var vertex: SbboOffset? = null
    var index: SbboOffset? = null
    var meshOffsets: SbboOffset? = null
    var material: SSBOBuffer? = null
    var variant: SSBOBuffer? = null
    var instance: SSBOBuffer? = null
    var drawInfo: SSBOBuffer? = null

    var drawBuffer: EnumMap<RenderStage, DrawBuffer>? = null

    @JvmField
    var modelBuffer: Int = 0

    var maxVertex: Int = 0

    var animations: Array<Animation>? = null
    var animationNames: Array<String>? = null
    var animationNameToId: Map<String, Int>? = null
    var hideDuringAnimation: Array<BooleanArray>? = null

    private val queue = ArrayList<(MultiRenderObject) -> Unit>()

    val dimensions: Vector3f = Vector3f()
    var scale = 1.0f

    var rootTransformation = Matrix4f()

    protected val instances = mutableListOf<ObjectInstance>()

    init {
        materialNameToId = names.materials.toIndexMap()
        imageNameToId = names.images.toIndexMap()
        variantNameToId = names.variants.toIndexMap()
    }

    fun onUpdate(consumer: (MultiRenderObject) -> Unit) {
        queue.add(consumer)
    }

    fun applyRootTransformation(state: ObjectInstance) {
        state.modelMatrix().mul(rootTransformation, state.modelMatrix())
    }

    fun update(absoluteTime: Double) {
        queue.forEach { consumer -> consumer.invoke(this) }
        queue.clear()

        instances.removeIf { !it.isLinked }
        instances.forEach { instance -> instance.update(absoluteTime) }

        updateSSBOs()
    }

    val availableVariants: MutableSet<String>
        get() = variantNameToId.keys

    fun getMaterial(mesh: Int, variant: Int): Material? {
        return getVariant(mesh, variant)?.material?.let { materials?.get(it) }
    }

    abstract fun render(pipeline: TraditionalPipeline, stage: RenderStage, instances: MutableList<ObjectInstance>)

    fun render(pipeline: TraditionalPipeline, stage: RenderStage) {
        render(pipeline, stage, instances)
    }

    fun shouldRender(mesh: Int, instance: ObjectInstance): Boolean {
        if (instance is AnimatedObjectInstance) {
//            val animation = instance.currentAnimation
//
//            if (animation != null) {
//                val animId = instance.currentAnimation!!.getAnimation().id
//                if (hideDuringAnimation[mesh][animId]) {
//                    return false
//                }
//            }
        }

        return !(getVariant(mesh, instance.variant())?.hide ?: true)
    }

    fun getVariant(mesh: Int, variant: Int): Variant? {
        return variantRelationships?.get(mesh)?.get(variant)?.let { variants?.get(it) }
    }

    @Throws(IOException::class)
    fun close() {
        this.materials?.forEach { it.close() }

        this.images?.close()
        GL43.glDeleteBuffers(modelBuffer)
        this.drawInfo?.delete()
        this.instance?.delete()
        this.variant?.delete()
        this.material?.delete()

        drawBuffer?.values?.forEach(DrawBuffer::delete)
    }

    val isEmpty: Boolean
        get() = instances.isEmpty()

    fun updateSSBOs() {
        ensureCapacity()
        resetSSBOs()

        var drawId = 0

        for (instanceId in instances.indices) {
            val instance = instances[instanceId]
            instance.update(this.instance)

            for (meshId in meshes.indices) {
                val stage = stageRelationships?.getOrNull(meshId)?.getOrNull(instance.variant()) ?: continue

                if (!shouldRender(meshId, instance)) continue

                val buffer = drawBuffer?.get(stage) ?: continue

                //TODO: Redo material animation
//                var variant = getVariant(meshId, instance.variant());
//                var material = variant.material();
//                Transform animationTransform = Transform.DEFAULT;


//                if (instance instanceof AnimatedObjectInstance animatedInstance) {
//
//                    var t = animatedInstance.getTransform(material);
//
//                    if (t != null && !t.isUnit()) {
//                        animationTransform = t;
//                    }
//                }

//                Transform.combine(animationTransform).upload(drawInfoBuffer);

                val variantId = variantRelationships?.getOrNull(meshId)?.get(instance.variant()) ?: return

                drawInfo?.put(variantId)
                drawInfo?.put(instanceId)
                drawInfo?.put(meshId)
                drawInfo?.put(0)

                buffer.putDraw(meshes[meshId], 1, 0, drawId)
                drawId++
            }
        }

        instance?.upload()
        drawInfo?.upload()
        drawBuffer?.values?.forEach { it.upload() }
    }

    private fun resetSSBOs() {
        instance?.reset()
        drawInfo?.reset()

        drawBuffer?.values?.forEach { it.reset() }
    }

    fun <T : ObjectInstance> add(instance: T): Boolean {
        if (!instance.isLinked) {
            instance.link(this)
            instances.add(instance)

            return true
        }

        return false
    }

    private fun ensureCapacity() {
        val size = instances.size

        instance?.ensureCapacity(size.toLong() * InstanceDetails.size)
        drawInfo?.ensureCapacity((instances.size.toLong() * meshes.size * TRANSFORM_ENTRY_BYTES))

        val drawSize = meshes.size.toLong() * size * Integer.BYTES * 4

        drawBuffer?.values?.forEach { it.ensureCapacity(drawSize) }
    }

    abstract fun targetVertexStride(): Int

    fun numOfInstances(): Int {
        return instances.size
    }

    companion object {
        private val TRANSFORM_ENTRY_BYTES = Float.SIZE_BYTES * 4 + Integer.BYTES * 4


        fun <T> Array<T>.toIndexMap(): MutableMap<T, Int> = this.foldIndexed(mutableMapOf()) { index, acc, element ->
            acc[element] = index
            acc
        }
    }
}
