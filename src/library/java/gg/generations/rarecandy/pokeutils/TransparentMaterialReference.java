package gg.generations.rarecandy.pokeutils;

public class TransparentMaterialReference extends DiffuseMaterialReference {
    public TransparentMaterialReference(String texture) {
        super(texture);
    }

    @Override
    public BlendType blendType() {
        return BlendType.Regular;
    }

    @Override
    public String shader() {
        return "transparent";
    }
}
