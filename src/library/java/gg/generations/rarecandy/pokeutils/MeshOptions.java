package gg.generations.rarecandy.pokeutils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

//TODO: make room for future options for modifying meshes.
public record MeshOptions(boolean invert) {
    public static class Serializer implements JsonDeserializer<MeshOptions> {
        @Override
        public MeshOptions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if(json.isJsonPrimitive()) return new MeshOptions(json.getAsBoolean());
            else {
                var invert = json.getAsJsonObject().getAsJsonPrimitive("invert").getAsBoolean();
                return new MeshOptions(invert);
            }
        }
    }

}
