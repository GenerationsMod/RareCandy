package gg.generationsmod.rarecandy.model.config.rks.variant;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public interface VariantModifier {
    VariantModifier INVALID = new VariantModifier() {};

    class Adapter implements JsonDeserializer<VariantModifier> {

        @Override
        public VariantModifier deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            var object = json.getAsJsonObject();
            var modifierType = object.get("type").getAsJsonPrimitive().getAsString();

            return switch (modifierType) {
                case "set_texture" -> new SetTextureModifier(object);
                case "hide_mesh" -> new HideMeshModifier(object);
                case "show_mesh" -> new ShowMeshModifier(object);
                default -> INVALID;
            };
        }
    }
}
