package gg.generations.rarecandy.pokeutils;

public class SolidReferenceMaterial extends DiffuseMaterialReference {
    public SolidReferenceMaterial(String texture) {
        super(texture);
    }

    @Override
    public String shader() {
        return "solid";
    }
}
