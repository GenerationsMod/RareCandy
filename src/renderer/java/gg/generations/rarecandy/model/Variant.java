package gg.generations.rarecandy.model;

import gg.generations.rarecandy.model.material.Material;

public record Variant(Material material, boolean hide) {
    public Variant(Material material) {
        this(material, false);
    }
}
