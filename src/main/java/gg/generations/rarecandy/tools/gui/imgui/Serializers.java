package gg.generations.rarecandy.tools.gui.imgui;

import com.google.gson.*;
import imgui.type.ImFloat;

import java.lang.reflect.Type;

public class Serializers {
    public static class ImFloatSerializer implements JsonSerializer<ImFloat>, JsonDeserializer<ImFloat> {

        @Override
        public ImFloat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new ImFloat(json.getAsFloat());
        }

        @Override
        public JsonElement serialize(ImFloat src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.get());
        }
    }
}
