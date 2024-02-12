package gg.generations.rarecandy.arceus.model.pk;

public record Variant(PkMaterial material, boolean hide) {
    public Variant(PkMaterial material) {
        this(material, false);
    }
}
