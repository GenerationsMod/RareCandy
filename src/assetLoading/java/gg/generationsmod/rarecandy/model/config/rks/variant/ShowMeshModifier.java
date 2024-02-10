package gg.generationsmod.rarecandy.model.config.rks.variant;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ShowMeshModifier implements VariantModifier {

    public final List<String> meshes = new ArrayList<>();

    public ShowMeshModifier(JsonObject object) {
        var jsonMeshes = object.get("meshes").getAsJsonArray();

        for (var element : jsonMeshes)
            meshes.add(element.getAsJsonPrimitive().getAsString());
    }
}
