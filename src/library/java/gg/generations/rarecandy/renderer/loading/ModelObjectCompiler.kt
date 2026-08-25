package gg.generations.rarecandy.renderer.loading

import gg.generations.rarecandy.pokeutils.*
import gg.generations.rarecandy.pokeutils.resource.ResourceReader
import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.renderer.animation.ITransformSet
import gg.generations.rarecandy.renderer.animation.Skeleton
import gg.generations.rarecandy.renderer.components.InstanceDetails
import gg.generations.rarecandy.renderer.components.MultiRenderObject
import gg.generations.rarecandy.renderer.model.Variant
import gg.generations.rarecandy.renderer.model.material.Material
import gg.generations.rarecandy.renderer.rendering.RenderStage
import gg.generations.rarecandy.renderer.storage.DrawBuffer
import gg.generations.rarecandy.renderer.storage.SSBOBuffer
import gg.generations.rarecandy.renderer.textures.Texture
import gg.generations.rarecandy.renderer.textures.TextureArray
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.assimp.*
import org.lwjgl.opengl.GL30C.glGetInteger
import org.lwjgl.opengl.GL43C.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.util.EnumMap

private const val MATERIAL_SIZE = 208
private const val VERTEX_BYTES = 96
const val DRAW_INFO_ENTRY_BYTES = Float.SIZE_BYTES * 4 + Int.SIZE_BYTES * 4

private val NORMAL_FACE = intArrayOf(0, 1, 2)
private val INVERT_FACE = intArrayOf(2, 1, 0)

private val N = Vector3f()
private val T = Vector3f()
private val B = Vector3f()
private val TEMP = Vector3f()

object ModelObjectCompiler {

    @JvmStatic
    @Throws(Exception::class)
    fun <T : MultiRenderObject> buildObject(
        objectBuilder: (Names) -> T,
        configSupplier: () -> IModelConfig,
        assetSupplier: () -> ResourceReader,
        imageFactory: (ResourceReader, Array<String>, Int) -> TextureArray,
        materialFactory: (String, Map<String, IMaterialReference>, Array<String>) -> Material,
        onFinish: (MultiRenderObject, Skeleton, MutableMap<String, AnimResource>) -> Unit,
    ) {
        val assets = assetSupplier.invoke()
        val config = configSupplier.invoke()
        val names = Names.from(config)
        val model = objectBuilder.invoke(names)

        model.images = imageFactory.invoke(assets, names.images, config.resolution ?: 1024)

        installMaterials(model, config, materialFactory)
        installVariants(model, config)
        model.scale = config.scale

        val resources = readAnimationResources(assets)
        val skeleton = installGeometry(model, assets, config)
        installAnimations(model, skeleton, resources, config)

        model.instance = SSBOBuffer(InstanceDetails.size)
        model.drawInfo = SSBOBuffer(maxOf(1, model.meshes.size) * DRAW_INFO_ENTRY_BYTES)

        onFinish.invoke(model, skeleton, resources)
    }

    /** Live-edit entry point. Material values may change; the name/ID layout may not. */
    @JvmStatic
    @Throws(Exception::class)
    fun rebuildMaterials(
        model: MultiRenderObject,
        config: IModelConfig,
        materialFactory: (String, Map<String, IMaterialReference>, Array<String>) -> Material,
    ) {
        installMaterials(model, config, materialFactory)
    }

    /** Live-edit entry point. Variant values may change; the name/ID layout may not. */
    @JvmStatic
    fun rebuildVariants(model: MultiRenderObject, config: IModelConfig) {
        installVariants(model, config)
    }

    @JvmStatic
    fun readAnimationResources(assets: ResourceReader): MutableMap<String, AnimResource> {
        val resources = LinkedHashMap<String, AnimResource>()
        SmdResource.read(assets, resources)
        GfbanmResource.read(assets, resources)
        TrAnimationResource.read(assets, resources)
        return resources
    }

    // ---- materials ----

