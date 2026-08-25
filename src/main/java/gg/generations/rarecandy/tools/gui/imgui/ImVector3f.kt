package gg.generations.rarecandy.tools.gui.imgui

import com.google.gson.*
import imgui.ImGui
import org.joml.Vector3f
import java.lang.reflect.Type

class ImVector3f(x: Float, y: Float, z: Float) {
    private val array: FloatArray = floatArrayOf(x, y, z)

    @JvmField val value: Vector3f = Vector3f(x, y, z)

    fun render(name: String) : Boolean = when {
        ImGui.colorEdit3(name, array) -> {
            value.set(array)
            true
        }

        else -> false
    }


    class Serializer : JsonSerializer<Vector3f>, JsonDeserializer<Vector3f> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): Vector3f {
            val `object` = json.getAsJsonObject()
            return Vector3f(
                `object`.get("x").asFloat,
                `object`.get("y").asFloat,
                `object`.get("z").asFloat
            )
        }

        override fun serialize(src: Vector3f, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            val `object` = JsonObject()

            `object`.addProperty("x", src.x)
            `object`.addProperty("y", src.y)
            `object`.addProperty("z", src.z)

            return `object`
        }
    }
}
