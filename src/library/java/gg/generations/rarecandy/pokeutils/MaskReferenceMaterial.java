package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.model.material.ImageSupplier;
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
    public Material process(String name, Map<String, String> images) {
        var reference = images.get(texture);
        var maskReference = images.get(maskTexture);

        return new Material(name, Map.of("diffuse", reference, "mask", maskReference), Map.of("color", color), cullType(), blendType(), shader());
    }
}