    fun installMaterials(
        model: MultiRenderObject,
        config: IModelConfig,
        materialFactory: (String, Map<String, IMaterialReference>, Array<String>) -> Material,
    ) {
        val configured = config.materials
        val created = ArrayList<Material>(model.names.materials.size)
        var buffer: SSBOBuffer? = null

        try {
            for (name in model.names.materials) {
                created += materialFactory.invoke(name, configured, model.names.images)
            }

            buffer = SSBOBuffer(maxOf(1, created.size * MATERIAL_SIZE))
            created.forEach { it.put(buffer) }
            buffer.upload()
        } catch (failure: Throwable) {
            buffer?.delete()
            created.forEach(::closeQuietly)
            throw failure
        }

        val materials = created.toTypedArray()
        val previousMaterials = model.materials
        val previousBuffer = model.material

        model.materials = materials
        model.material = buffer

        previousMaterials?.forEach { previous ->
            if (materials.none { it === previous }) closeQuietly(previous)
        }
        if (previousBuffer != null && previousBuffer !== buffer) previousBuffer.delete()
    }

    // ---- variants ----

    fun installVariants(model: MultiRenderObject, config: IModelConfig) {
        val names = model.names
        val aliases = config.aliases
        val meshCount = names.meshes.size
        val slotCount = maxOf(1, names.variants.size)

        val relationships = Array(meshCount) { IntArray(slotCount) }
        val stages = Array(meshCount) { arrayOfNulls<RenderStage>(slotCount) }
        val drawBuffers = EnumMap<RenderStage, DrawBuffer>(RenderStage::class.java)
        val compiled = ArrayList<Variant>()
        val cache = HashMap<String, Map<String, IVariantDetails>>()
        var buffer: SSBOBuffer? = null

        try {
            for (slot in 0 until slotCount) {
                val resolved = if (names.variants.isEmpty()) {
                    expandAliases(config.defaultVariant, aliases)
                } else {
                    resolveVariant(names.variants[slot], config, aliases, cache, HashSet())
                }

                for (mesh in 0 until meshCount) {
                    val details = resolved.getValue(names.meshes[mesh])
                    val id = addOrGet(compiled, createVariant(details, names.materials))
                    val stage = RenderStage.from(model.materials?.get(compiled[id].material))

                    relationships[mesh][slot] = id
                    stages[mesh][slot] = stage
                    drawBuffers.getOrPut(stage) { DrawBuffer(Int.SIZE_BYTES * 4 * maxOf(1, meshCount)) }
                }
            }

            val variants = compiled.toTypedArray()
            buffer = SSBOBuffer(maxOf(1, variants.size * Variant.SIZE))
            variants.forEach { it.put(buffer) }
            buffer.upload()

            val previousBuffer = model.variant
            val previousDraw = model.drawBuffer

            model.variants = variants
            model.variantRelationships = relationships
            model.stageRelationships = stages as Array<Array<RenderStage>>
            model.drawBuffer = drawBuffers
            model.variant = buffer

            if (previousBuffer !== buffer) previousBuffer?.delete()
            if (previousDraw !== drawBuffers) {
                previousDraw?.values?.forEach(SSBOBuffer::delete)
            }
        } catch (failure: Throwable) {
            buffer?.delete()
            drawBuffers.values.forEach(SSBOBuffer::delete)
            throw failure
        }
    }

    private fun resolveVariant(
        key: String,
        config: IModelConfig,
        aliases: Map<String, List<String>>,
        cache: MutableMap<String, Map<String, IVariantDetails>>,
        resolving: MutableSet<String>,
    ): Map<String, IVariantDetails> {
        cache[key]?.let { return it }
        require(resolving.add(key)) { "Variant inheritance cycle involving: $key" }

        val resolved = LinkedHashMap(expandAliases(config.defaultVariant, aliases))
        val parent = config.variants[key]

        if (parent != null) {
            parent.parent?.let { mergeOverride(resolved, resolveVariant(it, config, aliases, cache, resolving)) }
            mergeOverride(resolved, expandAliases(parent.details, aliases))
        }

        resolving -= key
        return resolved.toMap().also { cache[key] = it }
    }

    private fun expandAliases(
        source: Map<String, IVariantDetails>,
        aliases: Map<String, List<String>>,
    ): Map<String, IVariantDetails> {
        val expanded = mutableMapOf<String, IVariantDetails>()
        source.forEach { (meshName, details) ->
            val aliasNames = aliases[meshName]
            if (aliasNames.isNullOrEmpty()) expanded[meshName] = details
            else aliasNames.forEach { expanded[it] = details }
        }
        return expanded
    }

    private fun mergeOverride(
        target: MutableMap<String, IVariantDetails>,
        source: Map<String, IVariantDetails>,
    ) = source.forEach { (mesh, details) ->
        target[mesh] = target[mesh]?.let { overlay(it, details) } ?: details
    }

