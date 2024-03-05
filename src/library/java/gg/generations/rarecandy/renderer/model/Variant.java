package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.renderer.model.material.Material;
import org.joml.Vector2f;

public record Variant(Material material, boolean hide, Vector2f offset) {
    public Variant(Material material) {
        this(material, false, null);
    }
}
