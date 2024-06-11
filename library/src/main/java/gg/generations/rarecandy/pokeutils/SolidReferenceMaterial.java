package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.model.material.Material;

import java.util.Map;

public class SolidReferenceMaterial {
    public static Material process(String name, String texture, Map<String, String> images) {
        var reference = images.get(texture);

        return new Material(name, Map.of("diffuse", reference), Map.of(), CullType.None, BlendType.None, "solid");
    }
}
