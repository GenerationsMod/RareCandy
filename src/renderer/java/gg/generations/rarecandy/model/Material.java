package gg.generations.rarecandy.model;

import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.loading.Texture;

public class Material {

    private final String materialName;
    private final String type;
    public final TextureReference diffuseTextureReference;
    private Texture diffuseTexture;

    public Material(String materialName, TextureReference diffuseTexture) {
        this(materialName, "solid", diffuseTexture);
    }

    public Material(String materialName, String type, TextureReference diffuseTexture) {
        this.materialName = materialName;
        this.type = type;
        this.diffuseTextureReference = diffuseTexture;
    }

    public Texture getDiffuseTexture() {
        if(diffuseTexture == null) this.diffuseTexture = new Texture(diffuseTextureReference);
        return diffuseTexture;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getType() {
        return type;
    }
}
