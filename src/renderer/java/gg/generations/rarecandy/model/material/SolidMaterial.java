package gg.generations.rarecandy.model.material;

import gg.generations.pokeutils.reader.TextureReference;

public class SolidMaterial extends Material {
    private final String materialName;

    public SolidMaterial(TextureReference diffuseTexture) {
        this("", diffuseTexture);
    }

    public SolidMaterial(String materialName, TextureReference diffuseTexture) {
        super("solid", diffuseTexture);
        this.materialName = materialName;
    }

    public String getMaterialName() {
        return materialName;
    }
}
