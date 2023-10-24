package gg.generations.rarecandy.pokeutils;

public class UnlitCullMaterialReference extends UnlitMaterialReference {

    public UnlitCullMaterialReference(String texture) {
        super(texture);
    }

    @Override
    public CullType cullType() {
        return CullType.Back;
    }
}
