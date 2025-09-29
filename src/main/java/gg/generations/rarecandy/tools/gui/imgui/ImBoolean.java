package gg.generations.rarecandy.tools.gui.imgui;

import com.google.gson.*;
import imgui.ImGui;

import java.lang.reflect.Type;

public class ImBoolean {
    private imgui.type.ImBoolean value;

    public ImBoolean(boolean value) {
        this.value = new imgui.type.ImBoolean(value);
    }

    public void render(String name) {
        ImGui.checkbox(name, value);
    }

    public boolean getValue() {
        return value.get();
    }

    public static class Serializer implements JsonSerializer<ImBoolean>, JsonDeserializer<ImBoolean> {
        @Override
        public ImBoolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new ImBoolean(json.getAsBoolean());
        }

        @Override
        public JsonElement serialize(ImBoolean src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getValue());
        }
    }
}
