package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;

public interface IMeshOptions {
    boolean invert();
    List<String> aliases();

    IMeshOptions DEFAULT = new IMeshOptions() {
        @Override
        public boolean invert() {
            return false;
        }

        @Override
        public List<String> aliases() {
            return Collections.emptyList();
        }
    };

    static JsonElement serialize(IMeshOptions meshOptions) {
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

    static IMeshOptions deserialize(JsonElement json) {
        var invert = false;
        var aliases = Collections.<String>emptyList();

        if (json.isJsonPrimitive()) invert = json.getAsBoolean();
        else if (json.isJsonArray()) {
            var parsedAliases = new ArrayList<String>();
            json.getAsJsonArray().forEach(element -> parsedAliases.add(element.getAsJsonPrimitive().getAsString()));
            aliases = parsedAliases;
        }
        else {
            var obj = json.getAsJsonObject();

            if (obj.has("invert")) invert = obj.getAsJsonPrimitive("invert").getAsBoolean();
            if (obj.has("aliases")) {
                var parsedAliases = new ArrayList<String>();
                obj.getAsJsonArray("aliases").forEach(element -> parsedAliases.add(element.getAsJsonPrimitive().getAsString()));
                aliases = parsedAliases;
            }
        }

        return IModelConfig.Factory.ACTIVE_FACTORY.createMeshOptions(invert, aliases);
    }
}
