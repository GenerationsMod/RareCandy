package gg.generations.pokeutils;

import com.google.gson.JsonObject;

import java.util.Objects;

public record SoliddMaterialReference(String texture) implements ModelConfig.MaterialReference {

    public String texture() {
        return texture;
    }

    public String type() {
        return "solid";
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("texture", texture);
        return obj;
    }
}
