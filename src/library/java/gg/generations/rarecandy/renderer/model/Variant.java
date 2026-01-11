package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.model.material.Material;

import java.nio.ByteBuffer;

public record Variant(int material, int effect, boolean paradox, boolean hide, Transform offset) {
    public static final int SIZE = Integer.BYTES * 3;

    public Variant(int material) {
        this(material, 0, false, false, null);
    }

    public void put(ByteBuffer buffer) {
        buffer.putInt(material);
        buffer.putInt(effect);
        buffer.putInt(paradox ? 1 : 0);
    }
}
