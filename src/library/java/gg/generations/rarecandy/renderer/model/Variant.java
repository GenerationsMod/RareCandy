package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.animation.TransformSet;

import java.nio.ByteBuffer;

public record Variant(int material, int effect, boolean paradox, boolean hide, Transform[] transform) {
    public static final int SIZE = TransformSet.SIZE + Integer.BYTES * 4;

    public void put(ByteBuffer buffer) {

        transform[0].upload(buffer);
        transform[1].upload(buffer);
        transform[2].upload(buffer);
        transform[3].upload(buffer);
        buffer.putInt(material);
        buffer.putInt(effect);
        buffer.putInt(paradox ? 1 : 0);
        buffer.putInt(0);
    }
}
