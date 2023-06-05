package gg.generations.rarecandy.model.material;

import gg.generations.pokeutils.reader.TextureReference;

public class TransparentMaterial extends Material {
    public final float alpha;

    public TransparentMaterial(TextureReference diffuseTexture, float alpha) {
        super(diffuseTexture);
        this.alpha = alpha;
    }

    @Override
    public String getType() {
        return "transparent";
    }
}
