package gg.generations.rarecandy.model;

public record Variant(Material material, boolean hide) {
    public Variant(Material material) {
        this(material, false);
    }
}
