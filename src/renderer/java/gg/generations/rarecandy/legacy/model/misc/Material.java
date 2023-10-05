package gg.generations.rarecandy.legacy.model.misc;

import gg.generations.pokeutils.ModelConfig;
import gg.generations.pokeutils.SoliddMaterialReference;
import gg.generations.pokeutils.util.ResourceLocation;

import java.util.Map;

public interface Material {
    String type();

    public static Material create(ModelConfig.MaterialReference reference, Map<String, ResourceLocation> images) {
        return switch (reference.type()) {
            case "solid" -> new SolidMaterial(images.get(((SoliddMaterialReference) reference).texture()));
            default -> null;
        };
    }
}
