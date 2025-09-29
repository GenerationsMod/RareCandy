package gg.generations.rarecandy.tools.gui.imgui;

import com.google.gson.*;
import imgui.ImGui;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public class ImVector3f {
    private final float[] array;
    private final Vector3f value;

    public ImVector3f(float x, float y, float z) {
        array = new float[] { x, y, z };
        value = new Vector3f(x,y,z);
    }

    public Vector3f getValue() {
        return value;
    }

    public void render(String name) {
        if(ImGui.colorEdit3(name, array)) {
            value.x = array[0];
            value.y = array[1];
            value.z = array[2];
        }
    }

    public static class Serializer implements JsonSerializer<ImVector3f>, JsonDeserializer<ImVector3f> {
        @Override
        public ImVector3f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var object = json.getAsJsonObject();
            return new ImVector3f(object.get("x").getAsFloat(), object.get("y").getAsFloat(), object.get("z").getAsFloat());
        }

        @Override
        public JsonElement serialize(ImVector3f src, Type typeOfSrc, JsonSerializationContext context) {
            var object = new JsonObject();

            object.addProperty("x", src.getValue().x);
            object.addProperty("y", src.getValue().y);
            object.addProperty("z", src.getValue().z);

            return object;
        }
    }
}
