package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;

//TODO: make room for future options for modifying meshes.
public record MeshOptions(boolean invert, List<String> aliases) {
    static JsonElement serialize(MeshOptions meshOptions, JsonSerializationContext ctx) {
        var json = new JsonObject();
        json.addProperty("invert", meshOptions.invert());
        json.add("aliases", meshOptions.aliases().stream().collect(Collector.of(
                JsonArray::new, JsonArray::add,
                (jsonElements, jsonElements2) -> {
                    jsonElements.addAll(jsonElements2);
                    return jsonElements;
                }
        )));
        return json;
    }

    static MeshOptions deserialzie(JsonElement json, JsonDeserializationContext ctx) {
        var invert = false;
        var aliases = Collections.<String>emptyList();

        if (json.isJsonPrimitive()) invert = json.getAsBoolean();
        else if (json.isJsonArray())
            aliases = json.getAsJsonArray().asList().stream().map(JsonElement::getAsJsonPrimitive).map(JsonPrimitive::getAsString).toList();
        else {
            var obj = json.getAsJsonObject();

            if (obj.has("invert")) invert = obj.getAsJsonPrimitive("invert").getAsBoolean();
            if (obj.has("aliases"))
                aliases = obj.getAsJsonArray("aliases").asList().stream().map(JsonElement::getAsJsonPrimitive).map(JsonPrimitive::getAsString).toList();
        }

        return new MeshOptions(invert, aliases);
    }
}

