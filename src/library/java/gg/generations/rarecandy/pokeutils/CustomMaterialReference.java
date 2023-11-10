package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.model.material.ImageSupplier;
import gg.generations.rarecandy.renderer.model.material.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomMaterialReference extends MaterialReference {
    public String shader = "solid";

    private CullType cull = CullType.None;

    private BlendType blend = BlendType.None;

    private Map<String, String> textureMap = new HashMap<>();

    @Override
    public Material process(String name, @NotNull Map<String, String> images) {
        var map = textureMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, entry -> images.get(entry.getValue())));

        return new Material(name, map, Map.of(), cull, blend, shader);
    }
}
