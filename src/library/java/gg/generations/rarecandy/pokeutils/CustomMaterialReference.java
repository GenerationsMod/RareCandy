package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;
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
    public Material process(String name, @NotNull Map<String, TextureReference> images) {
        var map = textureMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, entry -> {
            var reference = images.get(entry.getValue());

            if(reference != null) return new Material.ImageSupplier(reference);
            else return Material.ImageSupplier.BLANK;
        }));

        return new Material(name, map, cull, blend, shader);
    }
}
