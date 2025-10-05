package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.model.material.Material;

public record Variant(int material, boolean hide, Transform offset) {
    public Variant(int material) {
        this(material, false, null);
    }
}
