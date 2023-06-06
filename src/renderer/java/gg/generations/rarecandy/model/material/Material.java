package gg.generations.rarecandy.model.material;

import gg.generations.pokeutils.ModelConfig;
import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.loading.Texture;

import java.util.Map;

public class Material {

    private final String type;
    public final TextureReference diffuseTextureReference;
    private Texture diffuseTexture;

    public Material(String type, TextureReference diffuseTexture) {
        this.type = type;
        this.diffuseTextureReference = diffuseTexture;
    }

    public Material(ModelConfig.MaterialReference value, Map<String, TextureReference> images) {
        this(value.type(), images.get(value.texture()));
    }

    public Texture getDiffuseTexture() {
        if(diffuseTexture == null) this.diffuseTexture = new Texture(diffuseTextureReference);
        return diffuseTexture;
    }

    public String getType() {
        return type;
    }
}
