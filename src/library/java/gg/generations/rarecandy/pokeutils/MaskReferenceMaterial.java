package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.model.material.Material;
import org.joml.Vector3f;

import java.util.Map;

public class MaskReferenceMaterial extends DiffuseMaterialReference {
    private final String maskTexture;
    private final Vector3f color;

    public MaskReferenceMaterial(String texture, String maskTexture, Vector3f color) {
        super(texture);
        this.maskTexture = maskTexture;
        this.color = color;
    }

    @Override
    public String shader() {
        return "masked";
    }

    @Override
    public Material process(String name, Map<String, TextureReference> images) {
        var reference = images.get(texture);
        var maskReference = images.get(maskTexture);


        var diffuse = reference != null ? new Material.ImageSupplier(reference) : Material.ImageSupplier.BLANK;
        var mask = maskReference != null ? new Material.ImageSupplier(maskReference) : Material.ImageSupplier.BLANK;

        return new Material(name, Map.of("diffuse", diffuse, "mask", mask), Map.of("color", color), cullType(), blendType(), shader());
    }
}