    private fun overlay(lower: IVariantDetails, higher: IVariantDetails) = IVariantDetails(
        higher.material ?: lower.material,
        higher.effect ?: lower.effect,
        higher.paradox ?: lower.paradox,
        higher.paradox ?: lower.paradox,
        when {
            higher.transform == null -> lower.transform
            lower.transform == null -> higher.transform
            else -> higher.transform?.fillIn(lower.transform)
        },
    )

    private fun createVariant(details: IVariantDetails, materialNames: Array<String>) = Variant(
        materialNames.indexOf(details.material),
        when (details.effect) {
            "galaxy" -> 1
            "pastel" -> 2
            "shadow" -> 3
            "sketch" -> 4
            "vintage" -> 5
            else -> 0
        },
        details.paradox == true,
        details.hide == true,
        (details.transform ?: ITransformSet.DEFAULT).array(),
    )

    private fun addOrGet(variants: MutableList<Variant>, variant: Variant): Int {
        val existing = variants.indexOf(variant)
        if (existing >= 0) return existing
        variants += variant
        return variants.lastIndex
    }

    // ---- animations ----

    fun installAnimations(
        model: MultiRenderObject,
        skeleton: Skeleton,
        resources: Map<String, AnimResource>,
        config: IModelConfig,
    ) {
        val animationNames = resources.keys.toTypedArray()

        model.animations = Array(animationNames.size) { id ->
            compileAnimation(id, animationNames[id], resources.getValue(animationNames[id]), model.names, skeleton, config)
        }
        model.animationNames = animationNames
        model.animationNameToId = animationNames.withIndex().associate { (id, name) -> name to id }

        val hideRules = config.hideDuringAnimation
        model.hideDuringAnimation = Array(model.names.meshes.size) { mesh ->
            val rule = hideRules[model.names.meshes[mesh]] ?: IHideDuringAnimation.NONE
            BooleanArray(animationNames.size) { rule.check(animationNames[it]) }
        }
    }

    private fun compileAnimation(
        animationId: Int,
        animationName: String,
        resource: AnimResource,
        names: Names,
        skeleton: Skeleton,
        config: IModelConfig,
    ): Animation {
        val fps = config.animationFpsOverride[animationName] ?: resource.fps().toInt()
        val loops = config.animationLoopsOverride[animationName] ?: resource.loops()

        val offsets = HashMap(resource.offsets)
        resource.offsets.forEach { (track, offset) ->
            config.getMaterialsForAnimation(track).forEach { offsets[it] = offset }
        }

        val offsetsByMaterial = arrayOfNulls<Animation.Offset>(names.materials.size)
        offsets.forEach { (materialName, offset) ->
            names.materials.indexOf(materialName).takeIf { it >= 0 }?.let { offsetsByMaterial[it] = offset }
        }

        val ignoreScale = config.ignoreScaleInAnimation.let { animationName in it || "all" in it }

        val configured = config.offsets[animationName] ?: ISkeletalTransform.DEFAULT
        val divisor = if (config.scale == 0.0f) 1.0f else config.scale

        return Animation(
            animationId, fps, loops, skeleton, resource.nodes, offsetsByMaterial, ignoreScale,
            ISkeletalTransform(Vector3f(configured.position).div(divisor), Quaternionf(configured.rotation)),
        )
    }

    // ---- geometry ----

