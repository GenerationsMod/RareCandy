package gg.generations.rarecandy.model.material;

import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.loading.Texture;

public class Material {

    public final TextureReference diffuseTextureReference;
    private Texture diffuseTexture;

    public Material(TextureReference diffuseTexture) {
        this.diffuseTextureReference = diffuseTexture;
    }

    public Texture getDiffuseTexture() {
        if(diffuseTexture == null) this.diffuseTexture = new Texture(diffuseTextureReference);
        return diffuseTexture;
    }

    public String getType() {
        return "solid";
    }
}
