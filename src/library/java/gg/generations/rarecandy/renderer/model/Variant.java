package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.renderer.model.material.Material;

public record Variant(Material material, boolean hide) {
    public Variant(Material material) {
        this(material, false);
    }
}