    fun installGeometry(
        model: MultiRenderObject,
        assets: ResourceReader,
        config: IModelConfig,
    ): Skeleton {
        val scene = read(assets)

        try {
            val rootNode = ModelNode.create(scene.mRootNode())
            val imported = Array(scene.mNumMeshes()) { AIMesh.create(scene.mMeshes()!!.get(it)) }
            val skeleton = Skeleton(rootNode, imported, config.excludeMeshNamesFromSkeleton)

            val order = model.names.meshes.withIndex().associate { (index, name) -> name to index }
            val selected = imported
                .filter { it.mName().dataString() in order }
                .sortedBy { order.getValue(it.mName().dataString()) }

            var vertexCount = 0
            var indexCount = 0
            var maxVertex = 0
            for (mesh in selected) {
                vertexCount += mesh.mNumVertices()
                indexCount += mesh.mNumFaces() * 3
                maxVertex = maxOf(maxVertex, mesh.mNumFaces() * 3)
            }

            val alignment = glGetInteger(GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT)
            val vertexBytes = vertexCount * VERTEX_BYTES
            val indexBytes = indexCount * Int.SIZE_BYTES
            val drawBytes = selected.size * Int.SIZE_BYTES
            val indexOffset = alignUp(vertexBytes, alignment)
            val drawOffset = indexOffset + alignUp(indexBytes, alignment)

            val vertexBuffer = MemoryUtil.memAlloc(maxOf(1, vertexBytes))
            val indexBuffer = MemoryUtil.memAlloc(maxOf(1, indexBytes))
            val drawBuffer = MemoryUtil.memAlloc(maxOf(1, drawBytes))
            val packed = MemoryUtil.memCalloc(maxOf(1, drawOffset + drawBytes))

            try {
                val counters = IntArray(2)
                val dimensions = Vector3f()
                val options = config.modelOptions

                val meshDrawCounts = IntArray(selected.size) {
                    processPrimitive(vertexBuffer, indexBuffer, drawBuffer, counters, skeleton, selected[it], options, dimensions)
                }

                val vertex = SbboOffset(0, vertexBytes)
                val index = SbboOffset(indexOffset, indexBytes)
                val meshOffsets = SbboOffset(drawOffset, drawBytes)

                if (vertexBytes > 0) vertex.put(packed, vertexBuffer.flip())
                if (indexBytes > 0) index.put(packed, indexBuffer.flip())
                if (drawBytes > 0) meshOffsets.put(packed, drawBuffer.flip())

                val modelBuffer = glGenBuffers()
                glBindBuffer(GL_SHADER_STORAGE_BUFFER, modelBuffer)
                glBufferData(GL_SHADER_STORAGE_BUFFER, packed, GL_STATIC_READ)
                glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

                val previous = model.modelBuffer
                model.meshes = meshDrawCounts
                model.vertex = vertex
                model.index = index
                model.meshOffsets = meshOffsets
                model.modelBuffer = modelBuffer
                model.maxVertex = maxVertex
                model.dimensions.set(dimensions)
                model.rootTransformation = rootTransformation(rootNode)

                if (previous != 0 && previous != modelBuffer) glDeleteBuffers(previous)
                return skeleton
            } finally {
                MemoryUtil.memFree(packed)
                MemoryUtil.memFree(drawBuffer)
                MemoryUtil.memFree(indexBuffer)
                MemoryUtil.memFree(vertexBuffer)
            }
        } finally {
            Assimp.aiReleaseImport(scene)
        }
    }

    private fun processPrimitive(
        vertexBuffer: ByteBuffer,
        indexBuffer: ByteBuffer,
        drawBuffer: ByteBuffer,
        counters: IntArray,
        skeleton: Skeleton,
        mesh: AIMesh,
        options: Map<String, IMeshOptions>,
        dimensions: Vector3f,
    ): Int {
        val meshName = mesh.mName().dataString()
        val faceOrder = if (options.getOrDefault(meshName, IMeshOptions.DEFAULT).invert) INVERT_FACE else NORMAL_FACE
        val vertexAmount = mesh.mNumVertices()
        val indexOffset = counters[0]
        val vertexOffset = counters[1]
        val drawCount = mesh.mNumFaces() * 3

        drawBuffer.putInt(indexOffset)
        counters[0] += drawCount

        val faces = mesh.mFaces()
        for (faceId in 0 until mesh.mNumFaces()) {
            val indices = faces.get(faceId).mIndices()
            indexBuffer
                .putInt(vertexOffset + indices.get(faceOrder[0]))
                .putInt(vertexOffset + indices.get(faceOrder[1]))
                .putInt(vertexOffset + indices.get(faceOrder[2]))
        }
        counters[1] += vertexAmount

        val positions = requireNotNull(mesh.mVertices()) { "Mesh positions missing: $meshName" }
        val uvs = requireNotNull(mesh.mTextureCoords(0)) { "Mesh UVs missing: $meshName" }
        val normals = requireNotNull(mesh.mNormals()) { "Mesh normals missing: $meshName" }
        val tangents = requireNotNull(mesh.mTangents()) { "Mesh tangents missing: $meshName" }
        val bitangents = requireNotNull(mesh.mBitangents()) { "Mesh bitangents missing: $meshName" }

        val boneIds = IntArray(vertexAmount * 4)
        val boneWeights = FloatArray(vertexAmount * 4)

        mesh.mBones()?.let { bones ->
            for (boneIndex in 0 until bones.capacity()) {
                val bone = AIBone.create(bones.get(boneIndex))
                val skeletonId = skeleton.getId(bone.mName().dataString())
                val weights = bone.mWeights()

                for (weightId in 0 until weights.capacity()) {
                    val weight = weights.get(weightId)
                    if (weight.mWeight() > 0.0f) {
                        addBoneData(boneIds, boneWeights, weight.mVertexId(), skeletonId, weight.mWeight())
                    }
                }
            }
        }

        val unweighted = boneWeights.all { it == 0.0f }

        for (vertexId in 0 until vertexAmount) {
            val position = positions.get(vertexId)
            val uv = uvs.get(vertexId)
            val normal = normals.get(vertexId)

            vertexBuffer
                .putFloat(position.x()).putFloat(position.y()).putFloat(position.z()).putFloat(0.0f)
                .putFloat(uv.x()).putFloat(1.0f - (uv.y() % 1.0f)).putFloat(0.0f).putFloat(0.0f)
                .putFloat(normal.x()).putFloat(normal.y()).putFloat(normal.z()).putFloat(0.0f)

            putTangent(vertexBuffer, tangents.get(vertexId), bitangents.get(vertexId), normal)

            if (unweighted) {
                vertexBuffer.putInt(1).putInt(0).putInt(0).putInt(0)
                vertexBuffer.putFloat(1.0f).putFloat(0.0f).putFloat(0.0f).putFloat(0.0f)
            } else {
                val base = vertexId * 4
                for (i in 0 until 4) vertexBuffer.putInt(boneIds[base + i])
                for (i in 0 until 4) vertexBuffer.putFloat(boneWeights[base + i])
            }

            dimensions.max(TEMP.set(position.x(), position.y(), position.z()))
        }

        return drawCount
    }

