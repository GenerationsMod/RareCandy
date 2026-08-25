package gg.generations.rarecandy.pokeutils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import gg.generations.rarecandy.codec.*
import gg.generations.rarecandy.pokeutils.resource.ResourceReader
import java.io.IOException
import java.io.InputStreamReader

data class IModelConfig(
    var scale: Float,
    val materials: MutableMap<String, IMaterialReference>,
    val defaultVariant: MutableMap<String, IVariantDetails>,
    val variants: MutableMap<String, IVariantParent>,
    val hideDuringAnimation: MutableMap<String, IHideDuringAnimation>,
    val animationFpsOverride: MutableMap<String, Int>,
    val animationLoopsOverride: MutableMap<String, Boolean>,
    val offsets: MutableMap<String, ISkeletalTransform>,
    val materialsWithSameMaterialAnimation: MutableMap<String, MutableList<String>>,
    val ignoreScaleInAnimation: MutableList<String>,
    val modelOptions: MutableMap<String, IMeshOptions>,
    val meshesToRenderFirst: MutableList<String>,
    val aliases: MutableMap<String, MutableList<String>>,
    val excludeMeshNamesFromSkeleton: Boolean,
    val resolution: Int?) {

    fun getMaterialsForAnimation(trackName: String?): MutableList<String?> {
        val list = ArrayList<String?>()
        list.add(trackName)

        if (materialsWithSameMaterialAnimation.containsKey(trackName)) {
            list.addAll(materialsWithSameMaterialAnimation[trackName]!!)
        }

        return list
    }

    companion object {
        val CODEC = codec({
            obj().also { obj ->
                obj.put("scale", FLOAT, it.scale)
                obj.putMap("materials", IMaterialReference.CODEC, it.materials)
                obj.putMap("defaultVariant", IVariantDetails.CODEC, it.defaultVariant)
                obj.putMap("variants", IVariantParent.CODEC, it.variants)
                obj.putMap("hideDuringAnimation", IHideDuringAnimation.CODEC, it.hideDuringAnimation)
                obj.putMap("animationFpsOverride", INT, it.animationFpsOverride)
                obj.putMap("animationLoopsOverride", BOOL, it.animationLoopsOverride)
                obj.putMap("offsets", ISkeletalTransform.CODEC, it.offsets)
                obj.putMap("materialsWithSameMaterialAnimation", STRING_LIST, it.materialsWithSameMaterialAnimation)
                obj.put("ignoreScaleInAnimation", STRING_LIST, it.ignoreScaleInAnimation)
                obj.putMap("modelOptions", IMeshOptions.CODEC, it.modelOptions)
                obj.put("meshesToRenderFirst", STRING_LIST, it.meshesToRenderFirst)
                obj.putMap("aliases", STRING_LIST, it.aliases)
                obj.put("excludeMeshNamesFromSkeleton", BOOL, it.excludeMeshNamesFromSkeleton)
                obj.putIfNotNull("resolution", INT, it.resolution)
            }
        }, {
            it.obj().let {
                IModelConfig(
                    it.read("scale", FLOAT, 1f),
                    it.readMap("materials", IMaterialReference.CODEC),
                    it.readMap("defaultVariant", IVariantDetails.CODEC),
                    it.readMap("variants", IVariantParent.CODEC),
                    it.readMap("hideDuringAnimation", IHideDuringAnimation.CODEC),
                    it.readMap("animationFpsOverride", INT),
                    it.readMap("animationLoopsOverride", BOOL),
                    it.readMap("offsets", ISkeletalTransform.CODEC),
                    it.readMap("materialsWithSameMaterialAnimation", STRING_LIST),
                    it.read("ignoreScaleInAnimation", STRING_LIST, mutableListOf()),
                    it.readMap("modelOptions", IMeshOptions.CODEC),
                    it.read("meshesToRenderFirst", STRING_LIST, mutableListOf()),
                    it.readMap("aliases", STRING_LIST),
                    it.read("excludeMeshNamesFromSkeleton", BOOL, false),
                    it.readOrNull("resolution", INT)
                )
            }
        })

        @Throws(IOException::class)
        fun from(asset: ResourceReader): IModelConfig {
            return asset.getFromJson(CODEC, "config.json")
        }

        @JvmField
        val GSON: Gson = GsonBuilder().setPrettyPrinting().setLenient().create()
    }
}

private fun <T> ResourceReader.getFromJson(codec: Decoder<T>, name: String): T {
    return this.getInputStream(name).reader().toJson().convert(codec);
}

private fun <T> JsonElement.convert(decoder: Decoder<T>): T {
    return decoder.decode(this)
}

private fun InputStreamReader.toJson(): JsonElement {
    return IModelConfig.GSON.fromJson(this, JsonElement::class.java)
}
