package gg.generations.rarecandy.pokeutils;

public class UnlitMaterialReference extends DiffuseMaterialReference {
    public UnlitMaterialReference(String texture) {
        super(texture);
    }

    @Override
    public String shader() {
        return "unlit";
    }
}
