package gg.generations.rarecandy.tools.gui.imgui

import com.google.gson.*
import imgui.ImGui
import org.joml.Vector2f
import org.joml.Vector3f
import java.lang.reflect.Type

class ImVector2f(x: Float, y: Float) : Vector2f(x, y) {
    constructor() : this(0f, 0f)
    private val array: FloatArray = floatArrayOf(x, y)

    fun render(name: String) : Boolean = when {
        ImGui.colorEdit3(name, array) -> {
            set(array)
            true
        }

        else -> false
    }


    class Serializer : JsonSerializer<ImVector2f>, JsonDeserializer<ImVector2f> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): ImVector2f {
            val `object` = json.getAsJsonObject()
            return ImVector2f(
                `object`.get("x").asFloat,
                `object`.get("y").asFloat
            )
        }

        override fun serialize(src: ImVector2f, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            val `object` = JsonObject()

            `object`.addProperty("x", src.x)
            `object`.addProperty("y", src.y)

            return `object`
        }
    }
}
