package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.model.material.ImageSupplier;
import gg.generations.rarecandy.renderer.model.material.Material;

import java.util.Map;

public abstract class DiffuseMaterialReference extends MaterialReference {
    public String texture;

    public CullType cullType() {
        return CullType.None;
    }
    public BlendType blendType() {
        return BlendType.None;
    }

    public abstract String shader();

    public DiffuseMaterialReference(String texture) {
        this.texture = texture;
    }

    @Override
    public Material process(String name, Map<String, String> images) {
        var reference = images.get(texture);

        return new Material(name, Map.of("diffuse", reference), Map.of(), cullType(), blendType(), shader());
    }
}
