package gg.generations.rarecandy.pokeutils;

public class CullMaterialReference extends SolidReferenceMaterial {
    private CullType cullType;

    public CullMaterialReference(String texture, CullType type) {
        super(texture);
        cullType = type;
    }

    @Override
    public CullType cullType() {
        return cullType;
    }
}
