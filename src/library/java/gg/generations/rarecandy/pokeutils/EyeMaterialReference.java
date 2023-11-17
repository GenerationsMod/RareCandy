package gg.generations.rarecandy.pokeutils;

public class EyeMaterialReference extends DiffuseMaterialReference {
    public EyeMaterialReference(String texture) {
        super(texture);
    }

    @Override
    public String shader() {
        return "eye";
    }
}