    private fun putTangent(target: ByteBuffer, tangent: AIVector3D, bitangent: AIVector3D, normal: AIVector3D) {
        N.set(normal.x(), normal.y(), normal.z())
        T.set(tangent.x(), tangent.y(), tangent.z())
        B.set(bitangent.x(), bitangent.y(), bitangent.z())

        N.mul(T.dot(N), TEMP)
        T.sub(TEMP).normalize()
        N.cross(T, TEMP)
        val handedness = if (TEMP.dot(B) < 0.0f) -1.0f else 1.0f

        target.putFloat(T.x()).putFloat(T.y()).putFloat(T.z()).putFloat(handedness)
    }

    private fun addBoneData(ids: IntArray, weights: FloatArray, vertexId: Int, boneId: Int, weight: Float) {
        val base = vertexId * 4
        for (influence in 0 until 4) {
            val index = base + influence
            if (weights[index] == 0.0f) {
                ids[index] = boneId
                weights[index] = weight
                return
            }
        }
    }

    private fun rootTransformation(rootNode: ModelNode): Matrix4f {
        val result = Matrix4f()
        fun accumulate(node: ModelNode) {
            result.add(node.transform)
            node.children.forEach(::accumulate)
        }
        accumulate(rootNode)
        return result
    }

    private fun alignUp(value: Int, alignment: Int) = (value + alignment - 1) / alignment * alignment

    // ---- shared ----

    private fun closeQuietly(material: Material?) {
        try {
            material?.close()
        } catch (_: Exception) {
            // Cleanup must not replace the original failure.
        }
    }

    @JvmStatic
    fun read(asset: ResourceReader): AIScene {
        val bytes = asset.getFile("model.glb")
        val buffer = MemoryUtil.memAlloc(bytes.size)
        buffer.put(bytes).flip()

        try {
            return Assimp.aiImportFileFromMemory(
                buffer,
                Assimp.aiProcess_Triangulate or Assimp.aiProcess_OptimizeMeshes
                        or Assimp.aiProcess_ImproveCacheLocality or Assimp.aiProcess_CalcTangentSpace,
                "glb",
            ) ?: throw RuntimeException(Assimp.aiGetErrorString())
        } finally {
            MemoryUtil.memFree(buffer)
        }
    }

    @JvmStatic
    fun readImages(asset: ResourceReader, imageNames: Array<String>, resolution: Int, usesViews: Boolean): TextureArray {
        val array = TextureArray(resolution, resolution, imageNames.size, usesViews)
        imageNames.forEachIndexed { i, name ->
            val image = Texture.getColorBuffer(asset.getFile(name), resolution)
            array.fillLayer(i, image)
            STBImage.stbi_image_free(image)
        }
        return array
    }

}