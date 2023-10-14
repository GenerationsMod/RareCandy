package gg.generations.rarecandy.pokeutils;

public class UnlitMaterialReference extends DiffuseMaterialReference {
    @Override
    public String shader() {
        return "unlit";
    }

    public UnlitMaterialReference(String texture) {
        super(texture);
    }
}
