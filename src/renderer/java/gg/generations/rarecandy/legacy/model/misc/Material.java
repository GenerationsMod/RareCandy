package gg.generations.rarecandy.legacy.model.misc;

import gg.generations.pokeutils.ModelConfig;
import gg.generations.pokeutils.reader.TextureReference;

import java.util.Map;

public class Material {

    public final TextureReference diffuseTextureReference;
    private final String type;
    private ITexture diffuseTexture;

    public Material(String type, TextureReference diffuseTexture) {
        this.type = type;
        this.diffuseTextureReference = diffuseTexture;
    }

    public Material(ModelConfig.MaterialReference value, Map<String, TextureReference> images) {
        this(value.type(), images.get(value.texture()));
    }

    public ITexture getDiffuseTexture() {
        if (diffuseTexture == null) this.diffuseTexture = ITexture.generate(diffuseTextureReference);
        return diffuseTexture;
    }

    public String getType() {
        return type;
    }
}
