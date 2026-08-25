package gg.generations.rarecandy.tools.gui.imgui

import com.google.gson.*
import imgui.ImGui
import java.lang.reflect.Type

class ImBoolean(value: Boolean) {
    val value: imgui.type.ImBoolean = _root_ide_package_.imgui.type.ImBoolean(value)

    fun render(name: String) {
        ImGui.checkbox(name, value)
    }

    class Serializer : JsonSerializer<ImBoolean>,
        JsonDeserializer<ImBoolean> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): ImBoolean {
            return ImBoolean(json.asBoolean)
        }

        override fun serialize(
            src: ImBoolean,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src.value.get())
        }
    }
}
