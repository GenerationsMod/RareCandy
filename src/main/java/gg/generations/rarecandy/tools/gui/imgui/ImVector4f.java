package gg.generations.rarecandy.tools.gui.imgui;

import com.google.gson.*;
import imgui.ImGui;
import org.joml.Vector4f;

import java.lang.reflect.Type;

public class ImVector4f {
    private final float[] array;
    private final Vector4f value;

    public ImVector4f(float x, float y, float z, float w) {
        array = new float[] { x, y, z, w };
        value = new Vector4f(x,y,z,w);
    }

    public Vector4f getValue() {
        return value;
    }

    public boolean render(String name) {
        if(ImGui.colorEdit4(name, array)) {
            value.x = array[0];
            value.y = array[1];
            value.z = array[2];
            value.w = array[3];

            return true;
        } else return false;
    }

    public static class Serializer implements JsonSerializer<ImVector4f>, JsonDeserializer<ImVector4f> {
        @Override
        public ImVector4f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var object = json.getAsJsonObject();
            return new ImVector4f(object.get("x").getAsFloat(), object.get("y").getAsFloat(), object.get("z").getAsFloat(), object.get("w").getAsFloat());
        }

        @Override
        public JsonElement serialize(ImVector4f src, Type typeOfSrc, JsonSerializationContext context) {
            var object = new JsonObject();

            object.addProperty("x", src.getValue().x);
            object.addProperty("y", src.getValue().y);
            object.addProperty("z", src.getValue().z);
            object.addProperty("w", src.getValue().w);

            return object;
        }
    }
}
